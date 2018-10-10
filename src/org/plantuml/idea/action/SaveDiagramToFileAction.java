package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
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

}
