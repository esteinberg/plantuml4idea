package org.plantuml.idea.action.save;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Steinberg
 */
public class SaveAllDiagramToFileAction extends AbstractSaveDiagramAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        e.getPresentation().setEnabled(project != null);
    }
}
