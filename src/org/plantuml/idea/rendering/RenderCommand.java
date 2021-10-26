package org.plantuml.idea.rendering;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.external.PlantUmlFacade;
import org.plantuml.idea.plantuml.ImageFormat;
import org.plantuml.idea.preview.ExecutionStatusPanel;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;
import org.plantuml.idea.preview.Zoom;
import org.plantuml.idea.settings.PlantUmlSettings;

import javax.swing.*;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.plantuml.idea.util.Utils.logDuration;


public class RenderCommand {
    public static final Logger logger = Logger.getInstance(RenderCommand.class);

    private final Set<PlantUmlPreviewPanel> targets = new CopyOnWriteArraySet<>();
    private final Project project;
    protected Reason reason;
    protected String sourceFilePath;
    protected final String source;
    protected final int page;
    protected Zoom zoom;
    protected RenderCacheItem cachedItem;
    protected int version;
    protected LazyApplicationPoolExecutor.Delay delay;
    private ExecutionStatusPanel.State currentState = ExecutionStatusPanel.State.WAITING;
    protected long startAtNanos;
    protected RenderRequest renderRequest;
    protected volatile RenderResult result;
    protected static final int MILLION = 1000000;
    protected long start;
    protected volatile RenderCacheItem newRenderCacheItem;

    public long getRemainingDelayMillis() {
        return (startAtNanos - System.nanoTime()) / MILLION;
    }

    public Set<PlantUmlPreviewPanel> getTargets() {
        return targets;
    }

    public long getStartAtNanos() {
        return startAtNanos;
    }

    public RenderResult getResult() {
        return result;
    }

    public void addTargets(Set<PlantUmlPreviewPanel> newTargets) {
        this.targets.addAll(newTargets);
        for (PlantUmlPreviewPanel target : newTargets) {
            updateState(target, currentState);
        }
    }

    public void updateState(ExecutionStatusPanel.State currentState) {
        for (PlantUmlPreviewPanel target : targets) {
            updateState(target, currentState);
        }
    }

    public boolean containsTargets(Set<PlantUmlPreviewPanel> targets) {
        return this.targets.containsAll(targets);
    }


    public synchronized boolean addTargetsIfPossible_blocking(RenderCommand newCommand) {
        if (result == null) {
            logger.debug("adding targets ", newCommand.getTargets(), " to ", this);
            addTargets(newCommand.getTargets());
            return true;
        }
        return false;
    }


    public enum Reason {
        INCLUDES,
        FILE_SWITCHED,
        REFRESH,
        CARET,
        MANUAL_UPDATE, /* no function*/
        SOURCE_PAGE_ZOOM
    }

    public RenderCommand(Set<PlantUmlPreviewPanel> previewPanel, Project project, Reason reason, String sourceFilePath, String source, int page, Zoom zoom, RenderCacheItem cachedItem, int version, LazyApplicationPoolExecutor.Delay delay, PlantUmlSettings settings) {
        this.targets.addAll(previewPanel);
        this.project = project;
        this.reason = reason;
        this.sourceFilePath = sourceFilePath;
        this.source = source;
        this.page = page;
        this.zoom = zoom;
        this.cachedItem = cachedItem;
        this.version = version;
        this.delay = delay;
        if (this.delay == LazyApplicationPoolExecutor.Delay.NOW) {
            startAtNanos = 0;
        } else {
            startAtNanos = System.nanoTime() + ((long) settings.getRenderDelayAsInt() * MILLION);
        }
    }

    public void render() {
        try {
            if (source.isEmpty()) {
                logger.debug("source is empty");
                return;
            }
            if (targets.isEmpty()) {
                logger.debug("no targets");
                return;
            }

            start = System.currentTimeMillis();
            updateState(ExecutionStatusPanel.State.EXECUTING);

            if (result != null) {
                logger.debug("race condition?, already rendered, skipping");
                return;
            }


            PlantUmlSettings plantUmlSettings = PlantUmlSettings.getInstance();

            CompatibilityCheck.checkTransformer(plantUmlSettings);

            ImageFormat imageFormat = plantUmlSettings.isDisplaySvg() ? ImageFormat.SVG : ImageFormat.PNG;

            renderRequest = new RenderRequest(sourceFilePath, source, imageFormat, page, zoom, version, plantUmlSettings.isRenderLinks(), reason);
            renderRequest.disableSvgZoom();
            long s1 = System.currentTimeMillis();
            RenderResult render = PlantUmlFacade.get().render(renderRequest, cachedItem);
            setResult_blocking(render);
            newRenderCacheItem = new RenderCacheItem(renderRequest, result, page, version);

            logger.debug("render ", (System.currentTimeMillis() - s1), "ms");


        } catch (RenderingCancelledException e) {
            logger.info("command interrupted", e);
            updateState(ExecutionStatusPanel.State.CANCELLED);
        } catch (Throwable e) {
            updateState(ExecutionStatusPanel.State.ERROR);
            logger.error("Exception occurred rendering " + this, e);
        }
    }

    private synchronized void setResult_blocking(RenderResult renderResult) {
        result = renderResult;
    }

    public void displayResult() {
        if (result == null) {
            return;
        }
        try {
            long s2 = System.currentTimeMillis();

            targets.parallelStream().forEach(target -> {
                result.getImageItems().parallelStream().forEach(imageItem -> {
                    try {
                        imageItem.initImage(this.project, renderRequest, result, target);
                    } catch (Throwable e) {
                        logger.error(RenderCommand.this.toString(), e);
                    }
                });

                long totalTime = System.currentTimeMillis() - start;
                logger.debug("initImages done in ", (System.currentTimeMillis() - s2), "ms, for: ", target);


                SwingUtilities.invokeLater(logDuration("EDT displayResult", () -> {
                    String resultMessage = getResultMessage(totalTime);
                    target.displayResult(newRenderCacheItem, resultMessage);
                    updateCache(newRenderCacheItem);
                }));
            });

        } catch (Throwable e) {
            updateState(ExecutionStatusPanel.State.ERROR);
            logger.error("Exception occurred rendering " + this, e);
        }
    }

    @NotNull
    protected String getResultMessage(long totalTime) {
        return result.resultMessage(totalTime, version);
    }

    protected void updateCache(RenderCacheItem newItem) {
        if (newItem == cachedItem) {
            return;
        }

        if (newItem.hasImagesOrStacktrace()) {
            RenderCache renderCache = RenderCache.getInstance();
            if (reason == RenderCommand.Reason.REFRESH) {
                if (cachedItem != null) {
                    renderCache.removeFromCache(cachedItem);
                }
            }
            if (!newItem.getRenderResult().hasError()) {
                renderCache.addToCache(newItem);
            }
        }
    }

    private void updateState(PlantUmlPreviewPanel target, ExecutionStatusPanel.State state) {
        currentState = state;
        target.executionStatusPanel.update(version, currentState);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("hash", hashCode())
                .append("targets", targets)
                .append("reason", reason)
                .append("sourceFilePath", sourceFilePath)
                .append("page", page)
                .append("scaledZoom", zoom)
                .append("cachedItem", cachedItem)
                .append("version", version)
                .toString();
    }

    public boolean isSame(RenderCommand o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RenderCommand that = (RenderCommand) o;

        if (page != that.page) return false;
        if (project != null ? !project.equals(that.project) : that.project != null) return false;
        if (reason != that.reason) return false;
        if (sourceFilePath != null ? !sourceFilePath.equals(that.sourceFilePath) : that.sourceFilePath != null)
            return false;
        if (source != null ? !source.equals(that.source) : that.source != null) return false;
        if (zoom != null ? !zoom.equals(that.zoom) : that.zoom != null) return false;
        return true;
    }

    public static class DisplayExisting extends RenderCommand {
        private static final Logger logger = Logger.getInstance(RenderCommand.class);

        public DisplayExisting(Set<PlantUmlPreviewPanel> previewPanel, Project project, Reason reason, String sourceFilePath, String source, int page, Zoom zoom, RenderCacheItem cachedItem, int version, LazyApplicationPoolExecutor.Delay delay, PlantUmlSettings settings) {
            super(previewPanel, project, reason, sourceFilePath, source, page, zoom, cachedItem, version, delay, settings);
            renderRequest = cachedItem.getRenderRequest();
            result = cachedItem.getRenderResult();
            newRenderCacheItem = cachedItem;
        }

        public DisplayExisting(RenderCommand newCommand, RenderCommand oldCommand) {
            super(newCommand.getTargets(), newCommand.project, newCommand.reason, newCommand.sourceFilePath, newCommand.source, newCommand.page, newCommand.zoom, newCommand.cachedItem, newCommand.version, newCommand.delay, PlantUmlSettings.getInstance());
            this.result = oldCommand.getResult();
            this.renderRequest = oldCommand.renderRequest;
            this.newRenderCacheItem = oldCommand.newRenderCacheItem;
        }

        @Override
        protected String getResultMessage(long totalTime) {
            String message = "cached";
            if (logger.isDebugEnabled()) {
                message += totalTime + "ms";
                message += " id:" + version;
            }
            return message;
        }
    }
}
