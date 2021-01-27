package org.plantuml.idea.toolwindow.listener;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.plantuml.idea.util.UIUtils;

public class PlantUmlSelectionListener implements SelectionListener {

    private static Logger logger = Logger.getInstance(PlantUmlSelectionListener.class);

    private PlantUmlSettings settings;

    public PlantUmlSelectionListener() {
        settings = PlantUmlSettings.getInstance();
    }

    @Override
    public void selectionChanged(@NotNull SelectionEvent e) {
        if (settings.isHighlightInImages()) {
            if (!e.getNewRange().equals(e.getOldRange())) {
                PlantUmlToolWindow plantUmlToolWindow = UIUtils.getPlantUmlToolWindow(e.getEditor().getProject());
                if (plantUmlToolWindow == null) {
                    return;
                }

                plantUmlToolWindow.highlightImages(e.getEditor());
            }
        }
    }
}
