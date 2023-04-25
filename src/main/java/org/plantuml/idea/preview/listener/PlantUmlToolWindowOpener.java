package org.plantuml.idea.preview.listener;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.lang.PlantUmlFileType;
import org.plantuml.idea.preview.editor.PlantUmlSplitEditor;
import org.plantuml.idea.preview.editor.SplitFileEditor;
import org.plantuml.idea.settings.PlantUmlSettings;
import org.plantuml.idea.util.UIUtils;

public class PlantUmlToolWindowOpener implements FileEditorManagerListener {

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
    }

    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        VirtualFile newFile = event.getNewFile();
        VirtualFile oldFile = event.getOldFile();
        FileEditor newEditor = event.getNewEditor();

        if (newEditor instanceof PlantUmlSplitEditor) {
            SplitFileEditor.SplitEditorLayout currentEditorLayout = ((PlantUmlSplitEditor) newEditor).getCurrentEditorLayout();
            if (currentEditorLayout != SplitFileEditor.SplitEditorLayout.FIRST) {
                return;
            }
        }

        ToolWindow window = getToolWindow(event);
        if (window != null && window.getType() == ToolWindowType.DOCKED && PlantUmlSettings.getInstance().isAutoHide()) {
            if (isPlantUml(newFile)) {
                if (!window.isVisible()) {
                    window.show(null);
                }
            } else if (isPlantUml(oldFile)) {
                if (window.isVisible()) {
                    window.hide(null);
                }
            }
        }
    }

    @Nullable
    private ToolWindow getToolWindow(FileEditorManagerEvent event) {
        return UIUtils.getToolWindow(event.getManager().getProject());
    }

    private boolean isPlantUml(VirtualFile file) {
        return file != null && PlantUmlFileType.INSTANCE.equals(file.getFileType());
    }

}
