package org.plantuml.idea.action.test;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.external.Classloaders;

public class ClearCacheAction extends DumbAwareAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Classloaders.clear();
    }

}
