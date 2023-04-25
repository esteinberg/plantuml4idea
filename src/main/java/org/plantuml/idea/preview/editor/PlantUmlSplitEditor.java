package org.plantuml.idea.preview.editor;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.rendering.RenderCommand;

import static org.plantuml.idea.rendering.LazyApplicationPoolExecutor.Delay.NOW;

public class PlantUmlSplitEditor extends SplitFileEditor<TextEditor, PlantUmlPreviewEditor> implements TextEditor {
    public PlantUmlSplitEditor(@NotNull TextEditor mainEditor,
                               @NotNull PlantUmlPreviewEditor secondEditor) {
        super(mainEditor, secondEditor);
        putUserData(PlantUmlPreviewEditor.PLANTUML_PREVIEW_PANEL, secondEditor.getPlantUmlPreview());
    }

    @Override
    protected void adjustEditorsVisibility() {
        super.adjustEditorsVisibility();
        getSecondEditor().renderIfVisible(NOW, RenderCommand.Reason.FILE_SWITCHED);
    }

    @NotNull
    @Override
    public String getName() {
        return "PlantUML split editor";
    }

    @NotNull
    @Override
    public Editor getEditor() {
        return getMainEditor().getEditor();
    }

    @Override
    public boolean canNavigateTo(@NotNull Navigatable navigatable) {
        return getMainEditor().canNavigateTo(navigatable);
    }

    @Override
    public void navigateTo(@NotNull Navigatable navigatable) {
        getMainEditor().navigateTo(navigatable);
    }

}
