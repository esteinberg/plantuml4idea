package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;

/**
 * @author Eugene Steinberg
 */
public abstract class ZoomAction extends AnAction {
    protected static int DEFAULT_ZOOM = 100;
    protected static int MAX_ZOOM=500;
    protected static int MIN_ZOOM=20;
    protected static int ZOOM_STEP=20;

    protected int getZoom(Project project) {
        PlantUmlToolWindow plantUML = getToolWindow(project);
        return plantUML.getZoom();
    }

    protected void setZoom(Project project,int zoom) {
        PlantUmlToolWindow plantUML = getToolWindow(project);
        plantUML.setZoom(zoom);
    }

    private PlantUmlToolWindow getToolWindow(Project project) {
        ToolWindow plantUMLToolWindow = ToolWindowManager.getInstance(project).getToolWindow("PlantUML");
        return (PlantUmlToolWindow) plantUMLToolWindow.getContentManager().getContent(0).getComponent();
    }
}
