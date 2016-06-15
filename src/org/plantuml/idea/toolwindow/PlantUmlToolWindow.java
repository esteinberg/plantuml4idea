package org.plantuml.idea.toolwindow;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.action.NextPageAction;
import org.plantuml.idea.action.SelectPageAction;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.rendering.*;
import org.plantuml.idea.toolwindow.listener.PlantUmlAncestorListener;
import org.plantuml.idea.util.UIUtils;

import javax.swing.*;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
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
    private int selectedPage = -1;

    private RenderCache renderCache;

    private AncestorListener plantUmlAncestorListener;

    private final LazyApplicationPoolExecutor lazyExecutor;

    private SelectPageAction selectPageAction;
    private Project project;
    private AtomicInteger sequence = new AtomicInteger();
    public boolean renderUrlLinks;
    public ExucutionTimeLabel executionTimeLabel;
    private SelectedPagePersistentStateComponent selectedPagePersistentStateComponent;

    public PlantUmlToolWindow(Project project, ToolWindow toolWindow) {
        super(new BorderLayout());
        this.project = project;
        this.toolWindow = toolWindow;
        PlantUmlSettings settings = PlantUmlSettings.getInstance();// Make sure settings are loaded and applied before we start rendering.

        setupUI();
        lazyExecutor = new LazyApplicationPoolExecutor(settings.getRenderDelayAsInt(), executionTimeLabel);
        plantUmlAncestorListener = new PlantUmlAncestorListener(this, project);
        //must be last
        this.toolWindow.getComponent().addAncestorListener(plantUmlAncestorListener);
        renderCache = new RenderCache(settings.getCacheSizeAsInt());
        selectedPagePersistentStateComponent = ServiceManager.getService(SelectedPagePersistentStateComponent.class);
    }

    private void setupUI() {
        DefaultActionGroup newGroup = getActionGroup();
        final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, newGroup, true);
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

    @NotNull
    private DefaultActionGroup getActionGroup() {
        DefaultActionGroup group = (DefaultActionGroup) ActionManager.getInstance().getAction("PlantUML.Toolbar");
        DefaultActionGroup newGroup = new DefaultActionGroup();
        AnAction[] childActionsOrStubs = group.getChildActionsOrStubs();
        for (int i = 0; i < childActionsOrStubs.length; i++) {
            AnAction stub = childActionsOrStubs[i];
            newGroup.add(stub);
            if (stub instanceof ActionStub) {
                if (((ActionStub) stub).getClassName().equals(NextPageAction.class.getName())) {
                    newGroup.add(new SelectPageAction(this));
                }
            }
        }
        executionTimeLabel = new ExucutionTimeLabel();
        newGroup.add(executionTimeLabel);
        return newGroup;
    }

    private void addScrollBarListeners(JComponent panel) {
        panel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    setZoom(Math.max(getZoom() - e.getWheelRotation() * 10, 1));
                } else {
                    e.setSource(scrollPane);
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


    public void renderLater(final LazyApplicationPoolExecutor.Delay delay, final RenderCommand.Reason reason) {
        logger.debug("renderLater ", project.getName(), " ", delay);
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                if (isProjectValid(project)) {
                    final String source = UIUtils.getSelectedSourceWithCaret(project);

                    if ("".equals(source)) { //is included file or some crap?
                        logger.debug("empty source");
                        VirtualFile selectedFile = UIUtils.getSelectedFile(project);
                        RenderCacheItem last = renderCache.getDisplayedItem(); //todo check all items for included file?

//                        if (last != null && reason == RenderCommand.Reason.FILE_SWITCHED) {
//                            selectedPage = selectedPagePersistentStateComponent.getPage(last.getSourceFilePath());
//                            logger.debug("file switched, setting selected page ",selectedPage);
//                        }
                        
                        if (last != null && reason == RenderCommand.Reason.REFRESH) {
                            logger.debug("empty source, executing command, reason=", reason);
                            lazyExecutor.execute(getCommand(RenderCommand.Reason.REFRESH, last.getSourceFilePath(), last.getSource(), last.getBaseDir(), selectedPage, zoom, null, delay));
                        }

                        if (last != null && last.isIncludedFile(selectedFile)) {
                            logger.debug("include file selected");
                            if (last.isIncludedFileChanged(selectedFile)) {
                                logger.debug("includes changed, executing command");
                                lazyExecutor.execute(getCommand(RenderCommand.Reason.INCLUDES, last.getSourceFilePath(), last.getSource(), last.getBaseDir(), selectedPage, zoom, last, delay));
                            } else if (last.renderRequired(project, selectedPage, zoom)) {
                                logger.debug("render required");
                                lazyExecutor.execute(getCommand(RenderCommand.Reason.SOURCE_PAGE_ZOOM, last.getSourceFilePath(), last.getSource(), last.getBaseDir(), selectedPage, zoom, last, delay));
                            } else {
                                logger.debug("include file, not changed");
                            }
                        } else if (last != null && !renderCache.isDisplayed(last, selectedPage)) {
                            logger.debug("empty source, not include file, displaying cached item ", last);
                            displayExistingDiagram(last);
                        } else {
                            logger.debug("nothing needed");
                        }
                        return;
                    }

                    String sourceFilePath = UIUtils.getSelectedFile(project).getPath();


                    if (reason == RenderCommand.Reason.REFRESH) {
                        logger.debug("executing command, reason=", reason);
                        final File selectedDir = UIUtils.getSelectedDir(project);
                        lazyExecutor.execute(getCommand(RenderCommand.Reason.REFRESH, sourceFilePath, source, selectedDir, selectedPage, zoom, null, delay));
                        return;
                    }

                    if (reason == RenderCommand.Reason.FILE_SWITCHED) {
                        selectedPage = selectedPagePersistentStateComponent.getPage(sourceFilePath);
                        logger.debug("file switched, setting selected page ", selectedPage);
                    }
                    
                    RenderCacheItem cachedItem = renderCache.getCachedItem(project, sourceFilePath, source, selectedPage, zoom);

                    if (cachedItem == null || cachedItem.renderRequired(project, source, selectedPage)) {
                        logger.debug("render required");
                        final File selectedDir = UIUtils.getSelectedDir(project);
                        lazyExecutor.execute(getCommand(RenderCommand.Reason.SOURCE_PAGE_ZOOM, sourceFilePath, source, selectedDir, selectedPage, zoom, cachedItem, delay));
                    } else if (!renderCache.isDisplayed(cachedItem, selectedPage)) {
                        logger.debug("render not required, displaying cached item ", cachedItem);
                        displayExistingDiagram(cachedItem);
                    } else {
                        logger.debug("render not required, item already displayed ", cachedItem);
                    }
                }
            }
        });
    }

    public void displayExistingDiagram(RenderCacheItem last) {
        executionTimeLabel.state(ExucutionTimeLabel.State.DONE, "cached");
        last.setVersion(sequence.incrementAndGet());
        last.setRequestedPage(selectedPage);
        displayDiagram(last);
    }


    @NotNull
    protected RenderCommand getCommand(RenderCommand.Reason reason, String selectedFile, final String source, @Nullable final File baseDir, final int page, final int zoom, RenderCacheItem cachedItem, LazyApplicationPoolExecutor.Delay delay) {
        logger.debug("#getCommand selectedFile='", selectedFile, "', baseDir=", baseDir, ", page=", page, ", zoom=", zoom);
        int version = sequence.incrementAndGet();
        return new MyRenderCommand(reason, selectedFile, source, baseDir, page, zoom, cachedItem, version, delay, renderUrlLinks, executionTimeLabel);
    }

    private class MyRenderCommand extends RenderCommand {

        public MyRenderCommand(Reason reason, String selectedFile, String source, File baseDir, int page, int zoom, RenderCacheItem cachedItem, int version, LazyApplicationPoolExecutor.Delay delay, boolean renderUrlLinks, ExucutionTimeLabel label) {
            super(reason, selectedFile, source, baseDir, page, zoom, cachedItem, version, renderUrlLinks, delay, label);
        }

        @Override
        public void postRenderOnEDT(RenderCacheItem newItem) {
            if (reason == Reason.REFRESH) {
                if (cachedItem != null) {
                    renderCache.removeFromCache(cachedItem);
                }
            }
            if (!newItem.getRenderResult().hasError()) {
                renderCache.addToCache(newItem);
            }
            logger.debug("displaying item ", newItem);
            displayDiagram(newItem);
        }
    }

    public void displayDiagram(RenderCacheItem cacheItem) {
        if (renderCache.isOlderRequest(cacheItem)) { //ctrl+z with cached image vs older request in progress
            logger.debug("skipping displaying older result", cacheItem);
            return;
        }
        renderCache.setDisplayedItem(cacheItem);

        ImageItem[] imagesWithData = cacheItem.getImageItems();
        RenderResult imageResult = cacheItem.getRenderResult();
        int requestedPage = cacheItem.getRequestedPage();

        if (requestedPage >= imageResult.getPages()) {
            logger.debug("requestedPage >= imageResult.getPages()", requestedPage, ">=", imageResult.getPages());
            requestedPage = -1;
            if (!imageResult.hasError()) {
                logger.debug("toolWindow.page=", requestedPage, " (previously page=", selectedPage, ")");
                selectedPage = requestedPage;
            }
        }


        imagesPanel.removeAll();
        if (requestedPage == -1) {
            logger.debug("displaying images ", requestedPage);
            for (int i = 0; i < imagesWithData.length; i++) {
                displayImage(cacheItem, i, imagesWithData[i]);
            }
        } else {
            logger.debug("displaying image ", requestedPage);
            displayImage(cacheItem, requestedPage, imagesWithData[requestedPage]);
        }
        imagesPanel.revalidate();
        imagesPanel.repaint();
    }

    public void displayImage(RenderCacheItem cacheItem, int i, ImageItem imageWithData) {
        if (imageWithData == null) {
            throw new RuntimeException("trying to display null image. selectedPage=" + selectedPage + ", nullPage=" + i + ", cacheItem=" + cacheItem);
        }
        PlantUmlLabel label = new PlantUmlLabel(imageWithData, i, cacheItem.getRenderRequest());
        addScrollBarListeners(label);

        if (i != 0) {
            imagesPanel.add(separator());
        }
        imagesPanel.add(label);
    }


    public void applyNewSettings(PlantUmlSettings plantUmlSettings) {
        lazyExecutor.setDelay(plantUmlSettings.getRenderDelayAsInt());
        renderCache.setMaxCacheSize(plantUmlSettings.getCacheSizeAsInt());
        renderUrlLinks = plantUmlSettings.isRenderUrlLinks();
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
        renderLater(LazyApplicationPoolExecutor.Delay.POST_DELAY, RenderCommand.Reason.SOURCE_PAGE_ZOOM);
    }

    public void setSelectedPage(int selectedPage) {
        if (selectedPage >= -1 && selectedPage < getNumPages()) {
            logger.debug("page ", selectedPage, " selected");
            this.selectedPage = selectedPage;
            selectedPagePersistentStateComponent.setPage(selectedPage, renderCache.getDisplayedItem());
            renderLater(LazyApplicationPoolExecutor.Delay.POST_DELAY, RenderCommand.Reason.SOURCE_PAGE_ZOOM);
        }
    }

    public void nextPage() {
        setSelectedPage(this.selectedPage + 1);
    }

    public void prevPage() {
        setSelectedPage(this.selectedPage - 1);
    }

    public int getNumPages() {
        int pages = -1;
        RenderCacheItem last = renderCache.getDisplayedItem();
        if (last != null) {
            RenderResult imageResult = last.getRenderResult();
            if (imageResult != null) {
                pages = imageResult.getPages();
            }
        }
        return pages;
    }

    public int getSelectedPage() {
        return selectedPage;
    }

    public RenderCacheItem getDisplayedItem() {
        return renderCache.getDisplayedItem();
    }

    private boolean isProjectValid(Project project) {
        return project != null && !project.isDisposed();
    }


    public JPanel getImagesPanel() {
        return imagesPanel;
    }


}

