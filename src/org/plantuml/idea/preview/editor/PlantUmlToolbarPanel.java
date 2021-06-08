package org.plantuml.idea.preview.editor;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.util.ui.JBEmptyBorder;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.action.NextPageAction;
import org.plantuml.idea.action.SelectPageAction;
import org.plantuml.idea.preview.ExecutionStatusPanel;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;

import javax.swing.*;
import java.awt.*;


/**
 * inspired by {@link com.intellij.ui.EditorNotificationPanel}.
 */
public class PlantUmlToolbarPanel extends JPanel {

	public PlantUmlToolbarPanel(PlantUmlPreviewPanel editorPlantUmlPreviewPanel, @NotNull final JComponent targetComponentForActions) {
		super(new BorderLayout());

		JPanel myLinksPanel = new JPanel(new FlowLayout());
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
		panel.add("West", myLinksPanel);
		panel.setMinimumSize(new Dimension(0, 0));
		this.add("Center", panel);

		final ActionToolbar toolbar = createToolbarFromGroupId(editorPlantUmlPreviewPanel);
		toolbar.setTargetComponent(targetComponentForActions);
		panel.add(toolbar.getComponent());

	}

	@NotNull
	private ActionToolbar createToolbarFromGroupId(PlantUmlPreviewPanel editorPlantUmlPreviewPanel) {
		final ActionManager actionManager = ActionManager.getInstance();

		DefaultActionGroup newGroup = new DefaultActionGroup();
		newGroup.add(actionManager.getAction("PlantUML.EditorLayout"));
		newGroup.add(prepareToolbar(editorPlantUmlPreviewPanel, editorPlantUmlPreviewPanel.executionStatusPanel, actionManager));

		ActionToolbar actionToolbar = actionManager.createActionToolbar("plantuml4idea-editorPreview", newGroup, true);
		final ActionToolbarImpl editorToolbar = ((ActionToolbarImpl) actionToolbar);
		editorToolbar.setOpaque(false);
		editorToolbar.setBorder(new JBEmptyBorder(0, 2, 0, 2));

		return editorToolbar;
	}

	@NotNull
	public static DefaultActionGroup prepareToolbar(PlantUmlPreviewPanel previewPanel, ExecutionStatusPanel executionStatusPanel, ActionManager actionManager) {
		final DefaultActionGroup group = (DefaultActionGroup) actionManager.getAction("PlantUML.Toolbar");
		DefaultActionGroup newGroup = new DefaultActionGroup();
		AnAction[] childActionsOrStubs = group.getChildActionsOrStubs();
		for (int i = 0; i < childActionsOrStubs.length; i++) {
			AnAction stub = childActionsOrStubs[i];
			newGroup.add(stub);
			if (stub instanceof ActionStub) {
				if (((ActionStub) stub).getClassName().equals(NextPageAction.class.getName())) {
					newGroup.add(new SelectPageAction(previewPanel));
				}
			} else if (stub instanceof NextPageAction) {
				newGroup.add(new SelectPageAction(previewPanel));
			}
		}

		newGroup.add(executionStatusPanel);

		return newGroup;
	}

}
