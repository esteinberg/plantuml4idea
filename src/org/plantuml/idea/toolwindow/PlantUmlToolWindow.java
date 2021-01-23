package org.plantuml.idea.toolwindow;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.LowMemoryWatcher;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.action.NextPageAction;
import org.plantuml.idea.action.SelectPageAction;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.rendering.*;
import org.plantuml.idea.toolwindow.listener.PlantUmlAncestorListener;
import org.plantuml.idea.util.UIUtils;
import org.plantuml.idea.util.Utils;

import javax.swing.*;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.plantuml.idea.rendering.LazyApplicationPoolExecutor.Delay.RESET_DELAY;

/**
 * @author Eugene Steinberg
 */
public class PlantUmlToolWindow extends JPanel implements Disposable {
    private static Logger logger = Logger.getInstance(PlantUmlToolWindow.class);

    private ToolWindow toolWindow;
    private JPanel imagesPanel;
    private JScrollPane scrollPane;

    private Zoom zoom = new Zoom(this, 100);
    private int selectedPage = -1;

    private RenderCache renderCache;

    private AncestorListener plantUmlAncestorListener;

    private final LazyApplicationPoolExecutor lazyExecutor;

    private Project project;
    private AtomicInteger sequence = new AtomicInteger();
    public boolean renderUrlLinks;
    public ExecutionStatusPanel executionStatusPanel;
    private SelectedPagePersistentStateComponent selectedPagePersistentStateComponent;
    private FileEditorManager fileEditorManager;
    private FileDocumentManager fileDocumentManager;
    private VirtualFileManager fileManager;

    private int lastValidVerticalScrollValue;
    private int lastValidHorizontalScrollValue;
    private LocalFileSystem localFileSystem;

    public PlantUmlToolWindow(Project project, final ToolWindow toolWindow) {
        super(new BorderLayout());
        this.project = project;
        this.toolWindow = toolWindow;

        PlantUmlSettings settings = PlantUmlSettings.getInstance();// Make sure settings are loaded and applied before we start rendering.
        renderCache = new RenderCache(settings.getCacheSizeAsInt());
        selectedPagePersistentStateComponent = ServiceManager.getService(SelectedPagePersistentStateComponent.class);
        plantUmlAncestorListener = new PlantUmlAncestorListener(this, project);
        fileEditorManager = FileEditorManager.getInstance(project);
        fileDocumentManager = FileDocumentManager.getInstance();
        fileManager = VirtualFileManager.getInstance();
        localFileSystem = LocalFileSystem.getInstance();

        setupUI();
        lazyExecutor = new LazyApplicationPoolExecutor(settings.getRenderDelayAsInt(), executionStatusPanel);
        LowMemoryWatcher.register(new Runnable() {
            @Override
            public void run() {
                renderCache.clear();
                if (renderCache.getDisplayedItem() != null && !toolWindow.isVisible()) {
                    renderCache.setDisplayedItem(null);
                    imagesPanel.removeAll();
                    imagesPanel.add(new JLabel("Low memory detected, cache and images cleared. Go to PlantUML plugin settings and set lower cache size, or increase IDE heap size (-Xmx)."));
                    imagesPanel.revalidate();
                    imagesPanel.repaint();
                }
            }
        }, this);

        //must be last
        this.toolWindow.getComponent().addAncestorListener(plantUmlAncestorListener);

        applyNewSettings(PlantUmlSettings.getInstance());
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
        scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent adjustmentEvent) {
                if (!adjustmentEvent.getValueIsAdjusting()) {
                    RenderCacheItem displayedItem = getDisplayedItem();
                    if (displayedItem != null && !displayedItem.getRenderResult().hasError()) {
                        lastValidVerticalScrollValue = adjustmentEvent.getValue();
                    }
                }
            }
        });
        scrollPane.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent adjustmentEvent) {
                if (!adjustmentEvent.getValueIsAdjusting()) {
                    RenderCacheItem displayedItem = getDisplayedItem();
                    if (displayedItem != null && !displayedItem.getRenderResult().hasError()) {
                        lastValidHorizontalScrollValue = adjustmentEvent.getValue();
                    }
                }

            }
        });
        imagesPanel.add(new Usage("Usage:\n"));

        add(scrollPane, BorderLayout.CENTER);

        addScrollBarListeners(imagesPanel);
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
        executionStatusPanel = new ExecutionStatusPanel();
        newGroup.add(executionStatusPanel);
        return newGroup;
    }

    private void addScrollBarListeners(JComponent panel) {
        panel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    setUnscaledZoom(Math.max(zoom.getUnscaledZoom() - e.getWheelRotation() * 10, 1));
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
        logger.debug("renderLater ", project.getName(), " ", delay, " ", reason);
        ApplicationManager.getApplication().invokeLater(Utils.logDuration("EDT renderLater", () -> {
            if (isProjectValid(project)) {

                String source = UIUtils.getSelectedSourceWithCaret(fileEditorManager);
                String sourceFilePath = null;
                RenderCacheItem cachedItem = null;

                if ("".equals(source)) { //is included file or some crap?
                    logger.debug("empty source");
                    cachedItem = renderCache.getDisplayedItem();
                    if (cachedItem == null) {
                        logger.debug("no DisplayedItem, empty source, reason=", reason);
                        return;
                    }

                    source = cachedItem.getSource();
                    sourceFilePath = cachedItem.getSourceFilePath();
                } else {
                    VirtualFile selectedFile = UIUtils.getSelectedFile(fileEditorManager, fileDocumentManager);
                    if (selectedFile != null) {
                        sourceFilePath = selectedFile.getPath();
                    } else {
                        sourceFilePath = "DUMMY_NO_PATH";
                    }
                }

                selectedPage = selectedPagePersistentStateComponent.getPage(sourceFilePath);

                logger.debug("setting selected page from storage ", selectedPage);

                if (reason == RenderCommand.Reason.REFRESH) {
                    logger.debug("executing command, reason=", reason);
                    lazyExecutor.execute(getCommand(RenderCommand.Reason.REFRESH, sourceFilePath, source, selectedPage, zoom, null, delay));
                    return;
                }

                RenderCacheItem betterItem = renderCache.getCachedItem(sourceFilePath, source, selectedPage, zoom, fileDocumentManager, fileManager);
                logger.debug("cacheItem ", betterItem);
                if (betterItem != null) {
                    cachedItem = betterItem;
                }

                if (cachedItem == null) {
                    logger.debug("no cached item");
                    lazyExecutor.execute(getCommand(reason, sourceFilePath, source, selectedPage, zoom, null, delay));
                } else if (cachedItem.includedFilesChanged(fileDocumentManager, fileManager)) {
                    logger.debug("includedFilesChanged");
                    lazyExecutor.execute(getCommand(RenderCommand.Reason.INCLUDES, sourceFilePath, source, selectedPage, zoom, cachedItem, delay));
                } else if (cachedItem.imageMissingOrZoomChanged(selectedPage, zoom)) {
                    logger.debug("render required imageMissingOrZoomChanged");
                    lazyExecutor.execute(getCommand(RenderCommand.Reason.SOURCE_PAGE_ZOOM, sourceFilePath, source, selectedPage, zoom, cachedItem, delay));
                } else if (cachedItem.sourceChanged(source)) {
                    logger.debug("render required sourceChanged");
                    lazyExecutor.execute(getCommand(RenderCommand.Reason.SOURCE_PAGE_ZOOM, sourceFilePath, source, selectedPage, zoom, cachedItem, RESET_DELAY));
                } else if (!renderCache.isDisplayed(cachedItem, selectedPage)) {
                    logger.debug("render not required, displaying cached item ", cachedItem);
                    displayExistingDiagram(cachedItem);
                } else {
                    logger.debug("render not required, item already displayed ", cachedItem);
                    if (reason != RenderCommand.Reason.CARET) {
                        cachedItem.setVersion(sequence.incrementAndGet());
                        lazyExecutor.cancel();
                        executionStatusPanel.updateNow(cachedItem.getVersion(), ExecutionStatusPanel.State.DONE, "cached");
                    }
                }
            }
        }));
    }

    public void displayExistingDiagram(RenderCacheItem last) {
        last.setVersion(sequence.incrementAndGet());
        last.setRequestedPage(selectedPage);
        executionStatusPanel.updateNow(last.getVersion(), ExecutionStatusPanel.State.DONE, "cached");
        displayDiagram(last, false);
    }


    @NotNull
    protected RenderCommand getCommand(RenderCommand.Reason reason, String selectedFile, final String source, final int page, final Zoom zoom, RenderCacheItem cachedItem, LazyApplicationPoolExecutor.Delay delay) {
        logger.debug("#getCommand selectedFile='", selectedFile, "', page=", page, ", scaledZoom=", zoom);
        int version = sequence.incrementAndGet();

        return new MyRenderCommand(reason, selectedFile, source, page, zoom, cachedItem, version, delay, renderUrlLinks, executionStatusPanel);
    }

    private class MyRenderCommand extends RenderCommand {

        public MyRenderCommand(Reason reason, String selectedFile, String source, int page, Zoom zoom, RenderCacheItem cachedItem, int version, LazyApplicationPoolExecutor.Delay delay, boolean renderUrlLinks, ExecutionStatusPanel label) {
            super(reason, selectedFile, source, page, zoom, cachedItem, version, renderUrlLinks, delay, label);
        }

        @Override
        public void displayResultOnEDT(RenderCacheItem newItem, long total, RenderResult result) {
            if (reason == Reason.REFRESH) {
                if (cachedItem != null) {
                    renderCache.removeFromCache(cachedItem);
                }
            }
            if (!newItem.getRenderResult().hasError()) {
                renderCache.addToCache(newItem);
            }
            logger.debug("displaying item ", newItem);

            if (renderCache.getDisplayedItem() != null
                    && !renderCache.getDisplayedItem().getRenderResult().hasError()
                    && newItem.getRenderResult().hasError()
                    && PlantUmlSettings.getInstance().isDoNotDisplayErrors()) {
                executionStatusPanel.updateNow(newItem.getVersion(), ExecutionStatusPanel.State.ERROR, total, result, new Runnable() {

                    private RenderCacheItem oldDiagram;

                    @Override
                    public void run() {
                        final RenderCacheItem displayedItem = renderCache.getDisplayedItem();

                        if (oldDiagram != null && displayedItem != oldDiagram) {
                            displayDiagram(oldDiagram, true);
                            SwingUtilities.invokeLater(() -> oldDiagram = null);
                        } else if (displayedItem != newItem) {
                            displayDiagram(newItem, true);
                            SwingUtilities.invokeLater(() -> oldDiagram = displayedItem);

                        }
                    }
                });
            } else if (displayDiagram(newItem, false)) {
                executionStatusPanel.updateNow(newItem.getVersion(), ExecutionStatusPanel.State.DONE, total, result, new Runnable() {

                    private RenderCacheItem oldDiagram;
                    private boolean hasError = newItem.getRenderResult().hasError();

                    @Override
                    public void run() {
                        if (hasError) {
                            final RenderCacheItem displayedItem = renderCache.getDisplayedItem();
                            if (oldDiagram != null && displayedItem != oldDiagram) {
                                displayDiagram(oldDiagram, true);
                                SwingUtilities.invokeLater(() -> oldDiagram = null);
                            } else {
                                RenderCacheItem renderCacheItem = renderCache.getLast();
                                if (renderCacheItem != null && displayedItem != renderCacheItem) {
                                    displayDiagram(renderCacheItem, true);
                                    SwingUtilities.invokeLater(() -> oldDiagram = displayedItem);
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    public boolean displayDiagram(RenderCacheItem cacheItem, boolean force) {
        if (!force && renderCache.isOlderRequest(cacheItem)) { //ctrl+z with cached image vs older request in progress
            logger.debug("skipping displaying older result", cacheItem);
            return false;
        }


        //maybe track position per file?
        RenderCacheItem displayedItem = renderCache.getDisplayedItem();
        boolean restoreScrollPosition = displayedItem != null && displayedItem.getRenderResult().hasError() && renderCache.isSameFile(cacheItem);
        //must be before revalidate
        int lastValidVerticalScrollValue = this.lastValidVerticalScrollValue;
        int lastValidHorizontalScrollValue = this.lastValidHorizontalScrollValue;


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

        for (Component component : imagesPanel.getComponents()) {
            if (component instanceof Disposable) {
                Disposer.dispose((Disposable) component);
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

        //would be nice without a new event :(
        if (restoreScrollPosition) {
            //hope concurrency wont be an issue
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    scrollPane.getVerticalScrollBar().setValue(lastValidVerticalScrollValue);
                    scrollPane.getHorizontalScrollBar().setValue(lastValidHorizontalScrollValue);
                }
            });
        }


        return true;
    }

    public void displayImage(RenderCacheItem cacheItem, int pageNumber, ImageItem imageWithData) {
        if (imageWithData == null) {
            throw new RuntimeException("trying to display null image. selectedPage=" + selectedPage + ", nullPage=" + pageNumber + ", cacheItem=" + cacheItem);
        }
        JComponent component = null;
        if (cacheItem.getRenderRequest().getFormat() == PlantUml.ImageFormat.SVG) {
//            component = new PlantUmlImagePanelSvg(imageWithData, pageNumber, cacheItem.getRenderRequest());
        } else {
            component = new PlantUmlImageLabel(project, imagesPanel, imageWithData, pageNumber, cacheItem.getRenderRequest(), cacheItem.getRenderResult(), fileEditorManager, localFileSystem);
        }
        addScrollBarListeners(component);

        if (pageNumber != 0 && imagesPanel.getComponentCount() > 0) {
            imagesPanel.add(separator());
        }
        imagesPanel.add(component);
    }


    public void applyNewSettings(PlantUmlSettings plantUmlSettings) {
        lazyExecutor.setDelay(plantUmlSettings.getRenderDelayAsInt());
        renderCache.setMaxCacheSize(plantUmlSettings.getCacheSizeAsInt());
        renderUrlLinks = plantUmlSettings.isRenderLinks();
    }

    private JSeparator separator() {
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        Dimension size = new Dimension(separator.getPreferredSize().width, 10);
        separator.setVisible(true);
        separator.setMaximumSize(size);
        separator.setPreferredSize(size);
        return separator;
    }


    @NotNull
    public Zoom getZoom() {
        return zoom;
    }

    public void setUnscaledZoom(int unscaledZoom) {
        zoom = new Zoom(imagesPanel, unscaledZoom);
        renderLater(LazyApplicationPoolExecutor.Delay.NOW, RenderCommand.Reason.SOURCE_PAGE_ZOOM);
    }

    public void setSelectedPage(int selectedPage) {
        if (selectedPage >= -1 && selectedPage < getNumPages()) {
            logger.debug("page ", selectedPage, " selected");
            this.selectedPage = selectedPage;
            selectedPagePersistentStateComponent.setPage(selectedPage, renderCache.getDisplayedItem());
            renderLater(LazyApplicationPoolExecutor.Delay.NOW, RenderCommand.Reason.SOURCE_PAGE_ZOOM);
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

