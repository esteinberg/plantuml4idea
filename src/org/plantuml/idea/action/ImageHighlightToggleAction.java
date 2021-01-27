package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.plantuml.idea.util.UIUtils;

public class ImageHighlightToggleAction extends ToggleAction implements DumbAware {

    @Override
    public boolean isSelected(AnActionEvent anActionEvent) {
        return PlantUmlSettings.getInstance().isHighlightInImages();
    }

    @Override
    public void setSelected(AnActionEvent anActionEvent, boolean b) {
        PlantUmlSettings.getInstance().setHighlightInImages(b);
        Project project = anActionEvent.getProject();
        PlantUmlToolWindow plantUmlToolWindow = UIUtils.getPlantUmlToolWindow(project);
        Editor selectedTextEditor = UIUtils.getSelectedTextEditor(FileEditorManager.getInstance(project));
        plantUmlToolWindow.highlightImages(selectedTextEditor);
    }
}
