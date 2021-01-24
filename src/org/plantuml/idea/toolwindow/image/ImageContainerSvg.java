package org.plantuml.idea.toolwindow.image;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.scale.ScaleContext;
import com.intellij.ui.scale.ScaleType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.action.context.*;
import org.plantuml.idea.external.PlantUmlFacade;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.rendering.ImageItem;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderResult;
import org.plantuml.idea.toolwindow.Zoom;
import org.plantuml.idea.toolwindow.image.links.LinkNavigator;
import org.plantuml.idea.toolwindow.image.links.MyJLabel;
import org.plantuml.idea.toolwindow.image.links.MyMouseAdapter;
import org.plantuml.idea.toolwindow.image.svg.MyImageEditorImpl;
import org.plantuml.idea.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * TODO CopyDiagramToClipboardContextAction not working
 */
public class ImageContainerSvg extends JPanel implements ImageContainer {

    private static final AnAction[] AN_ACTIONS = {
            new SaveDiagramToFileContextAction(),
            new CopyDiagramToClipboardContextAction(),
            new CopySvgToClipboard(),
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
            new CopyPlantUmlServerLinkContextAction(),
    };
    public static final ActionPopupMenu ACTION_POPUP_MENU = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.UNKNOWN, new ActionGroup() {

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


    @Override
    public ImageItem getImageWithData() {
        return imageWithData;
    }

    @Override
    public int getPage() {
        return imageWithData.getPage();
    }

    @Override
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
        editor = imageItem.getEditor(project, renderRequest, renderResult);

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

        add(this.editor.getComponent());

    }

    public static void updateLinks(JComponent image, ScaleContext ctx, Double imageScale) {
        Component[] components = image.getComponents();
        for (Component component : components) {
            MyJLabel jLabel = (MyJLabel) component;
            Rectangle area = getRectangle(ctx, imageScale, jLabel.getLinkData());
            jLabel.updatePosition(area);
        }
    }

    public static void initLinks(Project project, @NotNull ImageItem imageItem, RenderRequest renderRequest, RenderResult renderResult, JComponent image, ScaleContext ctx, Double imageScale) {
        long start = System.currentTimeMillis();
        //probably not needed if initting on background
        //https://stackoverflow.com/a/28048290/685796
        image.setVisible(false);

        LinkNavigator navigator = new LinkNavigator(renderRequest, renderResult, project);
        boolean showUrlLinksBorder = PlantUmlSettings.getInstance().isShowUrlLinksBorder();

        image.removeAll();

        List<ImageItem.LinkData> links = imageItem.getLinks();
        for (int i = 0; i < links.size(); i++) {
            ImageItem.LinkData linkData = links.get(i);

            Rectangle area = getRectangle(ctx, imageScale, linkData);

            JLabel button = new MyJLabel(linkData, area, showUrlLinksBorder);

            //When user clicks on item, url is opened in default system browser
            button.addMouseListener(new MyMouseAdapter(navigator, linkData, renderRequest));

            image.add(button);
        }

        image.setVisible(true);
        LOG.debug("initLinks done in ", System.currentTimeMillis() - start, "ms");
    }

    @NotNull
    private static Rectangle getRectangle(ScaleContext ctx, Double imageScale, ImageItem.LinkData linkData) {
        Rectangle area = linkData.getClickArea();

        double tolerance = 1 * imageScale;
        double scale = ctx.getScale(ScaleType.SYS_SCALE) / imageScale;
        int x = (int) ((double) area.x / scale);
        int width = (int) ((area.width) / scale + 5 * tolerance);

        int y = (int) (area.y / scale + 3 * tolerance);
        int height = (int) ((area.height) / scale + tolerance);

        area = new Rectangle(x, y, width, height);
        return area;
    }


    public void setZoom(int unscaledZoom) {
        double d = unscaledZoom;
        editor.getZoomModel().setZoomFactor(d / 100);
        editor.getZoomModel().setZoomLevelChanged(true);
    }

    @Override
    public Image getPngImage() {
        //            double zoomFactor = editor.getZoomModel().getZoomFactor();
        //            InputStream inputStream = editor.getFile().getInputStream();
        //            Image load = SVGLoader.load(inputStream, (float) zoomFactor);
        //            ImageIO.write((BufferedImage)image, "png", new File(path + ".png"));
        //        
        //todo can't get it to transform to png

        RenderRequest rr = new RenderRequest(this.renderRequest, PlantUml.ImageFormat.PNG);
        Zoom zoom = UIUtils.getPlantUmlToolWindow(project).getZoom();
        rr.setZoom(zoom);
        RenderResult render = PlantUmlFacade.get().render(rr, null);
        BufferedImage image = render.getImageItem(getPage()).getImage(project, rr, render);
        return image;
    }

    @Override
    public void highlight(List<String> list) {
        Component[] components = this.editor.getContentComponent().getComponents();
        for (Component component : components) {
            MyJLabel jLabel = (MyJLabel) component;
            jLabel.highlight(list);
        }
    }

    @Override
    public @Nullable
    Object getData(@NotNull @NonNls String s) {
        if (CONTEXT_COMPONENT.is(s)) {
            return this;
        }
        return null;
    }

}
