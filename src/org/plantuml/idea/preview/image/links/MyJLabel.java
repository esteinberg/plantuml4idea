package org.plantuml.idea.preview.image.links;

import com.intellij.ui.ColoredSideBorder;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.rendering.ImageItem;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

public class MyJLabel extends JLabel {
    private static final ColoredSideBorder BORDER = new ColoredSideBorder(Color.RED, Color.RED, Color.RED, Color.RED, 1);
    private static final CompoundBorder HIGHLIGHT = getCompoundBorder();
                       
    private final ImageItem.LinkData linkData;
    private Rectangle area;
    private final boolean showUrlLinksBorder;
    private boolean highlighted;

    public MyJLabel(ImageItem.LinkData linkData, Rectangle area, boolean showUrlLinksBorder) {
        this.linkData = linkData;
        this.area = area;
        this.showUrlLinksBorder = showUrlLinksBorder;
        if (showUrlLinksBorder) {
            setBorder(BORDER);
        }
 
        setLocation(area.getLocation());
        setSize(area.getSize());

        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public ImageItem.LinkData getLinkData() {
        return linkData;
    }

    //todo dunno if it is faster to just add new jlabel
    public void highlight(List<String> list) {
        boolean contain = containsText(linkData.getText(), list);
        if (contain) {
            if (!highlighted) {
                highlight();
                highlighted = true;
            }
        } else {
            if (highlighted) {
                unhighlight();
                highlighted = false;
            }
        }
    }

    private void unhighlight() {
        setBorder(showUrlLinksBorder ? BORDER : null);
        setLocation(area.getLocation());
        setSize(area.getSize());
    }

    private void highlight() {
        setBorder(HIGHLIGHT);

        Rectangle rectangle = new Rectangle(area.x - 2, area.y - 2, area.width + 4, area.height + 4);
        setLocation(rectangle.getLocation());
        setSize(rectangle.getSize());
    }

    @NotNull
    private static CompoundBorder getCompoundBorder() {
        LineBorder lineBorder1 = new LineBorder(Color.BLACK, 1);
        LineBorder lineBorder2 = new LineBorder(Color.GREEN, 1);
        LineBorder lineBorder3 = new LineBorder(Color.BLACK, 1);
        CompoundBorder border = new CompoundBorder(lineBorder1, new CompoundBorder(lineBorder2, lineBorder3));
        return border;
    }

    private boolean containsText(String text, List<String> list) {
        for (String s : list) {
            if (s.length() == 0) {
                continue;
            }
            if (text.contains(s)) {
                return true;
            }
        }
        return false;
    }

    public void updatePosition(Rectangle area) {
        this.area = area;
        
        if (highlighted) {
            highlight();
        } else {
            unhighlight();
        } 
    }
}
