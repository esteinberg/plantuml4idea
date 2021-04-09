package org.plantuml.idea.preview.listener;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;
import org.plantuml.idea.rendering.LazyApplicationPoolExecutor;
import org.plantuml.idea.rendering.RenderCommand;
import org.plantuml.idea.settings.PlantUmlSettings;
import org.plantuml.idea.util.UIUtils;

public class PlantUmlRenderingFileEditorManagerListener implements FileEditorManagerListener {
    private static Logger logger = Logger.getInstance(PlantUmlRenderingFileEditorManagerListener.class);
    private PlantUmlSettings settings;

    public PlantUmlRenderingFileEditorManagerListener() {
        settings = PlantUmlSettings.getInstance();
    }

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        logger.debug("file opened ", file);
    }

    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        if (logger.isDebugEnabled()) {
            logger.debug("file closed ", file);
        }
    }

    /**
     * tab switch
     */
    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        logger.debug("selection changed ", event);
        if (settings.isAutoRender()) {
            for (PlantUmlPreviewPanel panel : UIUtils.getEligiblePreviews(event.getNewEditor(), event.getManager().getProject())) {
                if (panel != null) {
                    panel.processRequest(LazyApplicationPoolExecutor.Delay.NOW, RenderCommand.Reason.FILE_SWITCHED);
                }
            }
        }
    }
}
