package org.plantuml.idea.preview.image;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.PopupHandler;
import com.intellij.util.Alarm;
import org.intellij.images.ui.ImageComponent;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.action.context.*;
import org.plantuml.idea.external.PlantUmlFacade;
import org.plantuml.idea.plantuml.ImageFormat;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;
import org.plantuml.idea.preview.Zoom;
import org.plantuml.idea.preview.image.links.LinkNavigator;
import org.plantuml.idea.preview.image.links.MyJLabel;
import org.plantuml.idea.preview.image.links.MyMouseAdapter;
import org.plantuml.idea.preview.image.svg.MyImageEditorImpl;
import org.plantuml.idea.preview.image.svg.MyImageEditorUI;
import org.plantuml.idea.rendering.ImageItem;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderResult;
import org.plantuml.idea.rendering.RenderingType;
import org.plantuml.idea.settings.PlantUmlSettings;
import org.plantuml.idea.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.charset.StandardCharsets;
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
    private final Alarm zoomAlarm;

    private RenderRequest renderRequest;
    private final Project project;
    private ImageItem imageWithData;
    private final RenderResult renderResult;
    @Nullable
    private MyImageEditorImpl editor;

    public ImageContainerSvg(PlantUmlPreviewPanel previewPanel, Project project, ImageItem imageWithData, int i, RenderRequest renderRequest, RenderResult renderResult) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.project = project;
        this.imageWithData = imageWithData;
        this.renderRequest = renderRequest;
        this.renderResult = renderResult;
        setAlignmentX(LEFT_ALIGNMENT);

        zoomAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, this);
        setup(previewPanel, this.imageWithData, i, renderRequest);
    }


    @Override
    public Dimension getPreferredSize() {
        if (editor != null) {
            ImageComponent imageComponent = this.editor.getComponent().getImageComponent();
            Dimension size = imageComponent.getSize();
            if (size.height > 0) {
                return size;
            }
        }
        return super.getPreferredSize();

    }

    @Override
    public Dimension getMaximumSize() {
        if (editor != null) {
            ImageComponent imageComponent = this.editor.getComponent().getImageComponent();
            Dimension size = imageComponent.getSize();
            if (size.height > 0) {
                return size;
            }
        }
        return super.getMaximumSize();
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

    public void setup(PlantUmlPreviewPanel previewPanel, @NotNull ImageItem imageItem, int i, RenderRequest renderRequest) {
        setOpaque(true);
        setBackground(JBColor.WHITE);
        if (imageItem.hasImageBytes()) {
            editor = imageItem.getEditor(previewPanel, project, this.renderRequest, renderResult);
            add(this.editor.getComponent());
        } else {
            add(new JLabel("page not rendered, probably plugin error, please report it and try to hit reload"));
        }
        this.renderRequest = renderRequest;
    }

    public static MyImageEditorImpl initEditor(PlantUmlPreviewPanel previewPanel, ImageItem imageItem, final Project project, final RenderRequest renderRequest, final RenderResult renderResult) {
        long start = System.currentTimeMillis();
        MyImageEditorImpl editor;
        String content = new String(imageItem.getImageBytes(), StandardCharsets.UTF_8);
        LightVirtualFile virtualFile = new LightVirtualFile("svg image.svg", content);
        editor = new MyImageEditorImpl(previewPanel, project, virtualFile, true, renderRequest.getZoom());
        ImageComponent imageComponent = editor.getComponent().getImageComponent();
        JComponent contentComponent = editor.getContentComponent();

        editor.setTransparencyChessboardVisible(PlantUmlSettings.getInstance().isShowChessboard());

//        if (imageItem.hasError()) {
//            imageComponent.setTransparencyChessboardWhiteColor(Color.BLACK);
//            imageComponent.setTransparencyChessboardBlankColor(Color.BLACK);
//        }

        contentComponent.addPropertyChangeListener(MyImageEditorUI.ZOOM_FACTOR_PROP, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                Double scale = (Double) propertyChangeEvent.getNewValue();
                Zoom zoom = new Zoom(contentComponent, (int) (scale * 100), PlantUmlSettings.getInstance());
                updateLinks(contentComponent, zoom);
            }
        });

        contentComponent.addMouseListener(new PopupHandler() {
            @Override
            public void invokePopup(Component comp, int x, int y) {
                ACTION_POPUP_MENU.getComponent().show(comp, x, y);
            }
        });

        initLinks(project, imageItem, renderRequest, renderResult, contentComponent);
        LOG.debug("initEditor done in ", System.currentTimeMillis() - start, "ms");

        return editor;
    }

    public static void updateLinks(JComponent image, Zoom zoom) {
        Component[] components = image.getComponents();
        for (Component component : components) {
            MyJLabel jLabel = (MyJLabel) component;
            Rectangle area = getRectangle(zoom, jLabel.getLinkData());
            jLabel.updatePosition(area);
        }
    }

    public static void initLinks(Project project, @NotNull ImageItem imageItem, RenderRequest renderRequest, RenderResult renderResult, JComponent image) {
        long start = System.currentTimeMillis();
        //probably not needed if initting on background
        //https://stackoverflow.com/a/28048290/685796
        image.setVisible(false);

        LinkNavigator navigator = new LinkNavigator(renderRequest, renderResult, project);
        boolean showUrlLinksBorder = PlantUmlSettings.getInstance().isShowUrlLinksBorder();
        Zoom zoom = renderRequest.getZoom();

        image.removeAll();

        List<ImageItem.LinkData> links = imageItem.getLinks();
        for (int i = 0; i < links.size(); i++) {
            ImageItem.LinkData linkData = links.get(i);

            Rectangle area = getRectangle(zoom, linkData);

            JLabel button = new MyJLabel(linkData, area, showUrlLinksBorder);

            //When user clicks on item, url is opened in default system browser
            button.addMouseListener(new MyMouseAdapter(navigator, linkData, renderRequest));

            image.add(button);
        }

        image.setVisible(true);
        LOG.debug("initLinks done in ", System.currentTimeMillis() - start, "ms");
    }

    @NotNull
    private static Rectangle getRectangle(@NotNull Zoom zoom, @NotNull ImageItem.LinkData linkData) {
        Rectangle area = linkData.getClickArea();
        Double imageScale = zoom.getDoubleScaledZoom();

        double tolerance = 1 * imageScale;
        double scale = zoom.getSystemScale() / imageScale;
        int x = (int) ((double) area.x / scale);
        int width = (int) ((area.width) / scale + 5 * tolerance);

        int y = (int) (area.y / scale + 3 * tolerance);
        int height = (int) ((area.height) / scale + tolerance);

        area = new Rectangle(x, y, width, height);
        return area;
    }


    public void setZoomOptimized(int unscaledZoom, Point point) {
        if (editor != null) {
            MyImageEditorUI.ImageZoomModelImpl zoomModel = (MyImageEditorUI.ImageZoomModelImpl) editor.getZoomModel();
            zoomModel.setZoomFactorOptimized((double) unscaledZoom / 100, point, zoomAlarm);
        }
    }

    @Override
    public Image getPngImage(AnActionEvent e) {
        //            double zoomFactor = editor.getZoomModel().getZoomFactor();
        //            InputStream inputStream = editor.getFile().getInputStream();
        //            Image load = SVGLoader.load(inputStream, (float) zoomFactor);
        //            ImageIO.write((BufferedImage)image, "png", new File(path + ".png"));
        //        
        //todo can't get it to transform to png
        if (this.renderResult.getStrategy() == RenderingType.REMOTE) {
            throw new RuntimeException("not implemented");
        }
        RenderRequest rr = new RenderRequest(this.renderRequest, ImageFormat.PNG);
        PlantUmlPreviewPanel previewPanel = UIUtils.getEditorOrToolWindowPreview(e);
        Zoom zoom = previewPanel.getZoom();
        rr.setZoom(zoom);
        RenderResult render = PlantUmlFacade.get().render(rr, null);
        BufferedImage image = render.getImageItem(getPage()).getImage(previewPanel, project, rr, render);
        return image;
    }

    @Override
    public void highlight(List<String> list) {
        if (this.editor != null) {
            Component[] components = this.editor.getContentComponent().getComponents();
            for (Component component : components) {
                MyJLabel jLabel = (MyJLabel) component;
                jLabel.highlight(list);
            }
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
