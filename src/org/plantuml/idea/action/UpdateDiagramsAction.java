package org.plantuml.idea.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.rendering.LazyApplicationPoolExecutor;
import org.plantuml.idea.rendering.RenderCommand;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.plantuml.idea.util.UIUtils;

import javax.swing.*;

public class UpdateDiagramsAction extends DumbAwareAction {
    public static final Icon UpdateAutoDisabled = IconLoader.getIcon("/images/forceUpdate.png");

    private PlantUmlSettings settings;

    public UpdateDiagramsAction() {
        settings = PlantUmlSettings.getInstance();
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        if (project != null) {
            PlantUmlToolWindow plantUmlToolWindow = UIUtils.getPlantUmlToolWindow(project);
            if (plantUmlToolWindow != null) {
                plantUmlToolWindow.processRequest(LazyApplicationPoolExecutor.Delay.NOW, RenderCommand.Reason.MANUAL_UPDATE);
            }
        }
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        if (settings.isAutoRender()) {
            e.getPresentation().setIcon(AllIcons.Javaee.UpdateRunningApplication);
            e.getPresentation().setDescription("Update PlantUml Diagram - changes only");
        } else {
            e.getPresentation().setIcon(UpdateAutoDisabled);
            e.getPresentation().setDescription("Update PlantUml Diagram - changes only (Automatic Rendering disabled)");
        }
    }
}
