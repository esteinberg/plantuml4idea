package org.plantuml.idea.action.context;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.rendering.ImageItem;
import org.plantuml.idea.rendering.PlantUmlRenderer;
import org.plantuml.idea.toolwindow.PlantUmlImageLabel;
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
        String selectedSource = getSource(e.getProject());

		String canonicalPath = null;
		try {

            File file = File.createTempFile("diagram-", "." + imageFormat.toString().toLowerCase());
			canonicalPath = file.getCanonicalPath();
			file.deleteOnExit();

            PlantUmlRenderer.renderAndSave(selectedSource, UIUtils.getSelectedDir(FileEditorManager.getInstance(e.getProject()), FileDocumentManager.getInstance()),
                    imageFormat, file.getAbsolutePath(), file.getName().replace(".", "-%03d."),
				UIUtils.getPlantUmlToolWindow(e.getProject()).getZoom(), getPage(e));

            Desktop.getDesktop().open(file);
        } catch (IOException ex) {
			logger.error(canonicalPath, ex);
		}
	}

	protected int getPage(AnActionEvent e) {
		PlantUmlImageLabel data = (PlantUmlImageLabel) e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
		ImageItem imageWithData = data.getImageWithData();
		return imageWithData.getPage();
	}

    private String getSource(Project project) {
        return UIUtils.getSelectedSourceWithCaret(FileEditorManager.getInstance(project));
    }
}
