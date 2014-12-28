package org.plantuml.idea.action;

import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
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

    protected int getZoom(Project project) {
        PlantUmlToolWindow plantUML = UIUtils.getPlantUmlToolWindow(project);
        return plantUML.getZoom();
    }

    protected void setZoom(Project project, int zoom) {
        PlantUmlToolWindow plantUML = UIUtils.getPlantUmlToolWindow(project);
        plantUML.setZoom(zoom);
    }

}
