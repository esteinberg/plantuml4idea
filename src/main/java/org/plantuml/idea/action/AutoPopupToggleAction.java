package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.plantuml.idea.settings.PlantUmlSettings;

public class AutoPopupToggleAction extends MyToggleAction {

    @Override
    public boolean isSelected(AnActionEvent anActionEvent) {
        return PlantUmlSettings.getInstance().isAutoComplete();
    }

    @Override
    public void setSelected(AnActionEvent anActionEvent, boolean b) {
        PlantUmlSettings.getInstance().setAutoComplete(b);
    }
}
