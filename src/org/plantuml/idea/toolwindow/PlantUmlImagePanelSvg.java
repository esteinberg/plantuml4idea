package org.plantuml.idea.toolwindow;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.ColoredSideBorder;
import com.intellij.ui.JBColor;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.components.JBLayeredPane;
import com.intellij.ui.components.Magnificator;
import com.intellij.util.ImageLoader;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.util.XMLResourceDescriptor;
import org.intellij.images.editor.ImageDocument;
import org.intellij.images.ui.ImageComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.action.context.*;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.rendering.ImageItem;
import org.plantuml.idea.rendering.RenderRequest;
import org.w3c.dom.Document;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * TODO CopyDiagramToClipboardContextAction not working
 */
public class PlantUmlImagePanelSvg extends JPanel implements Disposable {
    private static final AnAction[] AN_ACTIONS = {
            new SaveDiagramToFileContextAction(),
            new CopyDiagramToClipboardContextAction(),
            Separator.getInstance(),
            new CopyDiagramAsTxtToClipboardContextAction(),
            new CopyDiagramAsUnicodeTxtToClipboardContextAction(),
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

    private static Logger logger = Logger.getInstance(PlantUmlImagePanelSvg.class);
    private RenderRequest renderRequest;
    private ImageItem imageWithData;
    private Image originalImage;
    private ImageComponent imageComponent;

    public PlantUmlImagePanelSvg(ImageItem imageWithData, int i, RenderRequest renderRequest) {
        this.imageWithData = imageWithData;
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
        if (imageWithData.hasImage()) {
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
        originalImage = imageItem.getImage();
        Image scaledImage;

//        imageEditorUI = new ImageEditorUI(null);
//        add(imageEditorUI);
//        imageEditorUI.setImageProvider(new ImageDocument.CachedScaledImageProvider() {
//            private Double zoom;
//            private BufferedImage bufferedImage;
//
//            @Override
//            public BufferedImage apply(Double aDouble, Component component) {
//                try {
//                    if (!aDouble.equals(zoom)) {
//                        zoom = aDouble;
//                        bufferedImage = loadWithoutCache(null, new ByteArrayInputStream(imageItem.getImageBytes()), zoom, null);
//                        return bufferedImage;
//                    }
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                return bufferedImage;
//            }
//        }, null);


        imageComponent = new ImageComponent();
        imageComponent.setBackground(Color.WHITE);
        imageComponent.setOpaque(true);
        imageComponent.addMouseListener(new PopupHandler() {

            @Override
            public void invokePopup(Component comp, int x, int y) {
                ACTION_POPUP_MENU.getComponent().show(comp, x, y);
            }
        });

        ImageContainerPane comp = new ImageContainerPane(imageComponent);
        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        comp.setAlignmentY(Component.TOP_ALIGNMENT);
        add(comp);
        final ChangeListener changeListener = new DocumentChangeListener();
        imageComponent.getDocument().addChangeListener(changeListener);
        imageComponent.getDocument().setValue(imageItem.getImage());

        boolean showUrlLinksBorder = PlantUmlSettings.getInstance().isShowUrlLinksBorder();

        for (ImageItem.UrlData url : imageItem.getUrls()) {
            final URI uri = url.getUri();
            JLabel button = new JLabel();
            if (showUrlLinksBorder) {
                button.setBorder(new ColoredSideBorder(Color.RED, Color.RED, Color.RED, Color.RED, 1));
            }

            int tolerance = 5;
            Rectangle area = url.getClickArea();
            area = new Rectangle(area.x, area.y, area.width + tolerance, area.height + tolerance);
            Point location = area.getLocation();
            Dimension size = area.getSize();

            button.setLocation(location);
            button.setSize(size);

            button.setCursor(new Cursor(Cursor.HAND_CURSOR));

            //When user clicks on item, url is opened in default system browser
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        Desktop.getDesktop().browse(uri);
                    } catch (IOException ex) {
                        logger.warn(ex);
                    }
                }
            });


            imageComponent.add(button);
        }
    }

    public Image getOriginalImage() {
        return originalImage;
    }

    public static BufferedImage loadWithoutCache(@Nullable URL url, @NotNull InputStream stream, double scale, @Nullable ImageLoader.Dimension2DDouble docSize /*OUT*/) {
        try {
            MyTranscoder transcoder = MyTranscoder.createImage(scale, createTranscodeInput(url, stream));
            if (docSize != null) {
                docSize.setSize(transcoder.getOrigDocWidth(), transcoder.getOrigDocHeight());
            }
            return transcoder.getImage();
        } catch (Exception ex) {
            if (docSize != null) {
                docSize.setSize(0, 0);
            }
            throw new RuntimeException(ex);
        }
    }

    @NotNull
    private static TranscoderInput createTranscodeInput(@Nullable URL url, @NotNull InputStream stream) throws IOException {
        TranscoderInput myTranscoderInput;
        String uri = null;
        try {
            if (url != null && "jar".equals(url.getProtocol())) {
                // workaround for BATIK-1217
                url = new URL(url.getPath());
            }
            uri = url != null ? url.toURI().toString() : null;
        } catch (URISyntaxException ignore) {
        }

        Document document = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName()).createDocument(uri, stream);
//          patchColors(url, document);
        myTranscoderInput = new TranscoderInput(document);
        return myTranscoderInput;
    }

    @Override
    public void dispose() {
    }

    private class DocumentChangeListener implements ChangeListener {
        @Override
        public void stateChanged(@NotNull ChangeEvent e) {
            updateImageComponentSize();

            ImageDocument document = imageComponent.getDocument();
            BufferedImage value = document.getValue();

            //        CardLayout layout = (CardLayout)contentPanel.getLayout();
            //        layout.show(contentPanel, value != null ? IMAGE_PANEL : ERROR_PANEL);

            //        updateInfo();

            revalidate();
            repaint();
        }
    }

    private void updateImageComponentSize() {
        Rectangle bounds = imageComponent.getDocument().getBounds();
        if (bounds != null) {
            final double zoom = 1d;
            imageComponent.setCanvasSize((int) Math.ceil(bounds.width * zoom), (int) Math.ceil(bounds.height * zoom));
        }
    }

    private final class ImageContainerPane extends JBLayeredPane {
        private final ImageComponent imageComponent;

        ImageContainerPane(final ImageComponent imageComponent) {
            this.imageComponent = imageComponent;
            add(imageComponent);

            putClientProperty(Magnificator.CLIENT_PROPERTY_KEY, new Magnificator() {
                @Override
                public Point magnify(double scale, Point at) {
                    Point locationBefore = imageComponent.getLocation();
                    //              ImageZoomModel model = editor != null ? editor.getZoomModel() : getZoomModel();
                    //              double factor = model.getZoomFactor();
                    //              model.setZoomFactor(scale * factor);
                    return new Point(((int) ((at.x - Math.max(scale > 1.0 ? locationBefore.x : 0, 0)) * scale)),
                            ((int) ((at.y - Math.max(scale > 1.0 ? locationBefore.y : 0, 0)) * scale)));
                }
            });
        }

        private void centerComponents() {
            Rectangle bounds = getBounds();
            Point point = imageComponent.getLocation();
            // in embedded mode images should be left-side aligned
            boolean b = false;
            point.x = b ? 0 : (bounds.width - imageComponent.getWidth()) / 2;
            point.y = (bounds.height - imageComponent.getHeight()) / 2;
            imageComponent.setLocation(point);
        }

        @Override
        public void invalidate() {
//            centerComponents();
            super.invalidate();
        }

        @Override
        public Dimension getPreferredSize() {
            return imageComponent.getSize();
        }

        @Override
        public Dimension getMaximumSize() {
            return imageComponent.getSize();
//            return super.getMaximumSize();
        }
    }
}
