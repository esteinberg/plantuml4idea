package org.plantuml.idea.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;
import org.plantuml.idea.rendering.LazyApplicationPoolExecutor;
import org.plantuml.idea.rendering.RenderCommand;
import org.plantuml.idea.settings.PlantUmlSettings;
import org.plantuml.idea.util.UIUtils;

import javax.swing.*;

public class ReloadNowAction extends DumbAwareAction {
    public static final Icon refreshAutoDisabled = IconLoader.getIcon("/images/forceRefresh.png");

    private PlantUmlSettings settings;

    public ReloadNowAction() {
        settings = PlantUmlSettings.getInstance();
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        if (project != null) {
            PlantUmlPreviewPanel previewPanel = UIUtils.getEditorOrToolWindowPreview(e);
            if (previewPanel != null) {
                previewPanel.processRequest(LazyApplicationPoolExecutor.Delay.NOW, RenderCommand.Reason.REFRESH);
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
            e.getPresentation().setIcon(refreshAutoDisabled);
            e.getPresentation().setDescription("Reload PlantUml Diagram (Automatic Rendering disabled)");
        }
    }
}
