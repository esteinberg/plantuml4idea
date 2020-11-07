package org.plantuml.idea.action.context;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.rendering.PlantUmlRendererUtil;
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
        Project project = e.getProject();
        String selectedSource = getSource(project);
        VirtualFile sourceVFile = UIUtils.getSelectedSourceFile(project);
        File selectedDir = UIUtils.getParent(sourceVFile);
        File sourceFile = sourceVFile != null ? new File(sourceVFile.getPath()) : null;

        String canonicalPath = null;
        try {

            File file = File.createTempFile("diagram-", "." + imageFormat.toString().toLowerCase());
            canonicalPath = file.getCanonicalPath();
            file.deleteOnExit();

            PlantUmlRendererUtil.renderAndSave(selectedSource, sourceFile, selectedDir,
                    imageFormat, file.getAbsolutePath(), file.getName().replace(".", "-%03d."),
                    UIUtils.getPlantUmlToolWindow(project).getScaledZoom(), getPage(e));

            Desktop.getDesktop().open(file);
        } catch (IOException ex) {
			logger.error(canonicalPath, ex);
		}
	}

    protected int getPage(AnActionEvent e) {
        PlantUmlImageLabel data = (PlantUmlImageLabel) e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
        return data.getPage();
    }

    private String getSource(Project project) {
        return UIUtils.getSelectedSourceWithCaret(FileEditorManager.getInstance(project));
    }
}
