package org.plantuml.idea.toolwindow.listener;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.util.Alarm;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.rendering.LazyApplicationPoolExecutor;
import org.plantuml.idea.rendering.RenderCommand;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.plantuml.idea.toolwindow.image.links.Highlighter;
import org.plantuml.idea.util.UIUtils;

public class PlantUmlSelectionListener implements SelectionListener {

    private static Logger logger = Logger.getInstance(PlantUmlSelectionListener.class);

    private PlantUmlSettings settings;
    private Highlighter highlighter;

    public PlantUmlSelectionListener() {
        settings = PlantUmlSettings.getInstance();
        highlighter = new Highlighter();
    }

    @Override
    public void selectionChanged(@NotNull SelectionEvent e) {
        if (settings.isHighlightInImages()) {
            if (!e.getNewRange().equals(e.getOldRange())) {
                PlantUmlToolWindow plantUmlToolWindow = UIUtils.getPlantUmlToolWindow(e.getEditor().getProject());
                if (plantUmlToolWindow == null) {
                    return;
                }

                highlighter.highlightImages(plantUmlToolWindow, e.getEditor());
            }
        }
    }
}
