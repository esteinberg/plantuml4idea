package org.plantuml.idea.rendering;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.plantuml.PlantUmlIncludes;
import org.plantuml.idea.util.ImageWithUrlData;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;


public abstract class RenderCommand implements Runnable {
    public static final Logger logger = Logger.getInstance(RenderCommand.class);

    protected Reason reason;
    protected String sourceFilePath;
    protected final String source;
    protected final File baseDir;
    protected final int page;
    protected int zoom;
    protected RenderCacheItem cachedItem;
    protected int version;
    protected boolean renderUrlLinks;
    protected LazyApplicationPoolExecutor.Delay delay;

    public enum Reason {
        INCLUDES,
        PAGE_OR_SOURCE,
        SOURCE
    }

    public RenderCommand(Reason reason, String sourceFilePath, String source, File baseDir, int page, int zoom, RenderCacheItem cachedItem, int version, boolean renderUrlLinks, LazyApplicationPoolExecutor.Delay delay) {
        this.reason = reason;
        this.sourceFilePath = sourceFilePath;
        this.source = source;
        this.baseDir = baseDir;
        this.page = page;
        this.zoom = zoom;
        this.cachedItem = cachedItem;
        this.version = version;
        this.renderUrlLinks = renderUrlLinks;
        this.delay = delay;
    }

    @Override
    public void run() {
        if (source.isEmpty()) {
            logger.debug("source is empty");
            return;
        }

        try {
            final Map<File, Long> includedFiles = PlantUmlIncludes.commitIncludes(source, baseDir);
            logger.debug("includedFiles=", includedFiles);

            final RenderRequest renderRequest = new RenderRequest(baseDir, source, PlantUml.ImageFormat.PNG, page, zoom, version);
            final RenderResult imageResult = PlantUmlRenderer.render(renderRequest, cachedItem);
            RenderResult svgResult = null;
            if (renderUrlLinks) {
                svgResult = PlantUmlRenderer.render(new RenderRequest(baseDir, source, PlantUml.ImageFormat.SVG, page, zoom, version), cachedItem);
            }
            final ImageWithUrlData[] imagesWithData = toImagesWithUrlData(source, imageResult, svgResult, baseDir);


            if (hasImages(imagesWithData)) {
                ApplicationManager.getApplication().invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        RenderCacheItem newItem = new RenderCacheItem(renderRequest, sourceFilePath, source, baseDir, zoom, page, includedFiles, imageResult, imagesWithData, version);
                        postRenderOnEDT(newItem);
                    }
                });

            } else {
                logger.debug("no images rendered");
            }

        } catch (RenderingCancelledException e) {
            logger.info("command interrupted");
        } catch (Exception e) {
            logger.error("Exception occurred rendering " + this, e);
        }
    }

    protected abstract void postRenderOnEDT(RenderCacheItem newItem);

    private boolean hasImages(ImageWithUrlData[] imagesWithUrlData) {
        for (ImageWithUrlData imageWithUrlData : imagesWithUrlData) {
            if (imageWithUrlData != null && imageWithUrlData.getImage() != null) {
                return true;
            }
        }
        return false;
    }

    private ImageWithUrlData[] toImagesWithUrlData(@NotNull String source, @NotNull RenderResult imageResult, @Nullable RenderResult svgResult, File baseDir) throws IOException {
        List<RenderResult.Diagram> imageDiagrams = imageResult.getDiagrams();
        List<RenderResult.Diagram> svgDiagrams = svgResult != null ? svgResult.getDiagrams() : null;
        int pages = imageResult.getPages();

        //noinspection UndesirableClassUsage
        ImageWithUrlData[] imagesWithUrlData = new ImageWithUrlData[pages];
        if (cachedItem != null) {
            ImageWithUrlData[] imagesWithData = cachedItem.getImagesWithData();
            for (int i = 0; i < imagesWithData.length; i++) {
                if (pages > i) {
                    imagesWithUrlData[i] = imagesWithData[i];
                }
            }
        }


        for (int i = 0; i < imageDiagrams.size(); i++) {
            RenderResult.Diagram imageDiagram = imageDiagrams.get(i);
            RenderResult.Diagram svgDiagram = svgDiagrams != null ? svgDiagrams.get(i) : null;

            if (imageDiagram != null) {
                int page = imageDiagram.getPage();
                byte[] pngBytes = imageDiagram.getDiagramBytes();
                byte[] svgBytes = svgDiagram != null ? svgDiagram.getDiagramBytes() : new byte[0];
                if (pngBytes == null) {
                    logger.error("pngBytes are null for: " + imageDiagram);
                    continue;
                }
                String description = imageDiagram.getDescription();
                imagesWithUrlData[page] = new ImageWithUrlData(imageDiagram.getDocumentSource(), imageDiagram.getPageSource(), description, pngBytes, svgBytes, baseDir);
            }
        }
//        for (int i = 0; i < imagesWithUrlData.length; i++) {
//            ImageWithUrlData imageWithUrlData = imagesWithUrlData[i];
//            if (imageWithUrlData == null) {
//                logger.debug("imageWithUrlData == null,  cachedItem=", cachedItem, ", imageResult=", imageResult);
//            }
//        }
        return imagesWithUrlData;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("reason", reason)
                .append("sourceFilePath", sourceFilePath)
                .append("selectedDir", baseDir.getName())
                .append("page", page)
                .append("zoom", zoom)
                .append("cachedItem", cachedItem)
                .append("version", version)
                .toString();
    }
}
