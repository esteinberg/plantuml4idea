package org.plantuml.idea.action.context;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import org.plantuml.idea.action.AbstractSaveDiagramAction;
import org.plantuml.idea.toolwindow.PlantUmlImageLabel;
import org.plantuml.idea.util.UIUtils;

public class SaveDiagramToFileContextAction extends AbstractSaveDiagramAction {

	public SaveDiagramToFileContextAction() {
		super("Save Current Diagram", "Save Current Diagram", AllIcons.Actions.Menu_saveall);
	}

	@Override
	protected int getPageNumber(AnActionEvent e) {
		PlantUmlImageLabel data = (PlantUmlImageLabel) e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
		return data.getPage();
	}

	@Override
	protected String getSource(Project project) {
		return UIUtils.getSelectedSourceWithCaret(FileEditorManager.getInstance(project));
	}
}
