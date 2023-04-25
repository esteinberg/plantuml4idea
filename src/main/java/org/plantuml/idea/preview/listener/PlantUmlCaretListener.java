package org.plantuml.idea.preview.listener;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;
import org.plantuml.idea.rendering.LazyApplicationPoolExecutor;
import org.plantuml.idea.rendering.RenderCommand;
import org.plantuml.idea.settings.PlantUmlSettings;
import org.plantuml.idea.util.UIUtils;

public class PlantUmlCaretListener implements CaretListener {
    private static Logger logger = Logger.getInstance(PlantUmlCaretListener.class);
    public FileDocumentManager instance = FileDocumentManager.getInstance();
    private PlantUmlSettings settings;

    public PlantUmlCaretListener() {
        settings = PlantUmlSettings.getInstance();
    }


    @Override
    public void caretPositionChanged(final CaretEvent e) {
        if (instance.getFile(e.getEditor().getDocument()) == null) {
            return;//console            
        }
        logger.debug("caretPositionChanged");

        for (PlantUmlPreviewPanel panel : UIUtils.getEligiblePreviews(e.getEditor())) {
            if (panel != null) {
                if (settings.isAutoRender()) {
                    panel.processRequest(LazyApplicationPoolExecutor.Delay.MAYBE_WITH_DELAY, RenderCommand.Reason.CARET);
                }

                if (settings.isHighlightInImages()) {
                    panel.highlightImages(e.getEditor());
                }
            }
        }

    }

    @Override
    public void caretAdded(CaretEvent e) {
        if (instance.getFile(e.getEditor().getDocument()) == null) {
            return;//console            
        }
        logger.debug("caretAdded");

        for (PlantUmlPreviewPanel panel : UIUtils.getEligiblePreviews(e.getEditor())) {
            if (panel != null) {
                if (settings.isAutoRender()) {
                    panel.processRequest(LazyApplicationPoolExecutor.Delay.MAYBE_WITH_DELAY, RenderCommand.Reason.CARET);
                }

                if (settings.isHighlightInImages()) {
                    panel.highlightImages(e.getEditor());
                }
            }
        }


    }

    @Override
    public void caretRemoved(CaretEvent e) {
        // do nothing
    }
}
