package org.plantuml.idea.toolwindow;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.toolwindow.listener.PlantUmlRenderingFileEditorManagerListener;
import org.plantuml.idea.toolwindow.listener.PlantUmlToolWindowOpener;

public class PlantUmlProjectComponent implements ProjectComponent {

    private static final Logger logger = Logger.getInstance(PlantUmlProjectComponent.class);

    private static final PlantUmlToolWindowOpener OPENER = new PlantUmlToolWindowOpener();
    private static final FileEditorManagerListener RENDERER = new PlantUmlRenderingFileEditorManagerListener();

    private Project project;

    public PlantUmlProjectComponent(Project project) {
        this.project = project;
    }

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {
    }

    @NotNull
    public String getComponentName() {
        return "PlantUmlToolWindowAutoOpener";
    }

    @Override
    public void projectOpened() {
        logger.debug("Registering listeners");
        project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, OPENER);
        project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, RENDERER);
    }

    @Override
    public void projectClosed() {
    }

}
