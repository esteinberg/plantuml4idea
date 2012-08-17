package org.plantuml.idea.action;

import com.intellij.openapi.project.Project;
import org.plantuml.idea.util.UIUtils;

/**
 * @author Eugene Steinberg
 */
public class SaveAllDiagramToFileAction extends AbstractSaveDiagramAction {

    @Override
    protected String getSource(Project project) {
        return UIUtils.getSelectedSource(project);
    }
}
