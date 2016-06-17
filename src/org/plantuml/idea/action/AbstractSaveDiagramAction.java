package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.rendering.PlantUmlRenderer;
import org.plantuml.idea.util.UIUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author Eugene Steinberg
 */
public abstract class AbstractSaveDiagramAction extends DumbAwareAction {

    public static final String[] extensions;
    public static VirtualFile homeDir = null;
    private static VirtualFile lastDir = null;
    public static final String FILENAME = "diagram";
    Logger logger = Logger.getInstance(SaveDiagramToFileAction.class);

    static {
        PlantUml.ImageFormat[] values = PlantUml.ImageFormat.values();
        extensions = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            extensions[i] = values[i].toString().toLowerCase();
        }

        homeDir = LocalFileSystem.getInstance().findFileByPath(System.getProperty("user.home"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        FileSaverDescriptor fsd = new FileSaverDescriptor("Save diagram", "Please choose where to save diagram", extensions);

        VirtualFile baseDir = lastDir;

        if (baseDir == null) {
            if (e.getProject() == null) {
                baseDir = homeDir;
            } else {
                baseDir = e.getProject().getBaseDir();
            }
        }

        String defaultFileName = getDefaultFileName(e.getProject());

        final VirtualFileWrapper wrapper = FileChooserFactory.getInstance().createSaveFileDialog(
                fsd, e.getProject()).save(baseDir, defaultFileName);

        if (wrapper != null) {
            try {
                VirtualFile virtualFile = wrapper.getVirtualFile(true);
                if (virtualFile != null) {
                    lastDir = virtualFile.getParent();
                    logger.info("lastDir set to " + lastDir);
                }
                File file = wrapper.getFile();
                String[] tokens = file.getAbsolutePath().split("\\.(?=[^\\.]+$)");
                String base = tokens[0];
                String extension = tokens.length < 2 ? "" : tokens[1];
                PlantUml.ImageFormat imageFormat;
                try {
                    imageFormat = PlantUml.ImageFormat.valueOf(extension.toUpperCase());
                } catch (Exception ex) {
                    throw new IOException("Extension '" + extension + "' is not supported");
                }
                String selectedSource = getSource(e.getProject());

                String fileNameTemplate = base + "-%03d." + extension;

                PlantUmlRenderer.renderAndSave(selectedSource, UIUtils.getSelectedDir(FileEditorManager.getInstance(e.getProject()), FileDocumentManager.getInstance()),
                        imageFormat, file.getAbsolutePath(), fileNameTemplate,
                        UIUtils.getPlantUmlToolWindow(e.getProject()).getZoom());

            } catch (IOException e1) {
                String title = "Error writing diagram";
                String message = title + " to file:" + wrapper.getFile() + " : " + e1.toString();
                logger.warn(message);
                Messages.showErrorDialog(message, title);
            }
        }
    }

    protected abstract String getSource(Project project);

    private String getDefaultFileName(Project myProject) {
        VirtualFile selectedFile = UIUtils.getSelectedFile(FileEditorManager.getInstance(myProject), FileDocumentManager.getInstance());
        return selectedFile == null ? FILENAME : selectedFile.getNameWithoutExtension();
    }
}
