package org.plantuml.idea.preview.editor;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;
import org.plantuml.idea.rendering.LazyApplicationPoolExecutor;
import org.plantuml.idea.rendering.RenderCommand;

import javax.swing.*;
import java.beans.PropertyChangeListener;

import static org.plantuml.idea.rendering.LazyApplicationPoolExecutor.Delay.NOW;
import static org.plantuml.idea.rendering.LazyApplicationPoolExecutor.Delay.RESET_DELAY;

public class PlantUmlPreviewEditor extends UserDataHolderBase implements FileEditor {

    public static final Key<PlantUmlPreviewEditor> PLANTUML_PREVIEW = new Key<>("PLANTUML_PREVIEW_EDITOR");
    public static final Key<PlantUmlPreviewPanel> PLANTUML_PREVIEW_PANEL = new Key<>("PLANTUML_PREVIEW_PANEL");
    private final Logger log = Logger.getInstance(PlantUmlPreviewEditor.class);

    private final Document document;
    private final VirtualFile file;
    private final Project project;

    private PlantUmlPreviewPanel plantUmlPreview;
    private Editor editor;

    void renderIfVisible(LazyApplicationPoolExecutor.Delay delay, RenderCommand.Reason reason) {
        plantUmlPreview.processRequest(delay, reason);
    }

    public PlantUmlPreviewPanel getPlantUmlPreview() {
        return plantUmlPreview;
    }

    public PlantUmlPreviewEditor(final Document document, VirtualFile file, Project project, boolean documentListener) {
        this.document = document;
        this.file = file;
        this.project = project;
        plantUmlPreview = new PlantUmlPreviewPanel(project, this);
        putUserData(PLANTUML_PREVIEW_PANEL, plantUmlPreview);
        renderIfVisible(NOW, RenderCommand.Reason.FILE_SWITCHED);
        if (documentListener) {
            // Listen to the document modifications.
            this.document.addDocumentListener(new DocumentListener() {
                @Override
                public void documentChanged(@NotNull DocumentEvent e) {
                    renderIfVisible(RESET_DELAY, RenderCommand.Reason.SOURCE_PAGE_ZOOM);
                }
            }, this);

        }
    }

    public void setEditor(Editor editor) {
        this.editor = editor;
        editor.putUserData(PLANTUML_PREVIEW, this);
    }

    public Editor getEditor() {
        return editor;
    }

    @Override
    public @Nullable
    VirtualFile getFile() {
        return file;
    }

    @Override
    @Nullable
    public JComponent getComponent() {
        return plantUmlPreview;
    }

    @Override
    @Nullable
    public JComponent getPreferredFocusedComponent() {
        return plantUmlPreview;
    }

    @Override
    @NotNull
    @NonNls
    public String getName() {
        return "Preview";
    }

    @Override
    @NotNull
    public FileEditorState getState(@NotNull FileEditorStateLevel level) {
        return FileEditorState.INSTANCE;
    }

    @Override
    public void setState(@NotNull FileEditorState state) {
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return document.getText() != null;
    }

    @Override
    public void selectNotify() {
    }


    @Override
    public void deselectNotify() {
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
    }

    @Override
    @Nullable
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return null;
    }

    @Override
    @Nullable
    public FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    @Nullable
    public StructureViewBuilder getStructureViewBuilder() {
        return null;
    }

    @Override
    public void dispose() {
        Disposer.dispose(plantUmlPreview);
    }


}
