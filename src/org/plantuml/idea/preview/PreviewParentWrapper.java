package org.plantuml.idea.preview;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public interface PreviewParentWrapper {
    boolean isVisible();

    @NotNull
    JComponent getComponent();
}
