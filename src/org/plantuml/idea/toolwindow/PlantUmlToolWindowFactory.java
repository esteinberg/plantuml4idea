package org.plantuml.idea.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

/**
 * @author Eugene Steinberg
 */
public class PlantUmlToolWindowFactory implements ToolWindowFactory {

    public static final String ID = "PlantUML";

    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        PlantUmlToolWindow plantUmlToolWindow = new PlantUmlToolWindow(project, toolWindow);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(plantUmlToolWindow, "", false);
        toolWindow.getContentManager().addContent(content);
    }

}
