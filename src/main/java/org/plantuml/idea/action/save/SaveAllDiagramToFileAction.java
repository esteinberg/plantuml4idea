package org.plantuml.idea.action.save;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;
import org.plantuml.idea.preview.editor.PlantUmlSplitEditor;
import org.plantuml.idea.util.UIUtils;

/**
 * @author Eugene Steinberg
 */
public class SaveAllDiagramToFileAction extends AbstractSaveDiagramAction {

	@Override
	public void update(@NotNull AnActionEvent e) {
		final Project project = e.getProject();
		boolean enabled = false;
		if (project != null) {
			PlantUmlPreviewPanel previewPanel = UIUtils.getEditorOrToolWindowPreview(e);
			if (previewPanel != null) {
				if (previewPanel.getDisplayedItem() != null) {
					enabled = true;
				} else {  //maybe redundant
					FileEditor editor = e.getData(PlatformDataKeys.FILE_EDITOR);
					if (editor instanceof PlantUmlSplitEditor) {
						enabled = true;
					}
				}
			}
		}

		e.getPresentation().setEnabled(enabled);
	}
}
