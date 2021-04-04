package org.plantuml.idea.action.save;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;
import org.plantuml.idea.util.UIUtils;

/**
 * @author Eugene Steinberg
 */
public class SaveAllDiagramToFileAction extends AbstractSaveDiagramAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (project != null) {
            PlantUmlPreviewPanel previewPanel = UIUtils.getEditorPreviewOrToolWindowPanel(e);
            if (previewPanel != null) {
                int selectedPage = previewPanel.getSelectedPage();
                e.getPresentation().setEnabled(selectedPage != -1 || previewPanel.getNumPages() > 1);
            } else {
                e.getPresentation().setEnabled(false);
            }
        }
    }
}
