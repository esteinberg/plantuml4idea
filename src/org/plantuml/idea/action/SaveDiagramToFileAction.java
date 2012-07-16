package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.plantuml.PlantUmlResult;
import org.plantuml.idea.util.UIUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Eugene Steinberg
 */
public class SaveDiagramToFileAction extends AnAction {
    Logger logger = Logger.getInstance(SaveDiagramToFileAction.class);
    public static final String[] extensions;
    public static VirtualFile homeDir = null;
    private static VirtualFile lastFile = null;


    static {
        PlantUml.ImageFormat[] values = PlantUml.ImageFormat.values();
        extensions = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            extensions[i] = values[i].toString().toLowerCase();
        }

        homeDir = LocalFileSystem.getInstance().findFileByPath(System.getProperty("user.home"));
    }

    public static final String FILENAME = "diagram";

    @Override
    public void actionPerformed(AnActionEvent e) {
        FileOutputStream os = null;
        FileSaverDescriptor fsd = new FileSaverDescriptor("Save diagram", "Please choose where to save diagram", extensions);

        final VirtualFile baseDir = lastFile == null? homeDir: lastFile;
        String defaultFileName = getDefaultFileName(e.getProject());

        final VirtualFileWrapper wrapper = FileChooserFactory.getInstance().createSaveFileDialog(
                fsd, e.getProject()).save(baseDir, defaultFileName);

        if (wrapper != null) {
            try {
                if (wrapper.getVirtualFile() != null)
                    lastFile = wrapper.getVirtualFile().getParent();
                File file = wrapper.getFile();
                String name = file.getName();
                String extension = name.substring(name.lastIndexOf('.') + 1);
                PlantUml.ImageFormat imageFormat;
                try {
                    imageFormat = PlantUml.ImageFormat.valueOf(extension.toUpperCase());
                } catch (Exception e3) {
                    throw new IOException("Extension '" + extension + "' is not supported");
                }
                PlantUmlResult result = PlantUml.render(UIUtils.getSelectedSource(e.getProject()), imageFormat);
                os = new FileOutputStream(file);
                os.write(result.getDiagramBytes());
                os.flush();
            } catch (IOException e1) {
                String title = "Error writing diagram";
                String message = title + " to file:" + wrapper.getFile() + " : " + e1.toString();
                logger.warn(message);
                Messages.showErrorDialog(message, title);
            } finally {
                try {
                    if (os != null)
                        os.close();
                } catch (IOException e1) {
                    // do nothing
                }
            }
        }
    }

    private String getDefaultFileName(Project myProject) {
        VirtualFile selectedFile = UIUtils.getSelectedFile(myProject);
        return selectedFile == null? FILENAME: selectedFile.getNameWithoutExtension();
    }


}
