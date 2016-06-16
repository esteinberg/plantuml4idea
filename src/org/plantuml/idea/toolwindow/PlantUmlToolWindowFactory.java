package org.plantuml.idea.toolwindow;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.rendering.LazyApplicationPoolExecutor;
import org.plantuml.idea.rendering.RenderCommand;

/**
 * @author Eugene Steinberg
 */
public class PlantUmlToolWindowFactory implements ToolWindowFactory, DumbAware {

    public static final String ID = "PlantUML";

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        PlantUmlToolWindow plantUmlToolWindow = new PlantUmlToolWindow(project, toolWindow);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(plantUmlToolWindow, "", false);
        toolWindow.getContentManager().addContent(content);
        
        if (PlantUmlSettings.getInstance().isAutoRender()) {
            plantUmlToolWindow.renderLater(LazyApplicationPoolExecutor.Delay.POST_DELAY, RenderCommand.Reason.FILE_SWITCHED);
        }
    }

}
