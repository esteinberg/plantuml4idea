package org.plantuml.idea.components;

import javax.swing.*;
import java.awt.*;

/**
 * @author koroandr
 *         18.06.15
 */
public class TransparentButton extends JButton {
    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0));
        super.paint(g2);
        g2.dispose();
    }
}
