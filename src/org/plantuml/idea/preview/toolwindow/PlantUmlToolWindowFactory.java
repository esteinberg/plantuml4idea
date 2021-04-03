package org.plantuml.idea.preview.toolwindow;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
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
import org.plantuml.idea.preview.PreviewParentWrapper;
import org.plantuml.idea.rendering.LazyApplicationPoolExecutor;
import org.plantuml.idea.rendering.RenderCommand;

import java.awt.*;

/**
 * @author Eugene Steinberg
 */
public class PlantUmlToolWindowFactory implements ToolWindowFactory, DumbAware {

    public static final String ID = "PlantUML";

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        PlantUmlPreviewPanel previewPanel = new PlantUmlToolWindowPreviewPanel(project, new ToolWindowWrapper(toolWindow));
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(previewPanel, "", false);
        toolWindow.getContentManager().addContent(content);

//        if (PlantUmlSettings.getInstance().isAutoRender()) {
//            plantUmlPreviewPanel.processRequest(LazyApplicationPoolExecutor.Delay.NOW, RenderCommand.Reason.FILE_SWITCHED);
//        }
    }

    private class PlantUmlToolWindowPreviewPanel extends PlantUmlPreviewPanel {
        public PlantUmlToolWindowPreviewPanel(Project project, PreviewParentWrapper parentWrapper) {
            super(project, parentWrapper);
        }

        protected void createToolbar() {
            DefaultActionGroup newGroup = getActionGroup();
            final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, newGroup, true);
            actionToolbar.setTargetComponent(this);
            add(actionToolbar.getComponent(), BorderLayout.PAGE_START);
        }

        @Override
        public void processRequest(LazyApplicationPoolExecutor.Delay delay, RenderCommand.Reason reason) {
            super.processRequest(delay, reason);
        }
    }
}
