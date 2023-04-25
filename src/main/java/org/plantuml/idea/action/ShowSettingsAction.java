package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;
import org.plantuml.idea.settings.PlantUmlSettingsPage;

public class ShowSettingsAction extends DumbAwareAction {
    @Override
    public void actionPerformed(AnActionEvent event) {
        ShowSettingsUtil.getInstance().editConfigurable(event.getProject(), "PlantUMLSettings2", new PlantUmlSettingsPage());
    }
}
