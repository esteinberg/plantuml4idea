package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;
import org.plantuml.idea.settings.PlantUmlSettings;
import org.plantuml.idea.util.UIUtils;

public class ImageHighlightToggleAction extends MyToggleAction {

    @Override
    public boolean isSelected(AnActionEvent anActionEvent) {
        return PlantUmlSettings.getInstance().isHighlightInImages();
    }

    @Override
    public void setSelected(AnActionEvent anActionEvent, boolean b) {
        PlantUmlSettings.getInstance().setHighlightInImages(b);
        Project project = anActionEvent.getProject();
        PlantUmlPreviewPanel previewPanel = UIUtils.getEditorOrToolWindowPreview(anActionEvent);
        Editor selectedTextEditor = UIUtils.getSelectedTextEditor(FileEditorManager.getInstance(project), null);
        previewPanel.highlightImages(selectedTextEditor);
    }
}
