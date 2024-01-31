package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.plantuml.idea.settings.PlantUmlSettings;

public class AutoHideToolWindowToggleAction extends MyToggleAction {

    @Override
    public boolean isSelected(AnActionEvent anActionEvent) {
        return PlantUmlSettings.getInstance().isAutoHide();
    }

    @Override
    public void setSelected(AnActionEvent anActionEvent, boolean b) {
        PlantUmlSettings.getInstance().setAutoHide(b);
    }
}
