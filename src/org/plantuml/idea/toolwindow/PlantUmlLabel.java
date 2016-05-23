package org.plantuml.idea.toolwindow;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.JBColor;
import com.intellij.ui.PopupHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.action.*;
import org.plantuml.idea.rendering.ImageItem;
import org.plantuml.idea.rendering.RenderRequest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;

public class PlantUmlLabel extends JLabel {
    private static Logger logger = Logger.getInstance(PlantUmlLabel.class);
    private RenderRequest renderRequest;

    public PlantUmlLabel() {
    }

    public PlantUmlLabel(ImageItem imageWithData, int i, RenderRequest renderRequest) {
        setup(imageWithData, i, renderRequest);
    }

    public RenderRequest getRenderRequest() {
        return renderRequest;
    }

    public void setup(@NotNull ImageItem imageWithData, int i, RenderRequest renderRequest) {
        setOpaque(true);
        setBackground(JBColor.WHITE);
        if (imageWithData.getImage() != null) {
            setDiagram(imageWithData, this, 100);
        } else {
            setText("Failed to render page " + i);
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

        ImageIcon imageIcon = new ImageIcon(scaledImage);
        label.setIcon(imageIcon);
        label.setPreferredSize(new Dimension(newWidth, newHeight));
        label.addMouseListener(new PopupHandler() {

            @Override
            public void invokePopup(Component comp, int x, int y) {
                ActionManager.getInstance().createActionPopupMenu(ActionPlaces.UNKNOWN, new ActionGroup() {

                    @NotNull
                    @Override
                    public AnAction[] getChildren(@Nullable AnActionEvent e) {
                        return new AnAction[]{
                                new CopyDiagramToClipboardContextAction(),
                                Separator.getInstance(),
                                new CopyDiagramAsTxtToClipboardContextAction(),
                                new CopyDiagramAsUnicodeTxtToClipboardContextAction(),
                                Separator.getInstance(),
                                new ExternalOpenDiagramAsPNGAction(),
                                new ExternalOpenDiagramAsSVGAction()
                        };
                    }
                }).getComponent().show(comp, x, y);

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
