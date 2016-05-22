package org.plantuml.idea.toolwindow;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.action.SelectPageAction;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.rendering.*;
import org.plantuml.idea.toolwindow.listener.PlantUmlAncestorListener;
import org.plantuml.idea.util.ImageWithUrlData;
import org.plantuml.idea.util.UIUtils;

import javax.swing.*;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Eugene Steinberg
 */
public class PlantUmlToolWindow extends JPanel implements Disposable {
    private static Logger logger = Logger.getInstance(PlantUmlToolWindow.class);

    private ToolWindow toolWindow;
    private JPanel imagesPanel;
    private JScrollPane scrollPane;

    private int zoom = 100;
    private int page = -1;

    private RenderCache renderCache = new RenderCache(10);

    private AncestorListener plantUmlAncestorListener;

    private final LazyApplicationPoolExecutor lazyExecutor;

    private SelectPageAction selectPageAction;
    private Project project;
    private AtomicInteger sequence = new AtomicInteger();

    public PlantUmlToolWindow(Project project, ToolWindow toolWindow) {
        super(new BorderLayout());
        this.project = project;
        this.toolWindow = toolWindow;
        PlantUmlSettings instance = PlantUmlSettings.getInstance();// Make sure settings are loaded and applied before we start rendering.

        setupUI();

        lazyExecutor = new LazyApplicationPoolExecutor(instance.getRenderDelayAsInt());
        plantUmlAncestorListener = new PlantUmlAncestorListener(this, project);
        //must be last
        this.toolWindow.getComponent().addAncestorListener(plantUmlAncestorListener);
    }

    private void setupUI() {
        ActionGroup group = (ActionGroup) ActionManager.getInstance().getAction("PlantUML.Toolbar");
        final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, group, true);
        actionToolbar.setTargetComponent(this);
        add(actionToolbar.getComponent(), BorderLayout.PAGE_START);

        imagesPanel = new JPanel();
        imagesPanel.setLayout(new BoxLayout(imagesPanel, BoxLayout.Y_AXIS));

        scrollPane = new JBScrollPane(imagesPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        add(scrollPane, BorderLayout.CENTER);

        addScrollBarListeners(imagesPanel);

        selectPageAction = (SelectPageAction) ActionManager.getInstance().getAction("PlantUML.SelectPage");
    }

    private void addScrollBarListeners(JComponent panel) {
        panel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    setZoom(Math.max(getZoom() - e.getWheelRotation() * 10, 1));
                } else {
                    scrollPane.dispatchEvent(e);
                }
            }
        });

        panel.addMouseMotionListener(new MouseMotionListener() {
            private int x, y;

            @Override
            public void mouseDragged(MouseEvent e) {
                JScrollBar h = scrollPane.getHorizontalScrollBar();
                JScrollBar v = scrollPane.getVerticalScrollBar();

                int dx = x - e.getXOnScreen();
                int dy = y - e.getYOnScreen();

                h.setValue(h.getValue() + dx);
                v.setValue(v.getValue() + dy);

                x = e.getXOnScreen();
                y = e.getYOnScreen();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                x = e.getXOnScreen();
                y = e.getYOnScreen();
            }
        });
    }

    @Override
    public void dispose() {
        logger.debug("dispose");
        toolWindow.getComponent().removeAncestorListener(plantUmlAncestorListener);
    }


    public void renderLater(final LazyApplicationPoolExecutor.Delay delay) {
        logger.debug("renderLater ", project.getName(), " ", delay);
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                if (isProjectValid(project)) {
                    final String source = UIUtils.getSelectedSourceWithCaret(project);

                    if ("".equals(source)) { //is included file or some crap?
                        logger.debug("empty source");
                        VirtualFile selectedFile = UIUtils.getSelectedFile(project);
                        RenderCacheItem last = renderCache.getDisplayedItem(); //todo check all items for included file

                        if (last != null && last.isIncludedFile(selectedFile)) {
                            logger.debug("include file selected");
                            if (last.isIncludedFileChanged(selectedFile)) {
                                logger.debug("includes changed, executing command");
                                lazyExecutor.execute(getCommand(last.getSourceFilePath(), last.getSource(), last.getBaseDir(), page, zoom, null, delay), delay);
                            } else if (last.renderRequired(project, last.getSource(), page)) {
                                logger.debug("render required");
                                lazyExecutor.execute(getCommand(last.getSourceFilePath(), last.getSource(), last.getBaseDir(), page, zoom, last, delay), delay);
                            } else if (!renderCache.isDisplayed(last, page)) {
                                logger.debug("displaying cached item ", last);
                                displayExistingDiagram(last);
                            } else {
                                logger.debug("include file, not changed");
                            }
                        } else if (last != null && !renderCache.isDisplayed(last, page)) {
                            logger.debug("empty source, not include file, displaying cached item ", last);
                             displayExistingDiagram(last);   
                        } else {
                            logger.debug("nothing needed");
                        }
                        return;
                    }

                    String sourceFilePath = UIUtils.getSelectedFile(project).getPath();

                    if (delay == LazyApplicationPoolExecutor.Delay.NOW) {
                        logger.debug("executing Delay.NOW");
                        final File selectedDir = UIUtils.getSelectedDir(project);
                        lazyExecutor.execute(getCommand(sourceFilePath, source, selectedDir, page, zoom, null, delay), delay);
                        return;
                    }

                    RenderCacheItem cachedItem = renderCache.getCachedItem(sourceFilePath, source, zoom);
                    if (cachedItem == null || cachedItem.renderRequired(project, source, page)) {
                        logger.debug("render required");
                        final File selectedDir = UIUtils.getSelectedDir(project);
                        lazyExecutor.execute(getCommand(sourceFilePath, source, selectedDir, page, zoom, cachedItem, delay), delay);
                    } else {
                        if (!renderCache.isDisplayed(cachedItem, page)) {
                            logger.debug("displaying cached item ", cachedItem);
                            displayExistingDiagram(cachedItem);
                        } else {
                            logger.debug("item already displayed ", cachedItem);
                        }
                    }
                }
            }
        });
    }

    public void displayExistingDiagram(RenderCacheItem last) {
        last.setVersion(sequence.incrementAndGet());
        last.setPage(page);
        displayDiagram(last);
    }


    @NotNull
    protected RenderCommand getCommand(String selectedFile, final String source, final File baseDir, final int page, final int zoom, RenderCacheItem cachedItem, LazyApplicationPoolExecutor.Delay delay) {
        logger.debug("#getCommand selectedFile='", selectedFile, "', baseDir=", baseDir, ", page=", page, ", zoom=", zoom);
        int version = sequence.incrementAndGet();
        return new MyRenderCommand(selectedFile, source, baseDir, page, zoom, cachedItem, version, delay);
    }

    private class MyRenderCommand extends RenderCommand {
        private final LazyApplicationPoolExecutor.Delay delay;

        public MyRenderCommand(String selectedFile, String source, File baseDir, int page, int zoom, RenderCacheItem cachedItem, int version, LazyApplicationPoolExecutor.Delay delay) {
            super(selectedFile, source, baseDir, page, zoom, cachedItem, version);
            this.delay = delay;
        }

        @Override
        public void postRenderOnEDT(final RenderResult imageResult, final ImageWithUrlData[] imagesWithData, final Map<File, Long> includedFiles) {
            if (delay == LazyApplicationPoolExecutor.Delay.NOW) {
                if (cachedItem != null) {
                    renderCache.removeFromCache(cachedItem);
                }
            }
            RenderCacheItem newItem = new RenderCacheItem(sourceFilePath, source, baseDir, zoom, page, includedFiles, imageResult, imagesWithData, version);
            renderCache.addToCache(newItem);
            displayDiagram(newItem);
        }
    }

    public void displayDiagram(RenderCacheItem cacheItem) {
        if (renderCache.isOlderRequest(cacheItem)) { //ctrl+z with cached image vs older request in progress
            logger.debug("skipping displaying older result", cacheItem);
            return;
        }
        logger.debug("displaying item ", cacheItem);
        renderCache.setDisplayedItem(cacheItem);

        ImageWithUrlData[] imagesWithData = cacheItem.getImagesWithData();
        RenderResult imageResult = cacheItem.getImageResult();

        imagesPanel.removeAll();
        if (this.page == -1) {
            for (int i = 0; i < imagesWithData.length; i++) {
                displayImage(cacheItem, imageResult, i, imagesWithData[i]);
            }
        } else {
            displayImage(cacheItem, imageResult, page, imagesWithData[page]);
        }
        imagesPanel.revalidate();
        imagesPanel.repaint();
    }

    public void displayImage(RenderCacheItem cacheItem, RenderResult imageResult, int i, ImageWithUrlData imageWithData) {
        if (imageWithData == null) {
            logger.error("trying to display null image. selectedPage=" + page + ", nullPage=" + i + ", cacheItem=" + cacheItem);
        }
        logger.debug("displaying image ", i);
        PlantUmlLabel label = new PlantUmlLabel(imageWithData, i, imageResult.getRenderRequest());
        addScrollBarListeners(label);

        if (i != 0) {
            imagesPanel.add(separator());
        }
        imagesPanel.add(label);
    }


    public void applyNewSettings(PlantUmlSettings plantUmlSettings) {
        lazyExecutor.setDelay(plantUmlSettings.getRenderDelayAsInt());
        renderCache.setMaxCacheSize(plantUmlSettings.getCacheSizeAsInt());
    }

    private JSeparator separator() {
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        Dimension size = new Dimension(separator.getPreferredSize().width, 10);
        separator.setVisible(true);
        separator.setMaximumSize(size);
        separator.setPreferredSize(size);
        return separator;
    }


    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
        renderLater(LazyApplicationPoolExecutor.Delay.POST_DELAY);
    }

    public void setPage(int page) {
        if (page >= -1 && page < getNumPages()) {
            logger.debug("page ", page, " selected");
            this.page = page;
            renderLater(LazyApplicationPoolExecutor.Delay.POST_DELAY);
        }
    }

    public void nextPage() {
        setPage(this.page + 1);
    }

    public void prevPage() {
        setPage(this.page - 1);
    }

    public int getNumPages() {
        int pages = -1;
        RenderCacheItem last = renderCache.getDisplayedItem();
        if (last != null) {
            RenderResult imageResult = last.getImageResult();
            if (imageResult != null) {
                pages = imageResult.getPages();
            }
        }
        return pages;
    }

    public int getPage() {
        return page;
    }

    private boolean isProjectValid(Project project) {
        return project != null && !project.isDisposed();
    }


    public JPanel getImagesPanel() {
        return imagesPanel;
    }


}

