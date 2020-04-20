package org.plantuml.idea.toolwindow;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.ColoredSideBorder;
import com.intellij.ui.JBColor;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.scale.ScaleContext;
import com.intellij.ui.scale.ScaleType;
import com.intellij.util.ui.ImageUtil;
import com.intellij.util.ui.JBImageIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.action.context.*;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.rendering.ImageItem;
import org.plantuml.idea.rendering.RenderRequest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;

public class PlantUmlImageLabel extends JLabel {
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

    private static Logger logger = Logger.getInstance(PlantUmlImageLabel.class);
    private RenderRequest renderRequest;
    private ImageItem imageWithData;
    private Image originalImage;

    public PlantUmlImageLabel() {
    }

    public PlantUmlImageLabel(ImageItem imageWithData, int i, RenderRequest renderRequest) {
        this.imageWithData = imageWithData;
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
            setDiagram(imageWithData, this);
        } else {
            setText("page not rendered, probably plugin error, please report it and try to hit reload");
        }
        this.renderRequest = renderRequest;
    }

    /**
     * Scales the image and sets it to label
     *
     * @param imageItem source image and url data
     * @param label     destination label
     */
    private void setDiagram(@NotNull final ImageItem imageItem, final JLabel label) {
        originalImage = imageItem.getImage();
        Image scaledImage;

        ScaleContext ctx = ScaleContext.create(label);
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
        boolean showUrlLinksBorder = PlantUmlSettings.getInstance().isShowUrlLinksBorder();

        for (ImageItem.UrlData url : imageItem.getUrls()) {
            final URI uri = url.getUri();
            JLabel button = new JLabel();
            if (showUrlLinksBorder) {
                button.setBorder(new ColoredSideBorder(Color.RED, Color.RED, Color.RED, Color.RED, 1));
            }
            Rectangle area = url.getClickArea();

            int tolerance = 5;
            double scale = ctx.getScale(ScaleType.SYS_SCALE);
            int x = (int) ((double) area.x / scale);
            int y = (int) (area.y / scale);
            int width = (int) ((area.width) / scale) + tolerance;
            int height = (int) ((area.height) / scale) + tolerance;

            area = new Rectangle(x, y, width, height);

            button.setLocation(area.getLocation());
            button.setSize(area.getSize());

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


            label.add(button);
        }
    }

    public Image getOriginalImage() {
        return originalImage;
    }
}
