package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import org.plantuml.idea.toolwindow.PlantUmlImageLabel;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.plantuml.idea.util.UIUtils;

import javax.swing.*;

/**
 * @author Eugene Steinberg
 */
public class SaveDiagramToFileAction extends AbstractSaveDiagramAction {

    @Override
    protected int getPageNumber(AnActionEvent e) {
        PlantUmlToolWindow umlToolWindow = UIUtils.getPlantUmlToolWindow(e.getProject());
        JPanel imagesPanel = umlToolWindow.getImagesPanel();
        PlantUmlImageLabel image = (PlantUmlImageLabel) imagesPanel.getComponent(0);
        return image.getPage();
    }

    @Override
    protected String getSource(Project project) {
        return UIUtils.getSelectedSourceWithCaret(FileEditorManager.getInstance(project));
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        final Project project = e.getProject();
        if (project != null) {
            PlantUmlToolWindow toolWindow = UIUtils.getPlantUmlToolWindow(project);
            if (toolWindow != null) {
                e.getPresentation().setEnabled(toolWindow.getImagesPanel().getComponentCount() == 1);
            }
        }
    }
}
