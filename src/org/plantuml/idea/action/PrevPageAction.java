package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.plantuml.idea.util.UIUtils;

/**
 * Author: Eugene Steinberg
 * Date: 2/28/13
 */
public class PrevPageAction extends DumbAwareAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        final Project project = anActionEvent.getProject();
        if (project != null) {
            PlantUmlToolWindow plantUmlToolWindow = UIUtils.getPlantUmlToolWindow(project);
            if (plantUmlToolWindow != null)
                plantUmlToolWindow.prevPage();
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (project != null) {
            PlantUmlToolWindow plantUmlToolWindow = UIUtils.getPlantUmlToolWindow(project);
            if (plantUmlToolWindow != null)
                e.getPresentation().setEnabled(plantUmlToolWindow.getNumPages() > 1);
        }
    }
}
