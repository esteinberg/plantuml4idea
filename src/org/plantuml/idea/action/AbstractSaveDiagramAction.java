package org.plantuml.idea.action;

import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.util.PathUtilRt;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.rendering.ImageItem;
import org.plantuml.idea.rendering.PlantUmlRendererUtil;
import org.plantuml.idea.rendering.RenderCacheItem;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.plantuml.idea.util.UIUtils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

import static org.plantuml.idea.util.UIUtils.NOTIFICATION;

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

    public AbstractSaveDiagramAction() {
    }

    public AbstractSaveDiagramAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();

        String selectedSource = getSource(e.getProject());

        if (StringUtils.isBlank(selectedSource)) {
            Notifications.Bus.notify(NOTIFICATION.createNotification("No PlantUML source code", MessageType.WARNING));
            return;
        }

        FileSaverDescriptor fsd = new FileSaverDescriptor("Save diagram", "Please choose where to save diagram", extensions);

        VirtualFile baseDir = lastDir;

        if (baseDir == null) {
            if (project == null) {
                baseDir = homeDir;
            } else {
                baseDir = ProjectUtil.guessProjectDir(project);
            }
        }


        String defaultFileName = getDefaultFileName(e, e.getProject());

        final VirtualFileWrapper wrapper = FileChooserFactory.getInstance().createSaveFileDialog(
                fsd, project).save(baseDir, defaultFileName);

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
                String extension;

                if (tokens.length < 2) {
                    extension = "png";
                    file = new File(base + ".png");
                } else {
                    extension = tokens[1];
                }


                PlantUml.ImageFormat imageFormat;
                try {
                    imageFormat = PlantUml.ImageFormat.valueOf(extension.toUpperCase());
                } catch (Exception ex) {
                    throw new IOException("Extension '" + extension + "' is not supported");
                }


                String fileNameTemplate = base + "-%03d." + extension;

                PlantUmlRendererUtil.renderAndSave(selectedSource, UIUtils.getSelectedDir(FileEditorManager.getInstance(project), FileDocumentManager.getInstance()),
                        imageFormat, file.getAbsolutePath(), fileNameTemplate,
                        UIUtils.getPlantUmlToolWindow(project).getZoom(), getPageNumber(e));

            } catch (IOException e1) {
                String title = "Error writing diagram";
                String message = title + " to file:" + wrapper.getFile() + " : " + e1.toString();
                logger.warn(message);
                Messages.showErrorDialog(message, title);
            }
        }
    }

    protected int getPageNumber(AnActionEvent e) {
        return -1;
    }


    private String getDefaultFileName(AnActionEvent e, Project myProject) {
        String filename = null;

        try {
            PlantUmlToolWindow plantUmlToolWindow = UIUtils.getPlantUmlToolWindow(myProject);
            if (plantUmlToolWindow != null) {
                RenderCacheItem displayedItem = plantUmlToolWindow.getDisplayedItem();
                int selectedPage = plantUmlToolWindow.getSelectedPage();
                ImageItem imageItem = displayedItem.getImageItem(selectedPage < 0 ? 0 : selectedPage);
                if (imageItem != null) {
                    filename = imageItem.getFilename();
                }
                if (filename == null) {
                    String sourceFilePath = displayedItem.getSourceFilePath();
                    filename = FileUtilRt.getNameWithoutExtension(PathUtilRt.getFileName(sourceFilePath));
                }
            }
            if (filename != null) {
                filename = FileUtil.sanitizeFileName(filename);
            }
        } catch (Exception ex) {
            logger.error(ex);
        }

        if (filename == null) {
            filename = FILENAME;
        }
        return filename;
    }

    protected String getSource(Project project) {
        String selectedSource = null;
        PlantUmlToolWindow plantUmlToolWindow = UIUtils.getPlantUmlToolWindow(project);
        RenderCacheItem displayedItem = plantUmlToolWindow.getDisplayedItem();
        if (displayedItem != null) {
            selectedSource = displayedItem.getSource();
        }

        return selectedSource;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (project != null) {
            e.getPresentation().setEnabled(UIUtils.hasAnyImage(project));
        }
    }
}
