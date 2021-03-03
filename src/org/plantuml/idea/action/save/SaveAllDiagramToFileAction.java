package org.plantuml.idea.action.save;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.plantuml.idea.util.UIUtils;

/**
 * @author Eugene Steinberg
 */
public class SaveAllDiagramToFileAction extends AbstractSaveDiagramAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (project != null) {
            PlantUmlToolWindow umlToolWindow = UIUtils.getPlantUmlToolWindow(e.getProject());
            if (umlToolWindow != null) {
                int selectedPage = umlToolWindow.getSelectedPage();
                e.getPresentation().setEnabled(selectedPage != -1 || umlToolWindow.getNumPages() > 1);
            } else {
                e.getPresentation().setEnabled(false);
            }
        }
    }
}
