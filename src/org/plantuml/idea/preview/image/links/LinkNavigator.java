package org.plantuml.idea.preview.image.links;

import com.intellij.find.EditorSearchSession;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderResult;
import org.plantuml.idea.settings.PlantUmlSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class LinkNavigator {

    private final RenderRequest renderRequest;
    private final RenderResult renderResult;
    public String lastText = "";
    public File lastFile = null;
    private int lastIndex = 0;
    private LocalFileSystem localFileSystem;
    private FileEditorManager fileEditorManager;

    public LinkNavigator(RenderRequest renderRequest, RenderResult renderResult, Project project) {
        this.renderRequest = renderRequest;
        this.renderResult = renderResult;
        localFileSystem = LocalFileSystem.getInstance();
        fileEditorManager = FileEditorManager.getInstance(project);
    }

    public void findNextSourceAndNavigate(String text) {
        boolean continuing = continuing(text);
        if (!continuing) {
            reset();
        }

        if (continuing) {
            if (renderResult.includedFilesContains(lastFile)) {
                if (navigateToIncludedFile(text)) {
                    return;
                }
            } else {
                if (navigateToEditor(text, renderRequest.getSourceFile())) {
                    return;
                }
                reset();
                if (navigateToIncludedFile(text)) {
                    return;
                }
            }
        }

        reset();

        if (navigateToEditor(text, renderRequest.getSourceFile())) {
            return;
        }

        if (navigateToIncludedFile(text)) {
            return;
        }

        reset();
    }

    private boolean continuing(String text) {
        return lastText.equals(text) &&
                (FileUtil.filesEqual(lastFile, renderRequest.getSourceFile())
                        || renderResult.includedFilesContains(lastFile)
                );
    }

    private void reset() {
        lastIndex = 0;
        lastFile = null;
        lastText = "";
    }

    private boolean navigateToIncludedFile(String text) {
        Map<File, Long> includedFiles = renderResult.getIncludedFiles();
        ArrayList<File> files = new ArrayList<>(includedFiles.keySet());
        int from = 0;
        if (lastFile != null) {
            from = files.indexOf(lastFile);
        }

        for (int j = from; j < files.size(); j++) {
            File file = files.get(j);
            if (navigateToEditor(text, file)) {
                return true;
            }
            lastIndex = 0;
        }
        return false;
    }

    private boolean navigateToEditor(String text, File file) {
        VirtualFile virtualFile = localFileSystem.findFileByPath(file.getAbsolutePath());
        if (virtualFile != null) {
            Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
            if (document != null) {
                String documentText = document.getText();
                int i = documentText.indexOf(text, lastIndex);
                if (i >= 0) {
                    return navigateToEditor(file, virtualFile, text, i);
                }
            }
        }
        return false;
    }

    private boolean navigateToEditor(File file, VirtualFile virtualFile, String text, int i) {
        FileEditor[] fileEditors = fileEditorManager.openFile(virtualFile, true, true);
        if (fileEditors.length != 0) {
            FileEditor fileEditor = fileEditors[0];
            if (fileEditor instanceof TextEditor) {
                Editor editor = ((TextEditor) fileEditor).getEditor();
                editor.getCaretModel().moveToOffset(i);
                editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
                editor.getSelectionModel().setSelection(i, i + text.length());

                if (PlantUmlSettings.getInstance().isLinkOpensSearchBar()) {
                    EditorSearchSession editorSearchSession = EditorSearchSession.get(editor);
                    if (editorSearchSession != null) {
                        if (!text.equals(editorSearchSession.getTextInField())) {
                            editorSearchSession.setTextInField(text);
                        }
                    } else {
                        AnAction find = ActionManager.getInstance().getAction("Find");
                        if (find != null) {
                            DataContext dataContext = DataManager.getInstance().getDataContext(editor.getComponent());
                            AnActionEvent anActionEvent = AnActionEvent.createFromDataContext("plantuml image", find.getTemplatePresentation(), dataContext);
                            find.actionPerformed(anActionEvent);
                        }
                    }
                }
            }
        }
        lastFile = file;
        lastText = text;
        lastIndex = i + text.length();
        return true;
    }

    public boolean openFile(File file) {
        if (file.exists()) {
            VirtualFile virtualFile = localFileSystem.findFileByPath(file.getAbsolutePath());
            if (virtualFile == null) {
                return false;
            }
            FileEditor[] fileEditors = fileEditorManager.openFile(virtualFile, true, true);
            return fileEditors.length > 0;
        }
        return false;
    }

    @Override
    public String toString() {
        return "LinkNavigator{" +
                "lastText='" + lastText + '\'' +
                ", lastFile=" + lastFile +
                ", lastIndex=" + lastIndex +
                '}';
    }
}
