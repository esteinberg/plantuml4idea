package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.util.NlsActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.function.Supplier;

public abstract class MyDumbAwareAction extends DumbAwareAction {
    public MyDumbAwareAction() {
    }

    public MyDumbAwareAction(@Nullable Icon icon) {
        super(icon);
    }

    public MyDumbAwareAction(@Nullable @NlsActions.ActionText String text) {
        super(text);
    }

    public MyDumbAwareAction(@NotNull Supplier<@NlsActions.ActionText String> dynamicText) {
        super(dynamicText);
    }

    public MyDumbAwareAction(@Nullable @NlsActions.ActionText String text, @Nullable @NlsActions.ActionDescription String description, @Nullable Icon icon) {
        super(text, description, icon);
    }


    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
