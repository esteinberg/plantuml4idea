package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.plantuml.idea.util.UIUtils;

import javax.swing.*;

/**
 * Author: Eugene Steinberg
 * Date: 3/3/13
 */
public class SelectPageAction extends ComboBoxAction {
    private int numPages = 1;
    ComboBoxButton button;

    @Override
    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent button) {
        this.button = (ComboBoxButton) button;
        DefaultActionGroup group = new DefaultActionGroup();

        group.add(new SetPageAction(-1));
        for (int i = 0; i < numPages; i++) {
            group.add(new SetPageAction(i));
        }

        return group;
    }


    private static String getDisplayPage(int page) {
        if (page == -1) {
            return "All Pages";
        }
        return Integer.toString(page + 1);
    }


    private class SetPageAction extends DumbAwareAction {
        private int page = 0;

        private SetPageAction(int page) {
            super(getDisplayPage(page));
            this.page = page;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            final Project project = anActionEvent.getProject();
            if (project != null) {
                PlantUmlToolWindow plantUmlToolWindow = UIUtils.getPlantUmlToolWindow(project);

                if (plantUmlToolWindow != null)
                    plantUmlToolWindow.setPage(page);
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (project != null) {
            PlantUmlToolWindow plantUmlToolWindow = UIUtils.getPlantUmlToolWindow(project);
            if (plantUmlToolWindow != null) {
                numPages = plantUmlToolWindow.getNumPages();
                e.getPresentation().setText(getDisplayPage(plantUmlToolWindow.getPage()));
                e.getPresentation().setEnabled(numPages > 1);
            }
        }
    }
}
