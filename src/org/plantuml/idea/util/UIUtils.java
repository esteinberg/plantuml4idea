package org.plantuml.idea.util;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.plantuml.PlantUml;

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

    /**
     * Scales the image and sets it to label
     *
     * @param image source image
     * @param label destination label
     * @param zoom  zoom factor
     */
    public static void setImage(@NotNull BufferedImage image, JLabel label, int zoom) {
        int newWidth;
        int newHeight;
        Image scaledImage;

        if (zoom == 100) { // default zoom, no scaling
            newWidth = image.getWidth();
            newHeight = image.getHeight();
            scaledImage = image;
        } else {
            newWidth = Math.round(image.getWidth() * zoom / 100.0f);
            newHeight = Math.round(image.getHeight() * zoom / 100.0f);
            scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT);
        }

        ImageIcon imageIcon = new ImageIcon(scaledImage);
        label.setIcon(imageIcon);
        label.setPreferredSize(new Dimension(newWidth, newHeight));
    }

    public static String getSelectedSourceWithCaret(Project myProject) {
        String source = "";
        source = getSelectedSource(myProject);

        Editor selectedTextEditor = FileEditorManager.getInstance(myProject).getSelectedTextEditor();
        if (selectedTextEditor != null) {
            final Document document = selectedTextEditor.getDocument();
            int offset = selectedTextEditor.getCaretModel().getOffset();
            source = PlantUml.extractSource(document.getText(), offset);
        }
        return source;
    }

    public static String getSelectedSource(Project myProject) {
        String source = "";
        Editor selectedTextEditor = FileEditorManager.getInstance(myProject).getSelectedTextEditor();
        if (selectedTextEditor != null) {
            final Document document = selectedTextEditor.getDocument();
            source = document.getText();
        }
        return source;
    }


    public static VirtualFile getSelectedFile(Project myProject) {
        Editor selectedTextEditor = FileEditorManager.getInstance(myProject).getSelectedTextEditor();
        VirtualFile file = null;
        if (selectedTextEditor != null) {
            final Document document = selectedTextEditor.getDocument();
            file = FileDocumentManager.getInstance().getFile(document);
        }
        return file;
    }


}
