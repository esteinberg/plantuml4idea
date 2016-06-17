package org.plantuml.idea.action;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import org.plantuml.idea.util.UIUtils;

/**
 * @author Eugene Steinberg
 */
public class SaveDiagramToFileAction extends AbstractSaveDiagramAction {

    @Override
    protected String getSource(Project project) {
        return UIUtils.getSelectedSourceWithCaret(FileEditorManager.getInstance(project));
    }
}
