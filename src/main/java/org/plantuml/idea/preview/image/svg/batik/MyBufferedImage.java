package org.plantuml.idea.preview.image.svg.batik;

import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MyBufferedImage extends BufferedImage {
    private static final Logger LOG = Logger.getInstance(MyBufferedImage.class);

    private Color background;

    public MyBufferedImage(int width, int height, int imageType) {
        super(width, height, imageType);
    }

    /**
     * https://youtrack.jetbrains.com/issue/IDEA-250345
     */
    public void setStyle(String style) {
        try {
            if (style != null) {
                String color = StringUtils.substringBetween(style, "background:", ";");
                if (!StringUtils.isBlank(color)) {
                    background = Color.decode(color);
                }
            }
        } catch (Throwable e) {
            LOG.debug(style, e);
        }
    }

    public Color getBackground() {
        return background;
    }
}
