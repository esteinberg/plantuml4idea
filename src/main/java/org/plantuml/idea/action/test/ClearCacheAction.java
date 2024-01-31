package org.plantuml.idea.action.test;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.action.MyDumbAwareAction;
import org.plantuml.idea.external.Classloaders;

public class ClearCacheAction extends MyDumbAwareAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Classloaders.clear();
    }

}
