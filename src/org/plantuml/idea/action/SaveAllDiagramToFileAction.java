package org.plantuml.idea.action;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import org.plantuml.idea.util.UIUtils;

/**
 * @author Eugene Steinberg
 */
public class SaveAllDiagramToFileAction extends AbstractSaveDiagramAction {

    @Override
    protected String getDisplayedSource(Project project) {
        return UIUtils.getSelectedSource(FileEditorManager.getInstance(project));
    }
}
