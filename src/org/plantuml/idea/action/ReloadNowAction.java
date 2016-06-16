package org.plantuml.idea.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.rendering.LazyApplicationPoolExecutor;
import org.plantuml.idea.rendering.RenderCommand;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.plantuml.idea.util.UIUtils;

public class ReloadNowAction extends DumbAwareAction {

    private PlantUmlSettings settings;

    public ReloadNowAction() {
        settings = PlantUmlSettings.getInstance();
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        if (project != null) {
            PlantUmlToolWindow plantUmlToolWindow = UIUtils.getPlantUmlToolWindow(project);
            if (plantUmlToolWindow != null) {
                plantUmlToolWindow.renderLater(LazyApplicationPoolExecutor.Delay.NOW, RenderCommand.Reason.REFRESH);
            }
        }
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        if (settings.isAutoRender()) {
            e.getPresentation().setIcon(AllIcons.Actions.Refresh);
            e.getPresentation().setDescription("Reload PlantUml Diagram");
        } else {
            e.getPresentation().setIcon(AllIcons.Actions.ForceRefresh);
            e.getPresentation().setDescription("Reload PlantUml Diagram (Automatic Rendering Disabled)");
        }
    }
}
