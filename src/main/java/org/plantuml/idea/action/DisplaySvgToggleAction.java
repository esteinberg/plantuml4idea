package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.plantuml.idea.rendering.LazyApplicationPoolExecutor;
import org.plantuml.idea.rendering.RenderCommand;
import org.plantuml.idea.settings.PlantUmlSettings;
import org.plantuml.idea.util.UIUtils;

public class DisplaySvgToggleAction extends MyToggleAction {

    @Override
    public boolean isSelected(AnActionEvent anActionEvent) {
        return PlantUmlSettings.getInstance().isDisplaySvg();
    }

    @Override
    public void setSelected(AnActionEvent anActionEvent, boolean b) {
        PlantUmlSettings.getInstance().setDisplaySvg(b);
        PlantUmlSettings.SettingsChangedListener.settingsChanged();
        UIUtils.renderToolWindowAndEditorPreview(anActionEvent, LazyApplicationPoolExecutor.Delay.NOW, RenderCommand.Reason.REFRESH);
    }
}
