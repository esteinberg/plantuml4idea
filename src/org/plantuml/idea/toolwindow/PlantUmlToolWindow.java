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
import org.plantuml.idea.plantuml.PlantUmlResult;
import org.plantuml.idea.util.ImageWithUrlData;
import org.plantuml.idea.util.LazyApplicationPoolExecutor;
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
                    VirtualFile selectedFile = UIUtils.getSelectedFile(project);
                    String sourceFilePath = selectedFile.getPath();
                    final String source = UIUtils.getSelectedSourceWithCaret(project);

                    if ("".equals(source)) { //is included file or some crap?
                        RenderCache.RenderCacheItem last = renderCache.displayedItem; //todo check all items for included file
                        if (last != null && last.isIncludedFile(selectedFile)) {
                            if (last.isIncludedFileChanged(selectedFile)) {
                                logger.debug("includes changed, executing command");
                                lazyExecutor.execute(getCommand(last.getSourceFilePath(), last.getSource(), last.getBaseDir(), page, zoom, null), delay);
                            } else if (last.renderRequired(project, source, page)) {
                                logger.debug("render required");
                                lazyExecutor.execute(getCommand(last.getSourceFilePath(), last.getSource(), last.getBaseDir(), page, zoom, last), delay);
                            } else {
                                logger.debug("include file, not changed");
                            }
                        } else {
                            logger.debug("empty source, not include file from last image");
                        }
                        return;
                    }

                    if (delay == LazyApplicationPoolExecutor.Delay.NOW) {
                        logger.debug("executing Delay.NOW");
                        final File selectedDir = UIUtils.getSelectedDir(project);
                        lazyExecutor.execute(getCommand(sourceFilePath, source, selectedDir, page, zoom, null), delay);
                        return;
                    }

                    RenderCache.RenderCacheItem cachedItem = renderCache.getCachedItem(sourceFilePath, source, page, zoom);
                    if (cachedItem == null || cachedItem.renderRequired(project, source, page)) {
                        logger.debug("render required");
                        final File selectedDir = UIUtils.getSelectedDir(project);
                        lazyExecutor.execute(getCommand(sourceFilePath, source, selectedDir, page, zoom, cachedItem), delay);
                    } else {
                        if (!renderCache.isDisplayed(cachedItem, page)) {
                            logger.debug("displaying cached item ", cachedItem);
                            cachedItem.setVersion(sequence.incrementAndGet());
                            displayDiagram(cachedItem);
                        } else {
                            logger.debug("item already displayed ", cachedItem);
                        }
                    }
                }
            }
        });
    }


    @NotNull
    protected RenderCommand getCommand(String selectedFile, final String source, final File baseDir, final int page, final int zoom, RenderCache.RenderCacheItem cachedItem) {
        logger.debug("#getCommand selectedFile='", selectedFile, "', baseDir=", baseDir, ", page=", page, ", zoom=", zoom);
        return new RenderCommand(selectedFile, source, baseDir, page, zoom, cachedItem, sequence.incrementAndGet()) {
            @NotNull
            @Override
            protected Runnable postRender(final String sourceFilePath, final PlantUmlResult imageResult, final ImageWithUrlData[] imagesWithData, final Map<File, Long> includedFiles) {
                return new Runnable() {

                    @Override
                    public void run() {
                        RenderCache.RenderCacheItem newItem = new RenderCache.RenderCacheItem(sourceFilePath, source, baseDir, zoom, page, includedFiles, imageResult, imagesWithData, imageResult.getRenderRequest().getVersion());
                        renderCache.addToCache(newItem);
                        displayDiagram(newItem);
                    }
                };
            }
        };
    }

    public void displayDiagram(RenderCache.RenderCacheItem cachedItem) {
        if (renderCache.isOlderRequest(cachedItem)) {
            logger.debug("skipping displaying older result", cachedItem);
            return;
        }
        logger.debug("displaying item ", cachedItem);
        renderCache.displayedItem = cachedItem;
        ImageWithUrlData[] imagesWithData = cachedItem.getImagesWithData();
        PlantUmlResult imageResult = cachedItem.getImageResult();
        imagesPanel.removeAll();
        if (this.page >= imageResult.getPages()) {
            this.page = -1;
            selectPageAction.setPage(page);
        }
        if (this.page == -1) {
            for (int i = 0; i < imagesWithData.length; i++) {
                displayImage(imageResult, i, imagesWithData[i]);
            }
        } else {
            displayImage(imageResult, page, imagesWithData[page]);
        }
        imagesPanel.revalidate();
        imagesPanel.repaint();
    }

    public void displayImage(PlantUmlResult imageResult, int i, ImageWithUrlData imageWithData) {
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
            this.page = page;
            selectPageAction.setPage(page);
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
        RenderCache.RenderCacheItem last = renderCache.displayedItem;
        if (last != null) {
            PlantUmlResult imageResult = last.getImageResult();
            if (imageResult != null) {
                pages = imageResult.getPages();
            }
        }
        return pages;
    }


    private boolean isProjectValid(Project project) {
        return project != null && !project.isDisposed();
    }


    public JPanel getImagesPanel() {
        return imagesPanel;
    }
}

