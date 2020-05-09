package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.util.UIUtils;

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

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (project != null) {
            boolean enabled = UIUtils.hasAnyImage(project);
            e.getPresentation().setEnabled(enabled);
            if (enabled) {
                int zoom = getZoom(project);
                e.getPresentation().setEnabled(zoom < MAX_ZOOM);
            }
        }
    }

}
