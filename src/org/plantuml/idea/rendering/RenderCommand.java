package org.plantuml.idea.rendering;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.plantuml.idea.external.PlantUmlFacade;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.toolwindow.ExecutionStatusPanel;

import java.util.List;


public abstract class RenderCommand implements Runnable {
    public static final Logger logger = Logger.getInstance(RenderCommand.class);

    protected Reason reason;
    protected String sourceFilePath;
    protected final String source;
    protected final int page;
    protected int scaledZoom;
    protected RenderCacheItem cachedItem;
    protected int version;
    protected boolean renderUrlLinks;
    protected LazyApplicationPoolExecutor.Delay delay;
    protected ExecutionStatusPanel label;

    public enum Reason {
        INCLUDES,
        FILE_SWITCHED,
        REFRESH,
        CARET,
        MANUAL_UPDATE, /* no function*/
        SOURCE_PAGE_ZOOM
    }

    public RenderCommand(Reason reason, String sourceFilePath, String source, int page, int scaledZoom, RenderCacheItem cachedItem, int version, boolean renderUrlLinks, LazyApplicationPoolExecutor.Delay delay, ExecutionStatusPanel label) {
        this.reason = reason;
        this.sourceFilePath = sourceFilePath;
        this.source = source;
        this.page = page;
        this.scaledZoom = scaledZoom;
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
            label.update(version, ExecutionStatusPanel.State.EXECUTING);


            PlantUml.ImageFormat imageFormat = PlantUml.ImageFormat.PNG;

            final RenderRequest renderRequest = new RenderRequest(sourceFilePath, source, imageFormat, page, scaledZoom, version, renderUrlLinks, reason);
            long s1 = System.currentTimeMillis();
            final RenderResult result = PlantUmlFacade.get().render(renderRequest, cachedItem);
            logger.debug("render ", (System.currentTimeMillis() - s1), "ms");

            long s2 = System.currentTimeMillis();
            initImages(result);
            logger.debug("initImages ", (System.currentTimeMillis() - s2), "ms");

            final RenderCacheItem newItem = new RenderCacheItem(renderRequest, result, page, version);
            final long total = System.currentTimeMillis() - start;

            if (!Thread.currentThread().isInterrupted() && hasImages(newItem.getImageItems())) {
                ApplicationManager.getApplication().invokeLater(() -> displayResultOnEDT(newItem, total, result));
            } else {
                logger.debug("no images rendered");
                label.update(version, ExecutionStatusPanel.State.DONE, total, result);
            }
        } catch (RenderingCancelledException e) {
            logger.info("command interrupted", e);
            label.update(version, ExecutionStatusPanel.State.CANCELLED);
        } catch (Throwable e) {
            label.update(version, ExecutionStatusPanel.State.ERROR);
            logger.error("Exception occurred rendering " + this, e);
        }
    }

    private void initImages(RenderResult result) {
        List<ImageItem> imageItems = result.getImageItems();
        for (ImageItem imageItem : imageItems) {
            imageItem.initImage();
        }
    }

    protected abstract void displayResultOnEDT(RenderCacheItem newItem, long total, RenderResult result);

    private boolean hasImages(ImageItem[] imageItems) {
        for (ImageItem imageItem : imageItems) {
            if (imageItem != null && imageItem.hasImage()) {
                return true;
            }
        }
        return false;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("reason", reason)
                .append("sourceFilePath", sourceFilePath)
                .append("page", page)
                .append("scaledZoom", scaledZoom)
                .append("cachedItem", cachedItem)
                .append("version", version)
                .toString();
    }
}
