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
public class PrevPageAction extends DumbAwareAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        final Project project = anActionEvent.getProject();
        if (project != null) {
            PlantUmlPreviewPanel previewPanel = UIUtils.getEditorOrToolWindowPreview(anActionEvent);
            if (previewPanel != null)
                previewPanel.prevPage();
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        boolean enabled = false;
        if (project != null) {
            PlantUmlPreviewPanel previewPanel = UIUtils.getEditorOrToolWindowPreview(e);
            if (previewPanel != null) {
                enabled = previewPanel.getNumPages() > 1;
            }
        }
        e.getPresentation().setEnabled(enabled);
    }
}
