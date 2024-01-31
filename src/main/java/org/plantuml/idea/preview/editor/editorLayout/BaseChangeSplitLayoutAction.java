package org.plantuml.idea.preview.editor.editorLayout;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Toggleable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.action.MyDumbAwareAction;
import org.plantuml.idea.preview.editor.SplitFileEditor;
import org.plantuml.idea.settings.PlantUmlSettings;
import org.plantuml.idea.util.UIUtils;

abstract class BaseChangeSplitLayoutAction extends MyDumbAwareAction implements Toggleable {
  @Nullable
  private final SplitFileEditor.SplitEditorLayout myLayoutToSet;

  protected BaseChangeSplitLayoutAction(@Nullable SplitFileEditor.SplitEditorLayout layoutToSet) {
    myLayoutToSet = layoutToSet;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    final SplitFileEditor<?, ?> splitFileEditor = UIUtils.findSplitEditor(e);

    if (splitFileEditor != null) {
      if (myLayoutToSet == null) {
        splitFileEditor.triggerLayoutChange(true);
      } else {
        splitFileEditor.triggerLayoutChange(myLayoutToSet, true);
        PlantUmlSettings.getInstance().getPreviewSettings().setSplitEditorLayout(myLayoutToSet);
        Toggleable.setSelected(e.getPresentation(), true);
      }
    }
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    final SplitFileEditor<?, ?> splitFileEditor = UIUtils.findSplitEditor(e);
    e.getPresentation().setEnabledAndVisible(splitFileEditor != null);

    if (myLayoutToSet != null && splitFileEditor != null) {
      Toggleable.setSelected(e.getPresentation(), splitFileEditor.getCurrentEditorLayout() == myLayoutToSet);
    }
  }
}
