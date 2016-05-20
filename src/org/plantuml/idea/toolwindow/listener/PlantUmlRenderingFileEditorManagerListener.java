package org.plantuml.idea.toolwindow.listener;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.util.UIUtils;

public class PlantUmlRenderingFileEditorManagerListener implements FileEditorManagerListener {
    private static Logger logger = Logger.getInstance(PlantUmlRenderingFileEditorManagerListener.class);

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        if (logger.isDebugEnabled()) {
            logger.debug("file opened " + file);
        }
    }

    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        if (logger.isDebugEnabled()) {
            logger.debug("file closed " + file);
        }
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        if (logger.isDebugEnabled()) {
            logger.debug("selection changed " + event);
        }
        UIUtils.renderPlantUmlToolWindowLater(event.getManager().getProject());
    }
}
