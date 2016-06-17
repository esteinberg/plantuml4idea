package org.plantuml.idea.toolwindow.listener;

import com.intellij.lang.Language;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowType;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.lang.PlantUmlLanguage;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
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
        return file != null && getFileLanguage(file) == PlantUmlLanguage.INSTANCE;
    }

    @Nullable
    public static Language getFileLanguage(@Nullable VirtualFile file) {
        if (file == null) return null;
        Language l = file instanceof LightVirtualFile ? ((LightVirtualFile) file).getLanguage() : null;
        return l != null ? l : getFileTypeLanguage(file.getFileType());
    }

    @Nullable
    public static Language getFileTypeLanguage(@Nullable FileType fileType) {
        return fileType instanceof LanguageFileType ? ((LanguageFileType) fileType).getLanguage() : null;
    }
  
}
