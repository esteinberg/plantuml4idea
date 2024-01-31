package org.plantuml.idea.action.save;

import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
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
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.action.MyDumbAwareAction;
import org.plantuml.idea.external.PlantUmlFacade;
import org.plantuml.idea.plantuml.ImageFormat;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;
import org.plantuml.idea.preview.Zoom;
import org.plantuml.idea.preview.editor.PlantUmlSplitEditor;
import org.plantuml.idea.rendering.*;
import org.plantuml.idea.settings.PlantUmlSettings;
import org.plantuml.idea.util.UIUtils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.plantuml.idea.util.UIUtils.*;

/**
 * @author Eugene Steinberg
 */
public abstract class AbstractSaveDiagramAction extends MyDumbAwareAction {

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
        String selectedSource;
        File sourceFile;
        boolean remote;
        ImageFormat format;
        RenderResult renderResult = null;
        Zoom zoom;
        String defaultFileName;

        Project project = e.getProject();
        if (project == null) {
            return;
        }

        PlantUmlSettings plantUmlSettings = PlantUmlSettings.getInstance();
        PlantUmlPreviewPanel previewPanel = UIUtils.getEditorOrToolWindowPreview(e);
        if (previewPanel != null) {
            if (previewPanel.getDisplayedItem() == null) {
                previewPanel = null;
            }
        }

        if (previewPanel == null) {
            FileEditor editor = e.getData(PlatformDataKeys.FILE_EDITOR);
            if (!(editor instanceof PlantUmlSplitEditor)) {
                throw new RuntimeException("invalid editor " + editor);
            }
            final PlantUmlSplitEditor fileEditor = (PlantUmlSplitEditor) editor;
            VirtualFile file = editor.getFile();
            sourceFile = new File(file.getPath());
            selectedSource = getSelectedSourceWithCaret(FileEditorManager.getInstance(project), fileEditor.getEditor());
            remote = plantUmlSettings.isRemoteRendering();
            format = plantUmlSettings.getDefaultExportFileFormatEnum();
            zoom = new Zoom(100, plantUmlSettings);
            String filename = PlantUmlFacade.get().getFilename(project, selectedSource, file);
            defaultFileName = getDefaultFileName(e, project, null, editor, filename);
        } else {
            RenderCacheItem displayedItem = previewPanel.getDisplayedItem();
            renderResult = displayedItem.getRenderResult();

            selectedSource = displayedItem.getSource();
            sourceFile = new File(displayedItem.getSourceFilePath());
            remote = renderResult.getStrategy() == RenderingType.REMOTE;
            format = remote ? displayedItem.getRenderResult().getImageItem(0).getFormat() : plantUmlSettings.getDefaultExportFileFormatEnum();
            zoom = getEditorOrToolWindowPreview(e).getZoom();
            defaultFileName = getDefaultFileName(e, project, previewPanel, null, null);
        }

        String defaultExtension = format.name().toLowerCase();
        String[] extensions = remote ? getRemoteExtensions(format.name().toLowerCase()) : getExtensions(defaultExtension);

        if (StringUtils.isBlank(selectedSource)) {
            Notifications.Bus.notify(notification().createNotification("No PlantUML source code", MessageType.WARNING));
            return;
        }

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

        final VirtualFileWrapper wrapper = FileChooserFactory.getInstance().createSaveFileDialog(fsd, project).save(baseDir, defaultFileName);

        if (wrapper != null) {
            try {
                File saveTo = wrapper.getFile();

                if (plantUmlSettings.isRememberLastExportDir()) {
                    File parentDir = saveTo.getParentFile();
                    if (parentDir != null && parentDir.exists()) {
                        plantUmlSettings.setLastExportDir(parentDir.getAbsolutePath());
                        logger.debug("lastDir set to ", parentDir.getAbsolutePath());
                    }
                } else {
                    plantUmlSettings.setLastExportDir(null);
                }

                String[] tokens = saveTo.getAbsolutePath().split("\\.(?=[^\\.]+$)");
                String pathPrefix = tokens[0];
                String extension;

                if (tokens.length < 2) {
                    extension = defaultExtension;
                    saveTo = new File(pathPrefix + "." + defaultExtension);
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
                    if (renderResult == null || renderResult.getImageItem(0).getFormat() != imageFormat) {
                        renderResult = PlantUmlFacade.get().render(new RenderRequest(sourceFile.getAbsolutePath(), selectedSource, imageFormat, 0, zoom, -1, false, RenderCommand.Reason.REFRESH, project), null);
                    }

                    String path = saveTo.getAbsolutePath();

                    if (renderResult.getPages() > 1) {
                        throw new RuntimeException("renderResult.getPages() > 1");
                    }
                    ImageFormat realFormat = renderResult.getImageItem(0).getFormat();
                    if (realFormat != imageFormat) {
                        path = path + "." + realFormat.name().toLowerCase();
                    }
                    byte[] imageBytes = renderResult.getImageItem(0).getImageBytes();

                    PlantUmlFacade.get().save(path, imageBytes);
                } else {
                    PlantUmlFacade.get().renderAndSave(project, selectedSource, sourceFile,
                            imageFormat, saveTo.getAbsolutePath(), pathPrefix,
                            zoom, getPageNumber(e));

                }


            } catch (IOException e1) {
                String title = "Error Writing Diagram";
                String message = title + " to file:" + wrapper.getFile() + " : " + e1.toString();
                logger.warn(message);
                Messages.showErrorDialog(message, title);
            }
        }
    }

    private String[] getRemoteExtensions(String defaultExtension) {
        String[] extensions;
        ImageFormat[] values = new ImageFormat[]{ImageFormat.PNG, ImageFormat.SVG};
        extensions = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            extensions[i] = values[i].name().toLowerCase();
        }
        Arrays.sort(extensions);
        swapExtensions(extensions, defaultExtension);
        return extensions;
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


    private String getDefaultFileName(AnActionEvent e, Project myProject, PlantUmlPreviewPanel previewPanel, FileEditor editor, String customFilename) {
        String filename = null;

        PlantUmlSettings plantUmlSettings = PlantUmlSettings.getInstance();
        try {
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
            } else if (editor != null) {
                filename = customFilename;
                if (filename == null) {
                    filename = editor.getFile().getNameWithoutExtension();
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
        boolean enabled = false;
        if (project != null) {
            enabled = UIUtils.hasAnyImage(e);
        }
        e.getPresentation().setEnabled(enabled);
    }
}
