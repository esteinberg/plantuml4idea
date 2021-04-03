package org.plantuml.idea.util;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.plantuml.SourceExtractor;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;
import org.plantuml.idea.preview.editor.PlantUmlPreviewEditor;
import org.plantuml.idea.preview.editor.SplitFileEditor;
import org.plantuml.idea.preview.toolwindow.PlantUmlToolWindowFactory;
import org.plantuml.idea.rendering.LazyApplicationPoolExecutor;
import org.plantuml.idea.rendering.RenderCommand;

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

    @Nullable
    public static SplitFileEditor<?, ?> findSplitEditor(AnActionEvent e) {
        final FileEditor editor = e.getData(PlatformDataKeys.FILE_EDITOR);
        if (editor instanceof SplitFileEditor) {
            return (SplitFileEditor<?, ?>) editor;
        } else {
            return SplitFileEditor.PARENT_SPLIT_KEY.get(editor);
        }
    }

    public static String getSelectedSourceWithCaret(FileEditorManager instance) {
        String source = "";

        Editor selectedTextEditor = getSelectedTextEditor(instance);

        if (selectedTextEditor != null) {
            final Document document = selectedTextEditor.getDocument();
            int offset = selectedTextEditor.getCaretModel().getOffset();
            source = SourceExtractor.extractSource(document, offset);
        } else {
            Document selectedDocument = getSelectedDocument(instance);
            if (selectedDocument != null) {
                source = SourceExtractor.extractSource(selectedDocument, 0);
            }
        }
        return source;
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

    @Nullable
    public static VirtualFile getSelectedFile(FileEditorManager instance) {
        FileEditor selectedEditor = instance.getSelectedEditor();
        VirtualFile file = null;
        if (selectedEditor != null) {
            file = selectedEditor.getFile();
        }
        return file;
    }

    @Nullable
    public static Document getSelectedDocument(FileEditorManager instance) {
        FileEditor selectedEditor = instance.getSelectedEditor();
        Document document = null;
        if (selectedEditor != null) {
            VirtualFile file = selectedEditor.getFile();
            if (file != null) {
                document = FileDocumentManager.getInstance().getDocument(file);
            }
        }
        return document;
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

    public static PlantUmlPreviewPanel getPlantUmlPreviewPanel(AnActionEvent e) {
        final FileEditor editor = e.getData(PlatformDataKeys.FILE_EDITOR);
        PlantUmlPreviewPanel previewPanel = null;
        if (editor != null) {
            previewPanel = PlantUmlPreviewEditor.PLANTUML_PREVIEW_PANEL.get(editor);
        }
        if (previewPanel == null) {
            SplitFileEditor<?, ?> splitEditor = findSplitEditor(e);
            previewPanel = PlantUmlPreviewEditor.PLANTUML_PREVIEW_PANEL.get(splitEditor);
        }
        if (previewPanel == null) {
            previewPanel = getPlantUmlToolWindow(e.getProject());
        }
        return previewPanel;
    }

    @Deprecated
    @Nullable
    public static PlantUmlPreviewPanel getPlantUmlToolWindow(Project project) {
        if (project == null) {
            return null;
        }
        PlantUmlPreviewPanel result = null;
        ToolWindow toolWindow = getToolWindow(project);
        if (toolWindow != null) {
            result = getPlantUmlToolWindow(toolWindow);
        }
        return result;
    }

    @Nullable
    public static PlantUmlPreviewPanel getPlantUmlToolWindow(@NotNull ToolWindow toolWindow) {
        PlantUmlPreviewPanel result = null;
        Content[] contents = toolWindow.getContentManager().getContents();
        if (contents.length > 0) {
            JComponent component = contents[0].getComponent();
            //it can be JLabel "Initializing..."
            if (component instanceof PlantUmlPreviewPanel) {
                result = (PlantUmlPreviewPanel) component;
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

    public static void renderToolWindowAndEditorPreviewLater(AnActionEvent anActionEvent, @Nullable Project project, LazyApplicationPoolExecutor.Delay delay, RenderCommand.Reason reason) {
        if (project == null) return;

        PlantUmlPreviewPanel toolWindow = renderToolWindowLater(anActionEvent, project, delay, reason);

        if (anActionEvent != null) {
            PlantUmlPreviewPanel previewPanel = getPlantUmlPreviewPanel(anActionEvent);
            //noinspection ObjectEquality
            if (previewPanel != null && previewPanel != toolWindow) {
                previewPanel.processRequest(delay, reason);
            }
        }
    }

    private static PlantUmlPreviewPanel renderToolWindowLater(AnActionEvent anActionEvent, @Nullable Project project, LazyApplicationPoolExecutor.Delay delay, RenderCommand.Reason reason) {
        if (project == null) return null;

        ToolWindow toolWindow = getToolWindow(project);
        if (toolWindow == null || !toolWindow.isVisible()) {
            return null;
        }

        PlantUmlPreviewPanel previewPanel = getPlantUmlToolWindow(toolWindow);

        if (previewPanel != null) {
            previewPanel.processRequest(delay, reason);
        }
        return previewPanel;
    }

    public static boolean hasAnyImage(AnActionEvent actionEvent) {
        PlantUmlPreviewPanel previewPanel = getPlantUmlPreviewPanel(actionEvent);
        boolean hasAnyImage = false;
        if (previewPanel != null) {
            hasAnyImage = previewPanel.getNumPages() > 0;
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


    @Nullable
    public static PlantUmlPreviewPanel getEditorPreviewOrToolWindowPanel(Editor editor) {
        PlantUmlPreviewEditor userData = editor.getUserData(PlantUmlPreviewEditor.PLANTUML_PREVIEW);
        return getPlantUmlPreviewPanel(userData, editor.getProject());
    }

    @Nullable
    public static PlantUmlPreviewPanel getPlantUmlPreviewPanel(UserDataHolder userData, @Nullable Project project) {
        PlantUmlPreviewPanel plantUmlPreview = null;
        if (userData != null) {
            plantUmlPreview = userData.getUserData(PlantUmlPreviewEditor.PLANTUML_PREVIEW_PANEL);
        }
        if (plantUmlPreview == null || !plantUmlPreview.isVisible()) {
            plantUmlPreview = getPlantUmlToolWindow(project);
        }
        if (plantUmlPreview == null || !plantUmlPreview.isVisible()) {
            return null;
        }
        return plantUmlPreview;
    }
}
