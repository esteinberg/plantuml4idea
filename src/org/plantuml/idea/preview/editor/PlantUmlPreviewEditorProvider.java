
package org.plantuml.idea.preview.editor;

import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.PossiblyDumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.util.Utils;

public class PlantUmlPreviewEditorProvider implements FileEditorProvider, PossiblyDumbAware {

  public static final String EDITOR_TYPE_ID = "PlantUMLPreviewEditor";
  private boolean documentListener = true;

  public PlantUmlPreviewEditorProvider() {
  }

  public PlantUmlPreviewEditorProvider(boolean documentListener) {
    this.documentListener = documentListener;
  }

  @Override
  public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
    return Utils.isPlantUmlFileType(project, file);
  }

  @Override
  @NotNull
  public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
    return new PlantUmlPreviewEditor(FileDocumentManager.getInstance().getDocument(file), file, project, documentListener);
  }

  @Override
  public void disposeEditor(@NotNull FileEditor editor) {
    editor.dispose();
  }

  @Override
  @NotNull
  public FileEditorState readState(@NotNull Element sourceElement, @NotNull Project project, @NotNull VirtualFile file) {
    return FileEditorState.INSTANCE;
  }

  @Override
  public void writeState(@NotNull FileEditorState state, @NotNull Project project, @NotNull Element targetElement) {
  }

  @Override
  @NotNull
  public String getEditorTypeId() {
    return EDITOR_TYPE_ID;
  }

  @Override
  @NotNull
  public FileEditorPolicy getPolicy() {
    return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
  }

  @Override
  public boolean isDumbAware() {
    return true;
  }
}
