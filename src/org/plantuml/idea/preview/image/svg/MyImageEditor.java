package org.plantuml.idea.preview.image.svg;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.images.editor.ImageDocument;
import org.intellij.images.ui.ImageComponentDecorator;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public interface MyImageEditor extends ImageComponentDecorator {
    @NotNull
    VirtualFile getFile();

    Project getProject();

    ImageDocument getDocument();

    JComponent getComponent();

    JComponent getContentComponent();

    boolean isValid();
}
