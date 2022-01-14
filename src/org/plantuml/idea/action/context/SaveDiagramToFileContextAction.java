package org.plantuml.idea.action.context;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.IconLoader;
import org.plantuml.idea.action.save.AbstractSaveDiagramAction;
import org.plantuml.idea.preview.image.ImageContainer;

import javax.swing.*;

public class SaveDiagramToFileContextAction extends AbstractSaveDiagramAction {
    public static final Icon ICON = IconLoader.getIcon("/actions/menu-saveall.svg", SaveDiagramToFileContextAction.class);

    public SaveDiagramToFileContextAction() {
        super("Save Diagram", null, ICON);
    }

    @Override
    protected int getPageNumber(AnActionEvent e) {
        ImageContainer data = (ImageContainer) e.getData(ImageContainer.CONTEXT_COMPONENT);
        return data.getPage();
    }

}
