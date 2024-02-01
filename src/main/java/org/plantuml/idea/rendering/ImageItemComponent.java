package org.plantuml.idea.rendering;

import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.preview.image.svg.MyImageEditorImpl;

import java.awt.image.BufferedImage;

public class ImageItemComponent {
    @Nullable
    public volatile BufferedImage image;
    public volatile MyImageEditorImpl editor;

    @Nullable
    public BufferedImage getImage() {
        return image;
    }

    public void setImage(@Nullable BufferedImage image) {
        this.image = image;
    }

    public MyImageEditorImpl getEditor() {
        return editor;
    }

    public void setEditor(MyImageEditorImpl editor) {
        this.editor = editor;
    }

    boolean isNull() {
        return editor == null && image == null;
    }

    public void dispose() {
        image = null;
        if (editor != null) {
            editor.dispose();
        }
    }
}
