package org.plantuml.idea.action.context;

import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.plantuml.idea.external.PlantUmlFacade;
import org.plantuml.idea.plantuml.SourceExtractor;

public class ExtractEmbeddedSourcesFromPNGAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        if (file == null) {
            return;
        }
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        //Extract source from PNG-metadata
        String extracted = PlantUmlFacade.get().extractEmbeddedSourceFromImage(file);

        if (extracted != null) {
            // Remove other code after @enduml
            extracted = SourceExtractor.extractSource(extracted, 0);
            // Open extracted source in new scratch file
            VirtualFile scratchFile = ScratchRootType.getInstance().createScratchFile(project, file.getNameWithoutExtension() + ".puml", null, extracted);
            if (scratchFile != null) {
                FileEditorManager.getInstance(project).openFile(scratchFile, true);
            }
        }
    }
}
