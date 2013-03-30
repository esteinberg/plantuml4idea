package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.ui.components.JBComboBoxLabel;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.util.UIUtils;

import javax.swing.*;

/**
 * Author: Eugene Steinberg
 * Date: 3/3/13
 */
public class SelectPageAction extends ComboBoxAction {
    private int numPages = 1;
    ComboBoxButton button;

    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent button) {
        this.button = (ComboBoxButton) button;
        DefaultActionGroup group = new DefaultActionGroup();

        for (int i = 0; i < numPages; i++) {
            group.add(new SetPageAction(i));
        }

        return group;
    }

    public void setNumPages(int numPages) {
        this.numPages = numPages;
    }

    public void setPage(int page) {
        if (button != null) {
            button.setText(getDisplayPage(page));
        }
    }

    private static String getDisplayPage(int page) {
        return Integer.toString(page + 1);
    }


    private class SetPageAction extends AnAction {
        private int page = 0;

        private SetPageAction(int page) {
            super(getDisplayPage(page));
            this.page = page;
        }

        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
            UIUtils.getToolWindow(anActionEvent.getProject()).setPage(page);
            setPage(page);

        }
    }
}
