package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.plantuml.idea.util.UIUtils;

/**
 * Author: Eugene Steinberg
 * Date: 2/28/13
 */
public class PrevPageAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        PlantUmlToolWindow toolWindow = UIUtils.getToolWindow(project);
        if (toolWindow != null) {
            toolWindow.prevPage(project);
        }
    }
}
