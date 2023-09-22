package org.plantuml.idea.preview.editor;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.util.Utils;

public class PlantUmlSplitEditorProvider implements FileEditorProvider, DumbAware {
    public PlantUmlSplitEditorProvider() {
    }

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return Utils.isPlantUmlFileType(project, file);
    }

    //    @Override
    public boolean acceptRequiresReadAction() {
        return false;
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        TextEditor editor = (TextEditor) TextEditorProvider.getInstance().createEditor(project, file);
        PlantUmlPreviewEditor umlPreviewEditor = new PlantUmlPreviewEditor(file, project);
        umlPreviewEditor.setEditor(editor.getEditor());
        return new PlantUmlSplitEditor(editor, umlPreviewEditor);
    }

    @NotNull
    @Override
    public String getEditorTypeId() {
        return "plantuml4idea-split-editor";
    }

    @NotNull
    @Override
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }


}
