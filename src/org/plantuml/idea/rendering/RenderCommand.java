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

    protected String sourceFilePath;
    protected final String source;
    protected final File baseDir;
    protected final int page;
    protected int zoom;
    protected RenderCacheItem cachedItem;
    protected int version;
    private boolean renderUrlLinks;

    public RenderCommand(String sourceFilePath, String source, File baseDir, int page, int zoom, RenderCacheItem cachedItem, int version, boolean renderUrlLinks) {
        this.sourceFilePath = sourceFilePath;
        this.source = source;
        this.baseDir = baseDir;
        this.page = page;
        this.zoom = zoom;
        this.cachedItem = cachedItem;
        this.version = version;
        this.renderUrlLinks = renderUrlLinks;
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

        } catch (Exception e) {
            logger.warn("Exception occurred rendering source = " + source + ": " + e, e);
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
        ImageWithUrlData[] imagesWithUrlData;
        if (cachedItem != null && cachedItem.getImagesWithData().length == pages) {
            ImageWithUrlData[] imagesWithData = cachedItem.getImagesWithData();
            ImageWithUrlData[] result = new ImageWithUrlData[imagesWithData.length];
            for (int i = 0; i < imagesWithData.length; i++) {
                if (i == page) {
                    //requested to render, update source in case there was no change in this page, so it does not get rendered again all the time
                    result[i] = ImageWithUrlData.deepCloneWithNewSource(imagesWithData[i], source);
                } else {
                    result[i] = imagesWithData[i];
                } 
            }
            imagesWithUrlData = result;
        } else {
            imagesWithUrlData = new ImageWithUrlData[pages];
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
                imagesWithUrlData[page] = new ImageWithUrlData(source, imageDiagram.getDescription(), pngBytes, svgBytes, baseDir);
            }
        }
        return imagesWithUrlData;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("sourceFilePath", sourceFilePath)
                .append("selectedDir", baseDir.getName())
                .append("page", page)
                .append("zoom", zoom)
                .append("cachedItem", cachedItem)
                .append("version", version)
                .toString();
    }
}
