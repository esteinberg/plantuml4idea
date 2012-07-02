package org.plantuml.idea.util;

import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author Eugene Steinberg
 */
public class UIUtils {

    public static BufferedImage getBufferedImage(byte[] imageBytes) throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(imageBytes);
        return ImageIO.read(input);
    }

    public static void setImage(@NotNull BufferedImage image, JLabel label) {
        label.setIcon(new ImageIcon(image));
        label.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
    }

}
