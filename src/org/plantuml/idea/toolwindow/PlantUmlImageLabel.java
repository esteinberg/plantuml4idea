package org.plantuml.idea.toolwindow;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.JBColor;
import com.intellij.ui.PopupHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.action.context.*;
import org.plantuml.idea.rendering.ImageItem;
import org.plantuml.idea.rendering.RenderRequest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
            setDiagram(imageWithData, this, 100);
        } else {
            setText("page not rendered, probably plugin error, please report it and try to hit reload");
        }
        this.renderRequest = renderRequest;
    }

    /**
     * Scales the image and sets it to label
     *
     * @param imageItem source image and url data
     * @param label            destination label
     * @param zoom             zoom factor
     */
    private static void setDiagram(@NotNull final ImageItem imageItem, final JLabel label, int zoom) {
        Image image = imageItem.getImage();
        int newWidth;
        int newHeight;
        Image scaledImage;


        if (zoom == 100) { // default zoom, no scaling
            newWidth = image.getWidth(label);
            newHeight = image.getHeight(label);
            scaledImage = image;
        } else {
            newWidth = Math.round(image.getWidth(label) * zoom / 100.0f);
            newHeight = Math.round(image.getHeight(label) * zoom / 100.0f);
            scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT);
        }

        label.setIcon(new ImageIcon(scaledImage));
        label.setPreferredSize(new Dimension(newWidth, newHeight));
        label.addMouseListener(new PopupHandler() {

            @Override
            public void invokePopup(Component comp, int x, int y) {
                ACTION_POPUP_MENU.getComponent().show(comp, x, y);
            }
        });

        //Removing all children from image label and creating transparent buttons for each item with url

        label.removeAll();

        for (ImageItem.UrlData url : imageItem.getUrls()) {
            final URI uri = url.getUri();
            JButton button = new JButton();
            button.setContentAreaFilled(false);
            button.setBorder(null);
            button.setLocation(url.getClickArea().getLocation());
            button.setSize(url.getClickArea().getSize());

            button.setCursor(new Cursor(Cursor.HAND_CURSOR));

            //When user clicks on item, url is opened in default system browser
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    try {
                        Desktop.getDesktop().browse(uri);
                    } catch (IOException e) {
                        logger.warn(e);
                    }
                }
            });
            label.add(button);
        }
    }
}
