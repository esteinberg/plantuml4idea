package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;
import org.plantuml.idea.util.UIUtils;

/**
 * @author Eugene Steinberg
 */
public abstract class ZoomAction extends DumbAwareAction {
    protected static int DEFAULT_ZOOM = 100;
    public static int MAX_ZOOM = 500;
    protected static int MIN_ZOOM = 20;
    protected static int ZOOM_STEP = 20;

    protected int getUnscaledZoom(AnActionEvent event) {
        PlantUmlPreviewPanel plantUML = UIUtils.getEditorOrToolWindowPreview(event);
        return plantUML.getZoom().getUnscaledZoom();
    }

    protected void changeZoom(AnActionEvent e, int unscaledZoom) {
        PlantUmlPreviewPanel plantUML = UIUtils.getEditorOrToolWindowPreview(e);
        plantUML.changeZoom(unscaledZoom, null);
    }


    @Override
    public void update(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (project != null) {
            boolean enabled = UIUtils.hasAnyImage(e);
            e.getPresentation().setEnabled(enabled);
            if (enabled) {
                int zoom = getUnscaledZoom(e);
                e.getPresentation().setDescription("Actual zoom: " + zoom + "%");
            }
        }
    }

}
