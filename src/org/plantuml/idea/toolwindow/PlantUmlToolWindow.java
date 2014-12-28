package org.plantuml.idea.toolwindow;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.AncestorListenerAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import org.plantuml.idea.action.SelectPageAction;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.plantuml.PlantUmlResult;
import org.plantuml.idea.util.LazyApplicationPoolExecutor;
import org.plantuml.idea.util.UIUtils;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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
    private int numPages = 1;

    private String cachedSource = "";
    private int cachedPage = page;
    private int cachedZoom = zoom;

    private AncestorListener plantUmlAncestorListener = new PlantUmlAncestorListener();

    private LazyApplicationPoolExecutor lazyExecutor = new LazyApplicationPoolExecutor();

    private SelectPageAction selectPageAction;
    private Project project;

    public PlantUmlToolWindow(Project project, ToolWindow toolWindow) {
        super(new BorderLayout());
        this.project = project;
        this.toolWindow = toolWindow;

        setupUI();

        this.toolWindow.getComponent().addAncestorListener(plantUmlAncestorListener);
    }

    private void setupUI() {
        ActionGroup group = (ActionGroup) ActionManager.getInstance().getAction("PlantUML.Toolbar");
        final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, group, true);
        actionToolbar.setTargetComponent(this);
        add(actionToolbar.getComponent(), BorderLayout.PAGE_START);

        imagesPanel = new JPanel();

        scrollPane = new JBScrollPane(imagesPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        add(scrollPane, BorderLayout.CENTER);
        
        addScrollBarListeners(imagesPanel);

        selectPageAction = (SelectPageAction) ActionManager.getInstance().getAction("PlantUML.SelectPage");
    }

    private void addScrollBarListeners(JComponent panel) {
        panel.addMouseWheelListener(new MouseWheelListener() {
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

    private boolean renderRequired(String newSource) {
        if (newSource.isEmpty())
            return false;
        if (!newSource.equals(cachedSource) || page != cachedPage || zoom != cachedZoom) {
            cachedSource = newSource;
            cachedPage = page;
            cachedZoom = zoom;
            return true;
        }
        return false;
    }

    public void renderLater() {
        logger.debug("renderLater ", project.getName());
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                if (!isProjectValid(project))
                    return;
                final String source = UIUtils.getSelectedSourceWithCaret(project);
                if (!renderRequired(source))
                    return;
                final File selectedDir = UIUtils.getSelectedDir(project);
                lazyExecutor.execute(
                        new Runnable() {
                            @Override
                            public void run() {
                                renderWithBaseDir(source, selectedDir, page);
                            }
                        }
                );
            }
        });
    }

    private void renderWithBaseDir(String source, File baseDir, int pageNum) {
        if (source.isEmpty()) {
            return;
        }

        try {
            PlantUmlResult result = PlantUml.render(source, baseDir, pageNum, zoom);
            final BufferedImage[] images = toBufferedImages(result);

            if (hasImages(images)) {
                ApplicationManager.getApplication().invokeLater(new Runnable() {

                    public void run() {
                        imagesPanel.removeAll();
                        for (int i = 0; i < images.length; i++) {
                            BufferedImage image = images[i];
                            JLabel label = new JLabel();
                            label.setOpaque(true);
                            label.setBackground(JBColor.WHITE);
                            addScrollBarListeners(label);
                            if (image != null) {
                                UIUtils.setImage(image, label, 100);
                            } else {
                                label.setText("Failed to render page " + i);
                            }
                            if (i != 0) {
                                imagesPanel.add(separator());
                            }
                            imagesPanel.add(label);
                        }
                        imagesPanel.revalidate();
                        imagesPanel.repaint();
                    }
                });

            }
            if (!result.isError()) {
                setNumPages(result.getPages());
            }
        } catch (Exception e) {
            logger.warn("Exception occurred rendering source = " + source + ": " + e, e);
        }
    }

    private JSeparator separator() {
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        Dimension size = new Dimension(separator.getPreferredSize().width, 10);
        separator.setVisible(true);
        separator.setMaximumSize(size);
        separator.setPreferredSize(size);
        return separator;
    }

    private boolean hasImages(BufferedImage[] images) {
        for (int i = 0; i < images.length; i++) {
            BufferedImage image = images[i];
            if (image != null) {
                return true;
            }
        }
        return false;
    }

    private BufferedImage[] toBufferedImages(PlantUmlResult result) throws IOException {
        PlantUmlResult.Diagram[] diagrams = result.getDiagrams();
        //noinspection UndesirableClassUsage
        final BufferedImage[] images = new BufferedImage[diagrams.length];
        for (int i = 0; i < diagrams.length; i++) {
            PlantUmlResult.Diagram diagram = diagrams[i];
            if (diagram != null) {
                images[i] = UIUtils.getBufferedImage(diagram.getDiagramBytes());
            }
        }
        return images;
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
        renderLater();
    }

    public void setPage(int page) {
        if (page >= -1 && page < numPages) {
            this.page = page;
            selectPageAction.setPage(page);
            renderLater();
        }
    }

    public void nextPage() {
        setPage(this.page + 1);
    }

    public void prevPage() {
        setPage(this.page - 1);
    }

    public int getNumPages() {
        return numPages;
    }

    public void setNumPages(int numPages) {
        this.numPages = numPages;
        if (page >= numPages)
            setPage(numPages - 1);
        selectPageAction.setNumPages(numPages);
    }

    private boolean isProjectValid(Project project) {
        return project != null && !project.isDisposed();
    }


    public int getPage() {
        return page;
    }

    class PlantUmlAncestorListener extends AncestorListenerAdapter {
        private Logger logger = Logger.getInstance(PlantUmlAncestorListener.class);

        @Override
        public void ancestorAdded(AncestorEvent ancestorEvent) {
            logger.debug("ancestorAdded ", project.getName());
            renderLater();
        }

    }
}

