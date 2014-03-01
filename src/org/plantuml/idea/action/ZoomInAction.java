package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

/**
 * @author Eugene Steinberg
 */
public class ZoomInAction extends ZoomAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            setZoom(project, Math.min(MAX_ZOOM, getZoom(project) + ZOOM_STEP));
        }
    }
}
