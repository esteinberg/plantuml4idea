package org.plantuml.idea.preview.image;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.scale.ScaleContext;
import com.intellij.util.ui.ImageUtil;
import com.intellij.util.ui.JBImageIcon;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.action.context.*;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;
import org.plantuml.idea.preview.Zoom;
import org.plantuml.idea.preview.image.links.LinkNavigator;
import org.plantuml.idea.preview.image.links.MyJLabel;
import org.plantuml.idea.preview.image.links.MyMouseAdapter;
import org.plantuml.idea.rendering.ImageItem;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderResult;
import org.plantuml.idea.settings.PlantUmlSettings;

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
    private static final ActionPopupMenu ACTION_POPUP_MENU = ActionManager.getInstance().createActionPopupMenu("plantuml4idea-ImageContainerPng", new ActionGroup() {

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

    /**
     * Method AboutDialog.$$$setupUI$$$() contains an invokespecial instruction referencing an unresolved constructor ImageContainerPng.<init>().
     */
    public ImageContainerPng() {
    }

    public ImageContainerPng(PlantUmlPreviewPanel previewPanel, Project project, JPanel parent, ImageItem imageWithData, int i, RenderRequest renderRequest, RenderResult renderResult) {
        init(previewPanel, project, parent, imageWithData, i, renderRequest, renderResult);
    }

    public void init(PlantUmlPreviewPanel previewPanel, Project project, JPanel parent, ImageItem imageWithData, int i, RenderRequest renderRequest, RenderResult renderResult) {
        this.imageWithData = imageWithData;
        this.project = project;
        this.renderResult = renderResult;
        this.renderRequest = renderRequest;
        setOpaque(true);
        setBackground(JBColor.WHITE);
        setup(previewPanel, parent, renderRequest);
    }

    private void setup(PlantUmlPreviewPanel previewPanel, JPanel parent, RenderRequest renderRequest) {
        if (project.isDisposed()) {
            return;
        }
        originalImage = this.imageWithData.getImage(previewPanel, this.project, renderRequest, this.renderResult);
        if (originalImage != null) {
            setDiagram(parent, this.imageWithData, renderRequest, this);
        } else {
            setText("page not rendered, probably plugin error, please report it and try to hit reload");
        }
    }

    @Override
    public ImageItem getImageItem() {
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

    private void setDiagram(JPanel parent, @NotNull final ImageItem imageItem, RenderRequest renderRequest, final JLabel label) {
        long start = System.currentTimeMillis();
        Image scaledImage;
        if (originalImage != null) {
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
            initLinks(project, imageItem, renderRequest, renderResult, label);

            LOG.debug("setDiagram done in ", System.currentTimeMillis() - start, "ms");
        } else {
            setText("page not rendered, probably plugin error, please report it and try to hit reload");
        }

    }

    public static void initLinks(Project project, @NotNull ImageItem imageItem, RenderRequest renderRequest, RenderResult renderResult, JComponent image) {
        if (project.isDisposed()) {
            return;
        }
        long start = System.currentTimeMillis();
        LinkNavigator navigator = new LinkNavigator(renderRequest, renderResult, project);
        boolean showUrlLinksBorder = PlantUmlSettings.getInstance().isShowUrlLinksBorder();
        Zoom zoom = renderRequest.getZoom();

        image.removeAll();

        for (ImageItem.LinkData linkData : imageItem.getLinks()) {

            Rectangle area = getRectangle(zoom, linkData);

            JLabel button = new MyJLabel(linkData, area, showUrlLinksBorder);

            //When user clicks on item, url is opened in default system browser
            button.addMouseListener(new MyMouseAdapter(navigator, linkData, renderRequest));

            image.add(button);
        }
        LOG.debug("initLinks done in ", System.currentTimeMillis() - start, "ms");
    }

    @NotNull
    private static Rectangle getRectangle(Zoom zoom, ImageItem.LinkData linkData) {
        Rectangle area = linkData.getClickArea();

        int tolerance = 1;
        double scale = zoom.getSystemScale();
        int x = (int) ((double) area.x / scale) - 2 * tolerance;
        int width = (int) ((area.width) / scale) + 4 * tolerance;

        int y = (int) (area.y / scale);
        int height = (int) ((area.height) / scale) + 5 * tolerance;

        area = new Rectangle(x, y, width, height);
        return area;
    }


    @Override
    public Image getPngImage(AnActionEvent e) {
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

    @Override
    public void dispose() {

    }
}
