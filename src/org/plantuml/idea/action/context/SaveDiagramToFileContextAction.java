package org.plantuml.idea.action.context;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.plantuml.idea.action.save.AbstractSaveDiagramAction;
import org.plantuml.idea.preview.image.ImageContainer;

public class SaveDiagramToFileContextAction extends AbstractSaveDiagramAction {

    public SaveDiagramToFileContextAction() {
        super("Save Diagram", null, AllIcons.Actions.Menu_saveall);
    }

    @Override
    protected int getPageNumber(AnActionEvent e) {
        ImageContainer data = (ImageContainer) e.getData(ImageContainer.CONTEXT_COMPONENT);
        return data.getPage();
    }

}
