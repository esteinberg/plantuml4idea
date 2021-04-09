package org.plantuml.idea.action.context;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.external.PlantUmlFacade;
import org.plantuml.idea.plantuml.ImageFormat;
import org.plantuml.idea.preview.image.ImageContainer;
import org.plantuml.idea.rendering.ImageItem;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderingType;
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

    private ImageFormat imageFormat;

    public ExternalOpenDiagramAction(String text, @Nullable Icon icon, ImageFormat imageFormat) {
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
        ImageItem imageItem = data.getImageItem();
        byte[] imageBytes = imageItem.getImageBytes();

        String canonicalPath = null;
        try {

            File file = File.createTempFile("diagram-", "." + imageFormat.toString().toLowerCase());
            canonicalPath = file.getCanonicalPath();
            file.deleteOnExit();

            if (data.getImageItem().getRenderingType() == RenderingType.REMOTE) {
                if (imageItem.getFormat() != imageFormat) {
                    throw new RuntimeException("wrong format");
                }
                PlantUmlFacade.get().save(file.getAbsolutePath(), imageBytes);
            } else {
                PlantUmlFacade.get().renderAndSave(selectedSource, sourceFile,
                        imageFormat, file.getAbsolutePath(), null,
                        UIUtils.getEditorOrToolWindowPreview(e).getZoom(), getPage(e));
            }

            Desktop.getDesktop().open(file);
        } catch (IOException ex) {
            logger.error(canonicalPath, ex);
        }
    }

    protected int getPage(AnActionEvent e) {
        ImageContainer data = (ImageContainer) e.getData(ImageContainer.CONTEXT_COMPONENT);
        return data.getPage();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (project != null) {
            ImageContainer data = (ImageContainer) e.getData(ImageContainer.CONTEXT_COMPONENT);
            ImageItem imageItem = data.getImageItem();
            RenderingType renderingType = imageItem.getRenderingType();
            e.getPresentation().setEnabled(renderingType != RenderingType.REMOTE || imageItem.getFormat() == imageFormat);
        }
    }
}
