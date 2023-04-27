package org.plantuml.idea.preview.toolwindow;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;
import org.plantuml.idea.preview.editor.PlantUmlToolbarPanel;
import org.plantuml.idea.rendering.LazyApplicationPoolExecutor;
import org.plantuml.idea.rendering.RenderCommand;
import org.plantuml.idea.settings.PlantUmlSettings;

import javax.swing.*;
import javax.swing.event.AncestorListener;
import java.awt.*;

/**
 * @author Eugene Steinberg
 */
public class PlantUmlToolWindowFactory implements ToolWindowFactory, DumbAware {

    public static final String ID = "PlantUML";

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        PlantUmlPreviewPanel previewPanel = new PlantUmlToolWindowPreviewPanel(project, toolWindow);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(previewPanel, "", false);
        toolWindow.getContentManager().addContent(content);

        if (PlantUmlSettings.getInstance().isAutoRender()) {
            previewPanel.processRequest(LazyApplicationPoolExecutor.Delay.NOW, RenderCommand.Reason.FILE_SWITCHED);
        }
    }

    public static class PlantUmlToolWindowPreviewPanel extends PlantUmlPreviewPanel {
        private AncestorListener plantUmlAncestorListener;
        private JComponent parentComponent;

        public PlantUmlToolWindowPreviewPanel(Project project, ToolWindow toolWindow) {
            super(project, null, toolWindow.getComponent());
            plantUmlAncestorListener = new PlantUmlAncestorListener(this, project);

            //must be last
            parentComponent = toolWindow.getComponent();
            if (parentComponent != null) {
                parentComponent.addAncestorListener(plantUmlAncestorListener);
            }
        }

        protected void createToolbar() {
            ActionManager actionManager = ActionManager.getInstance();
            DefaultActionGroup newGroup = PlantUmlToolbarPanel.prepareToolbar(this, executionStatusPanel, actionManager);
            final ActionToolbar actionToolbar = actionManager.createActionToolbar("plantuml4idea-ToolWindow", newGroup, true);
            actionToolbar.setTargetComponent(this);
            add(actionToolbar.getComponent(), BorderLayout.PAGE_START);
        }


        @Override
        public void dispose() {
            super.dispose();
            if (parentComponent != null) {
                parentComponent.removeAncestorListener(plantUmlAncestorListener);
            }
        }

        @Override
        public void processRequest(LazyApplicationPoolExecutor.Delay delay, RenderCommand.Reason reason) {
            super.processRequest(delay, reason);
        }
    }
}
