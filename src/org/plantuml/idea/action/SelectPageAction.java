package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;
import org.plantuml.idea.rendering.RenderCacheItem;

import javax.swing.*;

/**
 * Author: Eugene Steinberg
 * Date: 3/3/13
 */
public class SelectPageAction extends ComboBoxAction {
    private int numPages = 1;
    private PlantUmlPreviewPanel previewPanel;
    private String[] titles;

    public SelectPageAction(PlantUmlPreviewPanel previewPanel) {
        this.previewPanel = previewPanel;
    }

    @Override
    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent button) {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new SetPageAction(-1));
        for (int i = 0; i < numPages; i++) {
            group.add(new SetPageAction(i));
        }
        return group;
    }


    private String getDisplayPage(int page) {
        if (page == -1) {
            return "All Pages";
        }
        String pageNumber = Integer.toString(page + 1);
        String[] titles = this.titles;
        if (titles != null && titles.length > page && titles[page] != null) {
            return pageNumber + " - " + titles[page];
        } else {
            return pageNumber;
        }
    }


    private class SetPageAction extends DumbAwareAction {
        private int page = 0;

        private SetPageAction(int page) {
            super(getDisplayPage(page));
            this.page = page;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            previewPanel.setSelectedPage(page);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        numPages = previewPanel.getNumPages();
        RenderCacheItem displayedItem = previewPanel.getDisplayedItem();
        if (displayedItem != null) {
            this.titles = displayedItem.getTitles();
        }
        e.getPresentation().setText(getDisplayPage(previewPanel.getSelectedPage()));
        e.getPresentation().setEnabled(numPages > 1);
    }
}
