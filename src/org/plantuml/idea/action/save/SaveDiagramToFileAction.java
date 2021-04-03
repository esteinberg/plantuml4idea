package org.plantuml.idea.action.save;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;
import org.plantuml.idea.preview.image.ImageContainer;
import org.plantuml.idea.util.UIUtils;

import javax.swing.*;

/**
 * @author Eugene Steinberg
 */
public class SaveDiagramToFileAction extends AbstractSaveDiagramAction {

    @Override
    protected int getPageNumber(AnActionEvent e) {
        PlantUmlPreviewPanel previewPanel = UIUtils.getPlantUmlPreviewPanel(e);
        JPanel imagesPanel = previewPanel.getImagesPanel();
        ImageContainer image = (ImageContainer) imagesPanel.getComponent(0);
        return image.getPage();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (project != null) {
            PlantUmlPreviewPanel previewPanel = UIUtils.getPlantUmlPreviewPanel(e);
            if (previewPanel != null) {
                int selectedPage = previewPanel.getSelectedPage();
                e.getPresentation().setEnabled(previewPanel.getNumPages() == 1 || (previewPanel.getNumPages() > 1 && selectedPage != -1));
            } else {
                e.getPresentation().setEnabled(false);
            }
        }
    }


}
