package org.plantuml.idea.toolwindow;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.lang.PlantUmlFileType;

public class PlantUmlToolWindowAutoOpener implements ProjectComponent {

    private static final Logger logger = Logger.getInstance(PlantUmlToolWindowAutoOpener.class);
    private static final PlantUmlFileManagerListener listener = new PlantUmlFileManagerListener();

    private Project project;

    public PlantUmlToolWindowAutoOpener(Project project) {
        this.project = project;
    }

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {
    }

    @NotNull
    public String getComponentName() {
        return "PlantUmlToolWindowAutoOpener";
    }

    @Override
    public void projectOpened() {
        logger.debug("Registering listeners");
        project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, listener);
    }

    @Override
    public void projectClosed() {
    }

    private static class PlantUmlFileManagerListener implements FileEditorManagerListener {

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
            if (isPlantUml(newFile)) {
                ToolWindow window = getToolWindow(event);
                if (window != null && !window.isVisible()) {
                    window.show(null);
                }
            } else if (isPlantUml(oldFile)) {
                ToolWindow window = getToolWindow(event);
                if (window != null && window.isVisible()) {
                    window.hide(null);
                }
            }
        }

        @Nullable
        private ToolWindow getToolWindow(FileEditorManagerEvent event) {
            return ToolWindowManager.getInstance(event.getManager().getProject()).getToolWindow(
                    PlantUmlToolWindowFactory.ID);
        }

        private boolean isPlantUml(VirtualFile file) {
            return file != null && file.getFileType() == PlantUmlFileType.PLANTUML_FILE_TYPE;
        }
    }
}
