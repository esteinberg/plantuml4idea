package org.plantuml.idea.util;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.plantuml.SourceExtractor;
import org.plantuml.idea.rendering.LazyApplicationPoolExecutor;
import org.plantuml.idea.rendering.RenderCommand;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.plantuml.idea.toolwindow.PlantUmlToolWindowFactory;

import javax.swing.*;
import java.io.File;

/**
 * @author Eugene Steinberg
 */
public class UIUtils {
    private static Logger logger = Logger.getInstance(UIUtils.class);

    public static NotificationGroup notification() {
        return NotificationGroupManager.getInstance().getNotificationGroup("PlantUML integration plugin");
    }

    public static String getSelectedSourceWithCaret(FileEditorManager instance) {
        String source = "";

        Editor selectedTextEditor = getSelectedTextEditor(instance);

        if (selectedTextEditor != null) {
            final Document document = selectedTextEditor.getDocument();
            int offset = selectedTextEditor.getCaretModel().getOffset();
            source = SourceExtractor.extractSource(document.getText(), offset);
        }
        return source;
    }

    public static VirtualFile getSelectedSourceFile(Project project) {
        FileEditorManager instance = FileEditorManager.getInstance(project);
        Editor selectedTextEditor = getSelectedTextEditor(instance);
        if (selectedTextEditor != null) {
            final Document document = selectedTextEditor.getDocument();
            return FileDocumentManager.getInstance().getFile(document);
        }
        return null;
    }


    /**
     * FileEditorManager#getSelectedTextEditor is not good enough, returns null for *.rst in PyCharm (TextEditorWithPreview)
     */
    @Nullable
    public static Editor getSelectedTextEditor(FileEditorManager instance) {
        Editor selectedTextEditor = instance.getSelectedTextEditor();

        if (selectedTextEditor == null) {
            FileEditor selectedEditor = instance.getSelectedEditor();
            if (selectedEditor != null) {
                FileEditorLocation location = selectedEditor.getCurrentLocation();
                if (location instanceof TextEditorLocation) {
                    TextEditorLocation currentLocation = (TextEditorLocation) location;
                    FileEditor fileEditor = currentLocation.getEditor();
                    if (fileEditor instanceof TextEditor) {
                        TextEditor textEditor = (TextEditor) fileEditor;
                        selectedTextEditor = textEditor.getEditor();
                    }
                }
            }
        }
        return selectedTextEditor;
    }

    public static String getSelectedSource(FileEditorManager instance) {
        String source = "";
        Editor selectedTextEditor = getSelectedTextEditor(instance);
        if (selectedTextEditor != null) {
            final Document document = selectedTextEditor.getDocument();
            source = document.getText();
        }
        return source;
    }


    @Nullable
    public static VirtualFile getSelectedFile(FileEditorManager instance, FileDocumentManager fileDocumentManager) {
        Editor selectedTextEditor = getSelectedTextEditor(instance);
        VirtualFile file = null;
        if (selectedTextEditor != null) {
            final Document document = selectedTextEditor.getDocument();
            file = fileDocumentManager.getFile(document);
        }
        return file;
    }

    @Nullable
    public static File getSelectedDir(FileEditorManager instance, FileDocumentManager fileDocumentManager) {
        Editor selectedTextEditor = getSelectedTextEditor(instance);
        File baseDir = null;
        if (selectedTextEditor != null) {

            final Document document = selectedTextEditor.getDocument();
            final VirtualFile file = fileDocumentManager.getFile(document);
            baseDir = getParent(file);
        }
        return baseDir;
    }

    @Nullable
    public static File getParent(@Nullable VirtualFile file) {
        File baseDir = null;
        if (file != null) {
            VirtualFile parent = file.getParent();
            if (parent != null && parent.isDirectory()) {
                baseDir = new File(parent.getPath());
            }
        }
        return baseDir;
    }

    @Nullable
    public static PlantUmlToolWindow getPlantUmlToolWindow(Project project) {
        if (project == null) {
            return null;
        }
        PlantUmlToolWindow result = null;
        ToolWindow toolWindow = getToolWindow(project);
        if (toolWindow != null) {
            result = getPlantUmlToolWindow(toolWindow);
        }
        return result;
    }

    @Nullable
    public static PlantUmlToolWindow getPlantUmlToolWindow(@NotNull ToolWindow toolWindow) {
        PlantUmlToolWindow result = null;
        Content[] contents = toolWindow.getContentManager().getContents();
        if (contents.length > 0) {
            JComponent component = contents[0].getComponent();
            //it can be JLabel "Initializing..."
            if (component instanceof PlantUmlToolWindow) {
                result = (PlantUmlToolWindow) component;
            }
        }
        return result;
    }


    @Nullable
    public static ToolWindow getToolWindow(@NotNull Project project) {
        ToolWindowManager instance = ToolWindowManager.getInstance(project);
        if (instance == null) {
            return null;
        }
        return instance.getToolWindow(PlantUmlToolWindowFactory.ID);
    }

    public static void renderPlantUmlToolWindowLater(@Nullable Project project, LazyApplicationPoolExecutor.Delay delay, RenderCommand.Reason reason) {
        if (project == null) return;

        ToolWindow toolWindow = getToolWindow(project);
        if (toolWindow == null || !toolWindow.isVisible()) {
            return;
        }

        PlantUmlToolWindow plantUmlToolWindow = getPlantUmlToolWindow(toolWindow);

        if (plantUmlToolWindow != null) {
            plantUmlToolWindow.processRequest(delay, reason);
        }
    }

    public static boolean hasAnyImage(Project project) {
        PlantUmlToolWindow plantUmlToolWindow = getPlantUmlToolWindow(project);
        boolean hasAnyImage = false;
        if (plantUmlToolWindow != null) {
            hasAnyImage = plantUmlToolWindow.getNumPages() > 0;
        }
        return hasAnyImage;
    }


    @Nullable
    public static File getParent(File file) {
        if (file.exists()) {
            File parentFile = file.getParentFile();
            if (parentFile != null && parentFile.isDirectory()) {
                return parentFile.getAbsoluteFile();
            }
        }
        return null;
    }


}
