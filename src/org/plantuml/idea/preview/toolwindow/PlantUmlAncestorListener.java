package org.plantuml.idea.preview.toolwindow;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;
import org.plantuml.idea.rendering.LazyApplicationPoolExecutor;
import org.plantuml.idea.rendering.RenderCommand;
import org.plantuml.idea.settings.PlantUmlSettings;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public class PlantUmlAncestorListener implements AncestorListener {
    private static Logger logger = Logger.getInstance(PlantUmlAncestorListener.class);
    private final PlantUmlSettings settings;

    private PlantUmlPreviewPanel previewPanel;
    private Project project;

    public PlantUmlAncestorListener(PlantUmlPreviewPanel previewPanel, Project project) {
        this.previewPanel = previewPanel;
        this.project = project;
        settings = PlantUmlSettings.getInstance();
    }

    @Override
    public void ancestorAdded(AncestorEvent ancestorEvent) {
        logger.debug("ancestorAdded ", project.getName());
        if (settings.isAutoRender()) {
            previewPanel.processRequest(LazyApplicationPoolExecutor.Delay.NOW, RenderCommand.Reason.FILE_SWITCHED);
        }
    }

    @Override
    public void ancestorRemoved(AncestorEvent event) {

    }

    @Override
    public void ancestorMoved(AncestorEvent event) {

    }

}
