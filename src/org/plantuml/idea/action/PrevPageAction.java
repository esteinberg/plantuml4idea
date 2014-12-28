package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.plantuml.idea.util.UIUtils;

/**
 * Author: Eugene Steinberg
 * Date: 2/28/13
 */
public class PrevPageAction extends DumbAwareAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        final Project project = anActionEvent.getProject();
        if (project != null)
            UIUtils.getPlantUmlToolWindow(project).prevPage();
    }
}
