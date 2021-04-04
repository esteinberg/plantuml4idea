package org.plantuml.idea.action.save;

import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.util.ArrayUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.external.PlantUmlFacade;
import org.plantuml.idea.plantuml.ImageFormat;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;
import org.plantuml.idea.rendering.ImageItem;
import org.plantuml.idea.rendering.RenderCacheItem;
import org.plantuml.idea.rendering.RenderResult;
import org.plantuml.idea.rendering.RenderingType;
import org.plantuml.idea.settings.PlantUmlSettings;
import org.plantuml.idea.util.UIUtils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.plantuml.idea.util.UIUtils.notification;

/**
 * @author Eugene Steinberg
 */
public abstract class AbstractSaveDiagramAction extends DumbAwareAction {

    public static VirtualFile homeDir = null;
    public static final String FILENAME = "diagram";
    Logger logger = Logger.getInstance(SaveDiagramToFileAction.class);

    static {
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

        PlantUmlPreviewPanel previewPanel = UIUtils.getEditorPreviewOrToolWindowPanel(e);
        RenderCacheItem displayedItem = previewPanel.getDisplayedItem();
        PlantUmlSettings plantUmlSettings = PlantUmlSettings.getInstance();

        String selectedSource = displayedItem.getSource();
        File sourceFile = new File(displayedItem.getSourceFilePath());
        RenderResult renderResult = displayedItem.getRenderResult();
        boolean remote = renderResult.getStrategy() == RenderingType.REMOTE;

        if (StringUtils.isBlank(selectedSource)) {
            Notifications.Bus.notify(notification().createNotification("No PlantUML source code", MessageType.WARNING));
            return;
        }
        ImageFormat format = remote ? displayedItem.getRenderRequest().getFormat() : plantUmlSettings.getDefaultExportFileFormatEnum();
        String defaultExtension = format.name().toLowerCase();

        String[] extensions = remote ? new String[]{format.name().toLowerCase()} : getExtensions(defaultExtension);
        FileSaverDescriptor fsd = new FileSaverDescriptor("Save Diagram", "Please choose where to save diagram", extensions);

        VirtualFile baseDir = null;
        if (plantUmlSettings.isRememberLastExportDir() && plantUmlSettings.getLastExportDir() != null) {
            baseDir = LocalFileSystem.getInstance().findFileByPath(plantUmlSettings.getLastExportDir());
        }
        if (baseDir == null) {
            baseDir = LocalFileSystem.getInstance().findFileByIoFile(sourceFile.getParentFile());
        }
        if (baseDir == null) {
            if (project == null) {
                baseDir = homeDir;
            } else {
                baseDir = ProjectUtil.guessProjectDir(project);
            }
        }
        String defaultFileName = getDefaultFileName(e, project);

        final VirtualFileWrapper wrapper = FileChooserFactory.getInstance().createSaveFileDialog(fsd, project).save(baseDir, defaultFileName);

        if (wrapper != null) {
            try {
                File file = wrapper.getFile();

                if (plantUmlSettings.isRememberLastExportDir()) {
                    File parentDir = file.getParentFile();
                    if (parentDir != null && parentDir.exists()) {
                        plantUmlSettings.setLastExportDir(parentDir.getAbsolutePath());
                        logger.debug("lastDir set to ", parentDir.getAbsolutePath());
                    }
                } else {
                    plantUmlSettings.setLastExportDir(null);
                }

                String[] tokens = file.getAbsolutePath().split("\\.(?=[^\\.]+$)");
                String pathPrefix = tokens[0];
                String extension;

                if (tokens.length < 2) {
                    extension = defaultExtension;
                    file = new File(pathPrefix + "." + defaultExtension);
                } else {
                    extension = tokens[1];
                }

                ImageFormat imageFormat;
                try {
                    imageFormat = ImageFormat.valueOf(extension.toUpperCase());
                } catch (Exception ex) {
                    throw new IOException("Extension '" + extension + "' is not supported");
                }


                if (remote) {
                    if (displayedItem.getRenderRequest().getFormat() != imageFormat) {
                        throw new RuntimeException("wrong format");
                    }
                    if (renderResult.getPages() > 1) {
                        throw new RuntimeException("renderResult.getPages() > 1");
                    }
                    ImageItem imageItem = renderResult.getImageItem(0);
                    byte[] imageBytes = imageItem.getImageBytes();
                    PlantUmlFacade.get().save(file.getAbsolutePath(), imageBytes);
                } else {
                    PlantUmlFacade.get().renderAndSave(selectedSource, sourceFile,
                            imageFormat, file.getAbsolutePath(), pathPrefix,
                            UIUtils.getEditorPreviewOrToolWindowPanel(e).getZoom(), getPageNumber(e));

                }


            } catch (IOException e1) {
                String title = "Error Writing Diagram";
                String message = title + " to file:" + wrapper.getFile() + " : " + e1.toString();
                logger.warn(message);
                Messages.showErrorDialog(message, title);
            }
        }
    }

    @NotNull
    private String[] getExtensions(String defaultExtension) {
        String[] extensions;
        ImageFormat[] values = ImageFormat.values();
        extensions = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            extensions[i] = values[i].name().toLowerCase();
        }
        Arrays.sort(extensions);
        swapExtensions(extensions, defaultExtension);
        return extensions;
    }

    private void swapExtensions(String[] extensions, String extension) {
        for (int i = 0; i < extensions.length; i++) {
            String s = extensions[i];
            if (s.equals(extension)) {
                ArrayUtil.swap(extensions, 0, i);
                break;
            }
        }
    }

    protected int getPageNumber(AnActionEvent e) {
        return -1;
    }


    private String getDefaultFileName(AnActionEvent e, Project myProject) {
        String filename = null;

        PlantUmlSettings plantUmlSettings = PlantUmlSettings.getInstance();
        try {
            PlantUmlPreviewPanel previewPanel = UIUtils.getEditorPreviewOrToolWindowPanel(e);
            if (previewPanel != null) {
                RenderCacheItem displayedItem = previewPanel.getDisplayedItem();
                int selectedPage = previewPanel.getSelectedPage();
                ImageItem imageItem = displayedItem.getImageItem(selectedPage < 0 ? 0 : selectedPage);
                if (imageItem != null) {
                    String customFileName = imageItem.getCustomFileName();
                    if (customFileName != null) {
                        filename = sanitize(customFileName);
                    }
                    if (filename == null) {
                        filename = displayedItem.getFileNameWithoutExtension();
                    }
                    if (filename != null && plantUmlSettings.isUsePageTitles() && getPageNumber(e) >= 0) {
                        String sanitize = sanitize(imageItem.getTitle());
                        if (sanitize != null) {
                            filename = filename + "-" + sanitize;
                        }
                    }
                }
                if (filename == null) {
                    filename = displayedItem.getFileNameWithoutExtension();
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }

        if (filename == null) {
            filename = FILENAME;
        }

        String defaultExtension = plantUmlSettings.getDefaultExportFileFormatEnum().name().toLowerCase();
        if (SystemInfo.isMac && Registry.is("ide.mac.native.save.dialog") && !filename.endsWith("." + defaultExtension)) {
            filename += "." + defaultExtension;
        }
        return filename;
    }

    @Nullable
    private String sanitize(String filename) {
        if (filename != null && filename.startsWith("[") && filename.endsWith("]")) {
            filename = filename.substring(1, filename.length() - 1);
        }
        if (StringUtils.isBlank(filename)) {
            return null;
        }
        return FileUtil.sanitizeFileName(filename);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (project != null) {
            e.getPresentation().setEnabled(UIUtils.hasAnyImage(e));
        }
    }
}
