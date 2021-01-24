package org.plantuml.idea.toolwindow.image;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.ColoredSideBorder;
import com.intellij.ui.JBColor;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.scale.ScaleContext;
import com.intellij.ui.scale.ScaleType;
import org.intellij.images.ui.ImageComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.action.context.*;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.rendering.ImageItem;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderResult;
import org.plantuml.idea.toolwindow.image.links.LinkNavigator;
import org.plantuml.idea.toolwindow.image.links.MyMouseAdapter;
import org.plantuml.idea.toolwindow.image.svg.MyImageEditorImpl;
import org.plantuml.idea.toolwindow.image.svg.MyImageEditorUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * TODO CopyDiagramToClipboardContextAction not working
 */
public class ImageContainerSvg extends JPanel {

    private static final AnAction[] AN_ACTIONS = {
            new SaveDiagramToFileContextAction(),
            new CopyDiagramToClipboardContextAction(),
            Separator.getInstance(),
            new CopyDiagramAsTxtToClipboardContextAction(),
            new CopyDiagramAsUnicodeTxtToClipboardContextAction(),
            Separator.getInstance(),
            new CopyDiagramAsLatexToClipboardContextAction(),
            new CopyDiagramAsTikzCodeToClipboardContextAction(),
            Separator.getInstance(),
            new ExternalOpenDiagramAsPNGAction(),
            new ExternalOpenDiagramAsSVGAction(),
            Separator.getInstance(),
            new CopyPlantUmlServerLinkContextAction()
    };
    private static final ActionPopupMenu ACTION_POPUP_MENU = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.UNKNOWN, new ActionGroup() {

        @NotNull
        @Override
        public AnAction[] getChildren(@Nullable AnActionEvent e) {
            return AN_ACTIONS;
        }
    });

    private static Logger LOG = Logger.getInstance(ImageContainerSvg.class);

    private RenderRequest renderRequest;
    private final Project project;
    private ImageItem imageWithData;
    private final RenderResult renderResult;
    private MyImageEditorImpl editor;

    public ImageContainerSvg(Project project, ImageItem imageWithData, int i, RenderRequest renderRequest, RenderResult renderResult) {
        this.project = project;
        this.imageWithData = imageWithData;
        this.renderRequest = renderRequest;
        this.renderResult = renderResult;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setup(this.imageWithData, i, renderRequest);
    }

    public ImageItem getImageWithData() {
        return imageWithData;
    }

    public int getPage() {
        return imageWithData.getPage();
    }

    public RenderRequest getRenderRequest() {
        return renderRequest;
    }

    public void setup(@NotNull ImageItem imageWithData, int i, RenderRequest renderRequest) {
        setOpaque(true);
        setBackground(JBColor.WHITE);
        if (imageWithData.hasImageBytes()) {
            setDiagram(imageWithData);
        } else {
            add(new JLabel("page not rendered, probably plugin error, please report it and try to hit reload"));
        }
        this.renderRequest = renderRequest;
    }

    /**
     * Scales the image and sets it to label
     *
     * @param imageItem source image and url data
     */
    private void setDiagram(@NotNull final ImageItem imageItem) {
        long start = System.currentTimeMillis();
        LightVirtualFile virtualFile = new LightVirtualFile("svg image.svg", new String(imageItem.getImageBytes(), StandardCharsets.UTF_8));
        editor = new MyImageEditorImpl(project, virtualFile, true, this);
        ImageComponent imageComponent = editor.getComponent().getImageComponent();
        JComponent contentComponent = editor.getContentComponent();

        editor.setTransparencyChessboardVisible(PlantUmlSettings.getInstance().isShowChessboard());

        if (imageItem.hasError()) {
            imageComponent.setTransparencyChessboardWhiteColor(Color.BLACK);
            imageComponent.setTransparencyChessboardBlankColor(Color.BLACK);
        }

        contentComponent.addPropertyChangeListener(MyImageEditorUI.ZOOM_FACTOR_PROP, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                Double scale = (Double) propertyChangeEvent.getNewValue();

                initLinks(project, imageItem, renderRequest, renderResult, contentComponent, ScaleContext.create(editor.getComponent()), scale);
            }
        });

        contentComponent.addMouseListener(new PopupHandler() {

            @Override
            public void invokePopup(Component comp, int x, int y) {
                ACTION_POPUP_MENU.getComponent().show(comp, x, y);
            }
        });

        addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent hierarchyEvent) {
                Container changedParent = hierarchyEvent.getChangedParent();
                if (changedParent.isValid()) {
                    LOG.debug("disposing svg editor ", editor);
                    editor.dispose();
                }
            }
        });

        add(editor.getComponent());

        initLinks(project, imageItem, renderRequest, renderResult, contentComponent, ScaleContext.create(editor.getComponent()), 1.0);

        LOG.debug("setDiagram done in ", System.currentTimeMillis() - start, "ms");
    }


    public static void initLinks(Project project, @NotNull ImageItem imageItem, RenderRequest renderRequest, RenderResult renderResult, JComponent image, ScaleContext ctx, Double imageScale) {
        long start = System.currentTimeMillis();
        LinkNavigator navigator = new LinkNavigator(renderRequest, renderResult, project);
        boolean showUrlLinksBorder = PlantUmlSettings.getInstance().isShowUrlLinksBorder();

        image.removeAll();
        LOG.debug("initLinks A1 ", System.currentTimeMillis() - start, "ms");

        List<ImageItem.LinkData> links = imageItem.getLinks();
        for (int i = 0; i < links.size(); i++) {
            ImageItem.LinkData linkData = links.get(i);
            JLabel button = new JLabel();
            if (showUrlLinksBorder) {
                button.setBorder(new ColoredSideBorder(Color.RED, Color.RED, Color.RED, Color.RED, 1));
            }
            Rectangle area = linkData.getClickArea();

            double tolerance = 1 * imageScale;
            double scale = ctx.getScale(ScaleType.SYS_SCALE) / imageScale;
            int x = (int) ((double) area.x / scale);
            int width = (int) ((area.width) / scale) + (int) (5 * tolerance);

            int y = (int) (area.y / scale) + (int) (3 * tolerance);
            int height = (int) ((area.height) / scale) + (int) (2 * tolerance);

            area = new Rectangle(x, y, width, height);

            button.setLocation(area.getLocation());
            button.setSize(area.getSize());

            button.setCursor(new Cursor(Cursor.HAND_CURSOR));

            //When user clicks on item, url is opened in default system browser
            button.addMouseListener(new MyMouseAdapter(navigator, linkData, renderRequest));

            image.add(button);
        }
        LOG.debug("initLinks done in ", System.currentTimeMillis() - start, "ms");
    }


    public void setZoom(int unscaledZoom) {
        double d = unscaledZoom;
        editor.getZoomModel().setZoomFactor(d / 100);
        editor.getZoomModel().setZoomLevelChanged(true);
    }
}
