package org.plantuml.idea.toolwindow.listener;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.rendering.LazyApplicationPoolExecutor;
import org.plantuml.idea.rendering.RenderCommand;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.plantuml.idea.toolwindow.image.links.Highlighter;
import org.plantuml.idea.util.UIUtils;

public class PlantUmlCaretListener implements CaretListener {
    private static Logger logger = Logger.getInstance(PlantUmlCaretListener.class);
    public FileDocumentManager instance = FileDocumentManager.getInstance();
    private PlantUmlSettings settings;
    private Highlighter highlighter;

    public PlantUmlCaretListener() {
        settings = PlantUmlSettings.getInstance();
        highlighter = new Highlighter();
    }

    @Override
    public void caretPositionChanged(final CaretEvent e) {
        if (instance.getFile(e.getEditor().getDocument()) == null) {
            return;//console            
        }
        logger.debug("caretPositionChanged");
        PlantUmlToolWindow plantUmlToolWindow = UIUtils.getPlantUmlToolWindow(e.getEditor().getProject());
        if (plantUmlToolWindow == null) {
            return;
        }

        if (settings.isAutoRender()) {
            if (plantUmlToolWindow != null) {
                plantUmlToolWindow.renderLater(LazyApplicationPoolExecutor.Delay.MAYBE_WITH_DELAY, RenderCommand.Reason.CARET);
            }
        }

        if (settings.isHighlightInImages()) {
            highlighter.highlightImages(plantUmlToolWindow, e.getEditor());
        }
    }

    @Override
    public void caretAdded(CaretEvent e) {
        if (instance.getFile(e.getEditor().getDocument()) == null) {
            return;//console            
        }
        logger.debug("caretAdded");

        PlantUmlToolWindow plantUmlToolWindow = UIUtils.getPlantUmlToolWindow(e.getEditor().getProject());
        if (plantUmlToolWindow == null) {
            return;
        }

        if (settings.isAutoRender()) {
            if (plantUmlToolWindow != null) {
                plantUmlToolWindow.renderLater(LazyApplicationPoolExecutor.Delay.MAYBE_WITH_DELAY, RenderCommand.Reason.CARET);
            }
        }

        if (settings.isHighlightInImages()) {
            highlighter.highlightImages(plantUmlToolWindow, e.getEditor());
        }
    }

    @Override
    public void caretRemoved(CaretEvent e) {
        // do nothing
    }
}
