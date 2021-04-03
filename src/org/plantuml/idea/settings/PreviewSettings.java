package org.plantuml.idea.settings;

import org.plantuml.idea.preview.editor.SplitFileEditor;

public class PreviewSettings {
    private boolean editorFirst = true;
    private SplitFileEditor.SplitEditorLayout splitEditorLayout = SplitFileEditor.SplitEditorLayout.SPLIT;

    public boolean isEditorFirst() {
        return editorFirst;
    }

    public void setEditorFirst(boolean editorFirst) {
        this.editorFirst = editorFirst;
    }

    public SplitFileEditor.SplitEditorLayout getSplitEditorLayout() {
        if (splitEditorLayout == null) {
            splitEditorLayout = SplitFileEditor.SplitEditorLayout.SPLIT;
        }
        return splitEditorLayout;
    }

    public void setSplitEditorLayout(SplitFileEditor.SplitEditorLayout splitEditorLayout) {
        this.splitEditorLayout = splitEditorLayout;
    }
}
