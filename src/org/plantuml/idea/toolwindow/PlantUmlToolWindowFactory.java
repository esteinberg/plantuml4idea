package org.plantuml.idea.toolwindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.messages.MessageBus;
import org.plantuml.idea.messaging.RenderingNotifier;
import org.plantuml.idea.plantuml.PlantUmlResult;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 *
 * @author Eugene Steinberg
 */

public class PlantUmlToolWindowFactory implements ToolWindowFactory {
    private ToolWindow toolWindow;
    private JPanel mainPanel;
    private JLabel imageLabel;

    public PlantUmlToolWindowFactory() {

    }

    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        this.toolWindow = toolWindow;
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);

        MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
        messageBus.connect().subscribe(RenderingNotifier.RENDERING_TOPIC, new RenderingNotifier() {
            public void afterRendering(PlantUmlResult result) {
                setDiagram(result.getDiagram());
            }
        });
    }

    private void setDiagram(BufferedImage image) {
        if (image != null) {
            imageLabel.setIcon(new ImageIcon(image));
            imageLabel.setPreferredSize(new Dimension(image.getWidth(),image.getHeight()));
        }
    }
}
