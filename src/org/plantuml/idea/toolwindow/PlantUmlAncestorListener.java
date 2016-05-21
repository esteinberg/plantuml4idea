package org.plantuml.idea.toolwindow;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.plantuml.idea.util.LazyApplicationPoolExecutor;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

class PlantUmlAncestorListener implements AncestorListener {
    private PlantUmlToolWindow plantUmlToolWindow;
    private Project project;
    private Logger logger = Logger.getInstance(PlantUmlAncestorListener.class);

    public PlantUmlAncestorListener(PlantUmlToolWindow plantUmlToolWindow, Project project) {
        this.plantUmlToolWindow = plantUmlToolWindow;
        this.project = project;
    }

    @Override
    public void ancestorAdded(AncestorEvent ancestorEvent) {
        logger.debug("ancestorAdded ", project.getName());
        plantUmlToolWindow.renderLater(LazyApplicationPoolExecutor.Delay.POST_DELAY);
    }

    @Override
    public void ancestorRemoved(AncestorEvent event) {

    }

    @Override
    public void ancestorMoved(AncestorEvent event) {

    }

}
