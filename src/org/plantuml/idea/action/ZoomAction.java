package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.plantuml.idea.util.UIUtils;

/**
 * @author Eugene Steinberg
 */
public abstract class ZoomAction extends DumbAwareAction {
    protected static int DEFAULT_ZOOM = 100;
    protected static int MAX_ZOOM = 500;
    protected static int MIN_ZOOM = 20;
    protected static int ZOOM_STEP = 20;

    protected int getUnscaledZoom(Project project) {
        PlantUmlToolWindow plantUML = UIUtils.getPlantUmlToolWindow(project);
        return plantUML.getUnscaledZoom();
    }

    protected void setUnscaledZoom(Project project, int unscaledZoom) {
        PlantUmlToolWindow plantUML = UIUtils.getPlantUmlToolWindow(project);
        plantUML.setUnscaledZoom(unscaledZoom);
    }


    @Override
    public void update(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (project != null) {
            e.getPresentation().setEnabled(UIUtils.hasAnyImage(project));
        }
    }

}
