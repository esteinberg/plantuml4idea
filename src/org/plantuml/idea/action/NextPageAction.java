package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;
import org.plantuml.idea.util.UIUtils;

/**
 * Author: Eugene Steinberg
 * Date: 2/28/13
 */
public class NextPageAction extends DumbAwareAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        final Project project = anActionEvent.getProject();
        if (project != null) {
            PlantUmlPreviewPanel previewPanel = UIUtils.getEditorPreviewOrToolWindowPanel(anActionEvent);
            if (previewPanel != null)
                previewPanel.nextPage();
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (project != null) {
            PlantUmlPreviewPanel previewPanel = UIUtils.getEditorPreviewOrToolWindowPanel(e);
            if (previewPanel != null)
                e.getPresentation().setEnabled(previewPanel.getNumPages() > 1);
        }
    }
}
