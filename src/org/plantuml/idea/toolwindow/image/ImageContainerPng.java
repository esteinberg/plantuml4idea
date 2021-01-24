package org.plantuml.idea.toolwindow.image;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.scale.ScaleContext;
import com.intellij.ui.scale.ScaleType;
import com.intellij.util.ui.ImageUtil;
import com.intellij.util.ui.JBImageIcon;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.action.context.*;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.rendering.ImageItem;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderResult;
import org.plantuml.idea.toolwindow.image.links.LinkNavigator;
import org.plantuml.idea.toolwindow.image.links.MyJLabel;
import org.plantuml.idea.toolwindow.image.links.MyMouseAdapter;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ImageContainerPng extends JLabel implements ImageContainer {
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

    private static Logger LOG = Logger.getInstance(ImageContainerPng.class);
    private Project project;
    private RenderResult renderResult;
    private RenderRequest renderRequest;
    private ImageItem imageWithData;
    private Image originalImage;

    private FileEditorManager fileEditorManager;

    /**
     * Method AboutDialog.$$$setupUI$$$() contains an invokespecial instruction referencing an unresolved constructor ImageContainerPng.<init>().
     */
    public ImageContainerPng() {
    }

    public ImageContainerPng(Project project, JPanel parent, ImageItem imageWithData, int i, RenderRequest renderRequest, RenderResult renderResult) {
        this.imageWithData = imageWithData;
        this.project = project;
        this.renderResult = renderResult;
        setup(parent, this.imageWithData, i, renderRequest);
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

    public void setup(JPanel parent, @NotNull ImageItem imageWithData, int i, RenderRequest renderRequest) {
        setOpaque(true);
        setBackground(JBColor.WHITE);
        if (imageWithData.hasImageBytes()) {
            setDiagram(parent, imageWithData, renderRequest, this);
        } else {
            setText("page not rendered, probably plugin error, please report it and try to hit reload");
        }
        this.renderRequest = renderRequest;
    }

    private void setDiagram(JPanel parent, @NotNull final ImageItem imageItem, RenderRequest renderRequest, final JLabel label) {
        long start = System.currentTimeMillis();
        originalImage = imageItem.getImage(project, renderRequest, renderResult);
        Image scaledImage;

        ScaleContext ctx = ScaleContext.create(parent);
        scaledImage = ImageUtil.ensureHiDPI(originalImage, ctx);
//        scaledImage = ImageLoader.scaleImage(scaledImage, ctx.getScale(JBUI.ScaleType.SYS_SCALE));

        label.setIcon(new JBImageIcon(scaledImage));


        label.addMouseListener(new PopupHandler() {

            @Override
            public void invokePopup(Component comp, int x, int y) {
                ACTION_POPUP_MENU.getComponent().show(comp, x, y);
            }
        });

        //Removing all children from image label and creating transparent buttons for each item with url
        label.removeAll();
        initLinks(project, imageItem, renderRequest, renderResult, label, ctx);

        LOG.debug("setDiagram done in ", System.currentTimeMillis() - start, "ms");
    }

    public static void initLinks(Project project, @NotNull ImageItem imageItem, RenderRequest renderRequest, RenderResult renderResult, JComponent image, ScaleContext ctx) {
        long start = System.currentTimeMillis();
        LinkNavigator navigator = new LinkNavigator(renderRequest, renderResult, project);
        boolean showUrlLinksBorder = PlantUmlSettings.getInstance().isShowUrlLinksBorder();

        image.removeAll();

        for (ImageItem.LinkData linkData : imageItem.getLinks()) {

            Rectangle area = getRectangle(ctx, linkData);

            JLabel button = new MyJLabel(linkData, area, showUrlLinksBorder);

            //When user clicks on item, url is opened in default system browser
            button.addMouseListener(new MyMouseAdapter(navigator, linkData, renderRequest));

            image.add(button);
        }
        LOG.debug("initLinks done in ", System.currentTimeMillis() - start, "ms");
    }

    @NotNull
    private static Rectangle getRectangle(ScaleContext ctx, ImageItem.LinkData linkData) {
        Rectangle area = linkData.getClickArea();

        int tolerance = 1;
        double scale = ctx.getScale(ScaleType.SYS_SCALE);
        int x = (int) ((double) area.x / scale) - 2 * tolerance;
        int width = (int) ((area.width) / scale) + 4 * tolerance;

        int y = (int) (area.y / scale);
        int height = (int) ((area.height) / scale) + 5 * tolerance;

        area = new Rectangle(x, y, width, height);
        return area;
    }


    @Override
    public Image getPngImage() {
        return originalImage;
    }

    @Override
    public void highlight(List<String> list) {
        Component[] components = this.getComponents();
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
