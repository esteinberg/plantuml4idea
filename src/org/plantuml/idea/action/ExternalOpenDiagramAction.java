package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.rendering.PlantUmlRenderer;
import org.plantuml.idea.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * @author Henady Zakalusky
 */
public class ExternalOpenDiagramAction extends DumbAwareAction {
    Logger logger = Logger.getInstance(ExternalOpenDiagramAction.class);

    private PlantUml.ImageFormat imageFormat;

    public ExternalOpenDiagramAction(String text, @Nullable Icon icon, PlantUml.ImageFormat imageFormat) {
        super(text, text, icon);
        this.imageFormat = imageFormat;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        String selectedSource = getSource(e.getProject());

        try {
            File file = File.createTempFile("diagram-", "." + imageFormat.toString().toLowerCase());

            file.deleteOnExit();

            PlantUmlRenderer.renderAndSave(selectedSource, UIUtils.getSelectedDir(e.getProject()),
                    imageFormat, file.getAbsolutePath(), file.getName().replace(".", "-%03d."),
                    UIUtils.getPlantUmlToolWindow(e.getProject()).getZoom());

            Desktop.getDesktop().open(file);
        } catch (IOException ex) {
            logger.error(ex);
        }
    }

    private String getSource(Project project) {
        return UIUtils.getSelectedSourceWithCaret(project);
    }
}
