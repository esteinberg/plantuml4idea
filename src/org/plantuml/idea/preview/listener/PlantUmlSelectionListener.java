package org.plantuml.idea.preview.listener;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;
import org.plantuml.idea.settings.PlantUmlSettings;
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
                Editor editor = e.getEditor();
                PlantUmlPreviewPanel previewPanel = UIUtils.getEditorPreviewOrToolWindowPanel(editor);
                if (previewPanel == null) {
                    return;
                }

                previewPanel.highlightImages(e.getEditor());
            }
        }
    }
}
