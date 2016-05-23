package org.plantuml.idea.rendering;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.plantuml.PlantUmlIncludes;
import org.plantuml.idea.toolwindow.ExucutionTimeLabel;

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
    protected ExucutionTimeLabel label;

    public enum Reason {
        INCLUDES,
        PAGE_OR_SOURCE,
        SOURCE
    }

    public RenderCommand(Reason reason, String sourceFilePath, String source, File baseDir, int page, int zoom, RenderCacheItem cachedItem, int version, boolean renderUrlLinks, LazyApplicationPoolExecutor.Delay delay, ExucutionTimeLabel label) {
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
        this.label = label;
    }

    @Override
    public void run() {
        try {
            if (source.isEmpty()) {
                logger.debug("source is empty");
                return;
            }
            long start = System.currentTimeMillis();
            label.setState(ExucutionTimeLabel.State.EXECUTING);
            
            final Map<File, Long> includedFiles = PlantUmlIncludes.commitIncludes(source, baseDir);
            logger.debug("includedFiles=", includedFiles);

            final RenderRequest renderRequest = new RenderRequest(baseDir, source, PlantUml.ImageFormat.PNG, page, zoom, version, renderUrlLinks);
            final RenderResult imageResult = PlantUmlRenderer.render(renderRequest, cachedItem);

            ImageItem[] imageItems = joinDiagrams(imageResult, cachedItem);
            final RenderCacheItem newItem = new RenderCacheItem(renderRequest, sourceFilePath, source, baseDir, zoom, page, includedFiles, imageResult, imageItems, version, total);
            if (hasImages(newItem.getImageItems())) {

                ApplicationManager.getApplication().invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        postRenderOnEDT(newItem);
                    }
                });

            } else {
                logger.debug("no images rendered");
            }
            long total = System.currentTimeMillis() - start;
            label.setState(ExucutionTimeLabel.State.DONE, total);
        } catch (RenderingCancelledException e) {
            logger.info("command interrupted");
            label.setState(ExucutionTimeLabel.State.CANCELLED);
        } catch (Exception e) {
            label.setState(ExucutionTimeLabel.State.ERROR);
            logger.error("Exception occurred rendering " + this, e);
        }
    }

    protected abstract void postRenderOnEDT(RenderCacheItem newItem);

    private boolean hasImages(ImageItem[] imagesWithUrlData) {
        for (ImageItem imageItem : imagesWithUrlData) {
            if (imageItem != null) {
                return true;
            }
        }
        return false;
    }

    private ImageItem[] joinDiagrams(@NotNull RenderResult renderResult, RenderCacheItem cachedItem) throws IOException {
        int pages = renderResult.getPages();

        //noinspection UndesirableClassUsage
        ImageItem[] imagesWithUrlData = new ImageItem[pages];
        if (cachedItem != null) {
            ImageItem[] imagesWithData = cachedItem.getImageItems();
            for (int i = 0; i < imagesWithData.length; i++) {
                if (pages > i) {
                    imagesWithUrlData[i] = imagesWithData[i];
                }
            }
        }

        List<ImageItem> imageImageItems = renderResult.getImageItems();
        for (int i = 0; i < imageImageItems.size(); i++) {
            ImageItem imageImageItem = imageImageItems.get(i);
            if (imageImageItem != null) {
                imagesWithUrlData[imageImageItem.getPage()] = imageImageItem;
            }
        }
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
