package org.plantuml.idea.preview.editor.editorLayout;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Toggleable;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.preview.editor.SplitFileEditor;
import org.plantuml.idea.settings.PlantUmlSettings;
import org.plantuml.idea.util.UIUtils;

public class VerticalSplitLayoutChangeAction extends AnAction implements DumbAware, Toggleable {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final SplitFileEditor<?, ?> splitFileEditor = UIUtils.findSplitEditor(e);

        if (splitFileEditor != null) {
            boolean newSplit = !splitFileEditor.isCurrentVerticalSplitOption();
            splitFileEditor.triggerSplitOrientationChange(newSplit);
            PlantUmlSettings.getInstance().getPreviewSettings().setVerticalSplit(newSplit);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        final SplitFileEditor<?, ?> splitFileEditor = UIUtils.findSplitEditor(e);
        e.getPresentation().setEnabledAndVisible(splitFileEditor != null);

        if (splitFileEditor != null) {
            Toggleable.setSelected(e.getPresentation(), !splitFileEditor.isCurrentVerticalSplitOption());
        }
    }
}
