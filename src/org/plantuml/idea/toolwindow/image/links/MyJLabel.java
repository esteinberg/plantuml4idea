package org.plantuml.idea.toolwindow.image.links;

import org.plantuml.idea.rendering.ImageItem;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

public class MyJLabel extends JLabel {
    private final ImageItem.LinkData linkData;
    private final Rectangle area;
    private Border oldBorder;
    private boolean highlighted;

    public MyJLabel(ImageItem.LinkData linkData, Rectangle area) {
        this.linkData = linkData;
        this.area = area;
    }

    //todo dunno if it is faster to just add new jlabel
    public void highlight(List<String> list) {
        if (containsText(linkData.getText(), list)) {
            if (!highlighted) { //already highlighted
                highlighted = true;
                oldBorder = getBorder();
                LineBorder lineBorder1 = new LineBorder(Color.BLACK, 1);
                LineBorder lineBorder2 = new LineBorder(Color.GREEN, 1);
                LineBorder lineBorder3 = new LineBorder(Color.BLACK, 1);
                CompoundBorder border = new CompoundBorder(lineBorder1, new CompoundBorder(lineBorder2, lineBorder3));
                setBorder(border);

                Rectangle rectangle = new Rectangle(area.x - 2, area.y - 2, area.width + 4, area.height + 4);
                setLocation(rectangle.getLocation());
                setSize(rectangle.getSize());
            }
        } else {
            highlighted = false;
            if (oldBorder != null) {
                setBorder(oldBorder);
                oldBorder = null;
                setLocation(area.getLocation());
                setSize(area.getSize());
            }
        }
    }

    private boolean containsText(String text, List<String> list) {
        for (String s : list) {
            if (text.contains(s)) {
                return true;
            }
        }
        return false;
    }

}
