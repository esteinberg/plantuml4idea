package org.plantuml.idea.preview.editor;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.util.ui.JBEmptyBorder;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;

import javax.swing.*;
import java.awt.*;


/**
 * inspired by {@link com.intellij.ui.EditorNotificationPanel}.
 */
public class PlantUmlToolbarPanel extends JPanel {

  private static final String ACTION_GROUP_ID = "PlantUML.EditorToolbar";

  public PlantUmlToolbarPanel(PlantUmlPreviewPanel editorPlantUmlPreviewPanel, @NotNull final JComponent targetComponentForActions) {
    super(new BorderLayout());

    JPanel myLinksPanel = new JPanel(new FlowLayout());
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
    panel.add("West", myLinksPanel);
    panel.setMinimumSize(new Dimension(0, 0));
    this.add("Center", panel);

    final ActionToolbar toolbar = createToolbarFromGroupId(editorPlantUmlPreviewPanel, ACTION_GROUP_ID);
    toolbar.setTargetComponent(targetComponentForActions);
    panel.add(toolbar.getComponent());

  }

  @NotNull
  private ActionToolbar createToolbarFromGroupId(PlantUmlPreviewPanel editorPlantUmlPreviewPanel, @NotNull String groupId) {
    final ActionManager actionManager = ActionManager.getInstance();

    if (!actionManager.isGroup(groupId)) {
      throw new IllegalStateException(groupId + " should have been a group");
    }

    DefaultActionGroup toolbarGroup = new DefaultActionGroup();

    final DefaultActionGroup group = (DefaultActionGroup) actionManager.getAction(groupId);
    AnAction[] children = group.getChildren(null);

    for (AnAction child : children) {
      toolbarGroup.addAction(child);
    }
    toolbarGroup.add(editorPlantUmlPreviewPanel.executionStatusPanel);

    final ActionToolbarImpl editorToolbar =
            ((ActionToolbarImpl) actionManager.createActionToolbar(ActionPlaces.EDITOR_TOOLBAR, toolbarGroup, true));
    editorToolbar.setOpaque(false);
    editorToolbar.setBorder(new JBEmptyBorder(0, 2, 0, 2));

    return editorToolbar;
  }

}
