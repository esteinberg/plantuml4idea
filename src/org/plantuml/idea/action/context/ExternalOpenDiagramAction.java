package org.plantuml.idea.action.context;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.external.PlantUmlFacade;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.toolwindow.image.ImageContainer;
import org.plantuml.idea.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * @author Henady Zakalusky
 */
public abstract class ExternalOpenDiagramAction extends DumbAwareAction {

    protected static final Logger logger = Logger.getInstance(ExternalOpenDiagramAction.class);

    private PlantUml.ImageFormat imageFormat;

    public ExternalOpenDiagramAction(String text, @Nullable Icon icon, PlantUml.ImageFormat imageFormat) {
        super(text, text, icon);
        this.imageFormat = imageFormat;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        ImageContainer data = (ImageContainer) e.getData(ImageContainer.CONTEXT_COMPONENT);
        RenderRequest renderRequest = data.getRenderRequest();
        String selectedSource = renderRequest.getSource();
        File sourceFile = renderRequest.getSourceFile();

        String canonicalPath = null;
        try {

            File file = File.createTempFile("diagram-", "." + imageFormat.toString().toLowerCase());
            canonicalPath = file.getCanonicalPath();
            file.deleteOnExit();

            PlantUmlFacade.get().renderAndSave(selectedSource, sourceFile,
                    imageFormat, file.getAbsolutePath(), null,
                    UIUtils.getPlantUmlToolWindow(project).getZoom(), getPage(e));

            Desktop.getDesktop().open(file);
        } catch (IOException ex) {
            logger.error(canonicalPath, ex);
        }
    }

    protected int getPage(AnActionEvent e) {
        ImageContainer data = (ImageContainer) e.getData(ImageContainer.CONTEXT_COMPONENT);
        return data.getPage();
    }

    private String getSource(Project project) {
        return UIUtils.getSelectedSourceWithCaret(FileEditorManager.getInstance(project));
    }
}
