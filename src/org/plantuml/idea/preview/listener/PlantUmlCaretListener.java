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
        PlantUmlPreviewPanel plantUmlPreview = UIUtils.getEditorPreviewOrToolWindowPanel(e.getEditor());
        if (plantUmlPreview == null) {
            return;
        }

        if (settings.isAutoRender()) {
            plantUmlPreview.processRequest(LazyApplicationPoolExecutor.Delay.MAYBE_WITH_DELAY, RenderCommand.Reason.CARET);
        }

        if (settings.isHighlightInImages()) {
            plantUmlPreview.highlightImages(e.getEditor());
        }
    }

    @Override
    public void caretAdded(CaretEvent e) {
        if (instance.getFile(e.getEditor().getDocument()) == null) {
            return;//console            
        }
        logger.debug("caretAdded");

        PlantUmlPreviewPanel previewPanel = UIUtils.getEditorPreviewOrToolWindowPanel(e.getEditor());
        if (previewPanel == null) {
            return;
        }

        if (settings.isAutoRender()) {
            previewPanel.processRequest(LazyApplicationPoolExecutor.Delay.MAYBE_WITH_DELAY, RenderCommand.Reason.CARET);
        }

        if (settings.isHighlightInImages()) {
            previewPanel.highlightImages(e.getEditor());
        }
    }

    @Override
    public void caretRemoved(CaretEvent e) {
        // do nothing
    }
}
