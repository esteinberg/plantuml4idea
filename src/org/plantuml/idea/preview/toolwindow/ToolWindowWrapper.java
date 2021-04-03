package org.plantuml.idea.preview.toolwindow;

import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.preview.PreviewParentWrapper;

import javax.swing.*;

public class ToolWindowWrapper implements PreviewParentWrapper {
    private ToolWindow toolWindow;

    public ToolWindowWrapper(ToolWindow toolWindow) {
        this.toolWindow = toolWindow;
    }

    public ToolWindowWrapper() {
    }

    @Override
    public boolean isVisible() {
        return toolWindow.isVisible();
    }

    @Override
    @Nullable
    public JComponent getComponent() {
        return toolWindow.getComponent();
    }
}
