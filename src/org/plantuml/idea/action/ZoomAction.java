package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.plantuml.idea.util.UIUtils;

/**
 * @author Eugene Steinberg
 */
public abstract class ZoomAction extends AnAction {
    protected static int DEFAULT_ZOOM = 100;
    protected static int MAX_ZOOM = 500;
    protected static int MIN_ZOOM = 20;
    protected static int ZOOM_STEP = 10;

    protected int getZoom(Project project) {
        PlantUmlToolWindow toolWindow = UIUtils.getToolWindow(project);
        if (toolWindow != null) {
            return toolWindow.getZoom();
        }
        return 100;
    }

    protected void setZoom(Project project, int zoom) {
        PlantUmlToolWindow toolWindow = UIUtils.getToolWindow(project);
        if (toolWindow != null) {
            toolWindow.setZoom(project, zoom);
        }
    }

}
