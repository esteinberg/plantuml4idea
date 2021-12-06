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
import java.util.ArrayList;
import java.util.List;

/**
 * @author Eugene Steinberg
 */
public class UIUtils {
    private static Logger logger = Logger.getInstance(UIUtils.class);

    public static NotificationGroup notification() {
        return NotificationGroupManager.getInstance().getNotificationGroup("PlantUML integration plugin");
    }

    public static String getSelectedSourceWithCaret(FileEditorManager instance, @Nullable PlantUmlPreviewEditor fileEditor) {
        return getSelectedSourceWithCaret(instance, getSelectedTextEditor(instance, fileEditor));
    }

    public static String getSelectedSourceWithCaret(FileEditorManager instance, @Nullable Editor selectedTextEditor) {
        String source = "";
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
    public static Editor getSelectedTextEditor(FileEditorManager instance, @Nullable PlantUmlPreviewEditor plantUmlPreviewEditor) {
        Editor selectedTextEditor = null;
        if (plantUmlPreviewEditor != null) {
            selectedTextEditor = plantUmlPreviewEditor.getEditor();
        }

        if (selectedTextEditor == null) {
            selectedTextEditor = instance.getSelectedTextEditor();
        }

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

    public static PlantUmlPreviewPanel getEditorOrToolWindowPreview(AnActionEvent e) {
        final FileEditor fileEditor = e.getData(PlatformDataKeys.FILE_EDITOR);
        PlantUmlPreviewPanel panel = getEditorPlantUmlPreviewPanel(fileEditor, true);
        if (panel == null || !panel.isPreviewVisible()) {
            panel = getToolWindowPreview(e.getProject());
        }
        return panel;
    }

    public static List<PlantUmlPreviewPanel> getEligiblePreviews(Editor... editors) {
        List<PlantUmlPreviewPanel> panels = new ArrayList<>();
        Project project = null;
        for (Editor editor : editors) {
            project = editor.getProject();
            panels.add(getEditorPlantUmlPreviewPanel(editor.getUserData(PlantUmlPreviewEditor.PLANTUML_PREVIEW), true));
        }
        if (project != null) {
            panels.add(getToolWindowPreview(project));
        }
        return panels;
    }


    public static List<PlantUmlPreviewPanel> getEligiblePreviews(@Nullable FileEditor fileEditor, @Nullable Project project) {
        ArrayList<PlantUmlPreviewPanel> panels = new ArrayList<>();
        panels.add(getEditorPlantUmlPreviewPanel(fileEditor, true));
        panels.add(getToolWindowPreview(project));
        return panels;
    }

    @Nullable
    public static PlantUmlPreviewPanel getEditorPlantUmlPreviewPanel(@Nullable FileEditor fileEditor, boolean onlyVisiblePreview) {
        if (fileEditor == null) {
            return null;
        }
        PlantUmlPreviewPanel previewPanel = PlantUmlPreviewEditor.PLANTUML_PREVIEW_PANEL.get(fileEditor);
        if (previewPanel == null) {
            SplitFileEditor<?, ?> splitEditor = findSplitEditor(fileEditor);
            previewPanel = PlantUmlPreviewEditor.PLANTUML_PREVIEW_PANEL.get(splitEditor);
        }
        if (previewPanel == null || (onlyVisiblePreview && !previewPanel.isPreviewVisible())) {
            return null;
        }
        return previewPanel;
    }

    @Nullable
    public static PlantUmlPreviewPanel getToolWindowPreview(@Nullable Project project) {
        PlantUmlPreviewPanel previewPanel = null;
        if (project != null) {
            ToolWindow toolWindow = getToolWindow(project);
            if (toolWindow != null && toolWindow.isVisible()) {
                previewPanel = getToolWindowPreviewPanel(toolWindow);
            }
        }
        if (previewPanel == null || !previewPanel.isPreviewVisible()) {
            return null;
        }
        return previewPanel;
    }

    @Nullable
    public static PlantUmlPreviewPanel getToolWindowPreviewPanel(@NotNull ToolWindow toolWindow) {
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
    public static SplitFileEditor<?, ?> findSplitEditor(AnActionEvent e) {
        final FileEditor editor = e.getData(PlatformDataKeys.FILE_EDITOR);
        return findSplitEditor(editor);
    }

    @Nullable
    public static SplitFileEditor<? extends TextEditor, ? extends FileEditor> findSplitEditor(FileEditor editor) {
        if (editor instanceof SplitFileEditor) {
            return (SplitFileEditor<?, ?>) editor;
        } else {
            return SplitFileEditor.PARENT_SPLIT_KEY.get(editor);
        }
    }


    @Nullable
    public static ToolWindow getToolWindow(@NotNull Project project) {
        ToolWindowManager instance = ToolWindowManager.getInstance(project);
        if (instance == null) {
            return null;
        }
        return instance.getToolWindow(PlantUmlToolWindowFactory.ID);
    }

    public static void renderToolWindowAndEditorPreview(AnActionEvent e, LazyApplicationPoolExecutor.Delay delay, RenderCommand.Reason reason) {
        final FileEditor fileEditor = e.getData(PlatformDataKeys.FILE_EDITOR);
        List<PlantUmlPreviewPanel> panels = getEligiblePreviews(fileEditor, e.getProject());
        for (PlantUmlPreviewPanel panel : panels) {
            if (panel != null) {
                panel.processRequest(delay, reason);
            }
        }
    }

    public static boolean hasAnyImage(AnActionEvent actionEvent) {
        PlantUmlPreviewPanel previewPanel = getEditorOrToolWindowPreview(actionEvent);
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

}
