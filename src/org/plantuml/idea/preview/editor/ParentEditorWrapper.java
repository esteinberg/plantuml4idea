package org.plantuml.idea.preview.editor;

import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.preview.toolwindow.ToolWindowWrapper;

import javax.swing.*;

/**/
public class ParentEditorWrapper extends ToolWindowWrapper {
    private final PlantUmlPreviewEditor plantUmlPreviewEditor;

    public ParentEditorWrapper(PlantUmlPreviewEditor plantUmlPreviewEditor) {
        this.plantUmlPreviewEditor = plantUmlPreviewEditor;
    }

    @Override
    public boolean isVisible() {
        return plantUmlPreviewEditor != null && plantUmlPreviewEditor.isVisible();
    }

    @Nullable
    @Override
    public JComponent getComponent() {
        return plantUmlPreviewEditor.getComponent();
    }
}
