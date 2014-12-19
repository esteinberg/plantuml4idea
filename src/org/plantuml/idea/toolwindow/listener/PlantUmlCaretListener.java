package org.plantuml.idea.toolwindow.listener;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import org.plantuml.idea.util.UIUtils;

public class PlantUmlCaretListener implements CaretListener {
    private static Logger logger = Logger.getInstance(PlantUmlCaretListener.class);

    @Override
    public void caretPositionChanged(final CaretEvent e) {
        logger.debug("caretPositionChanged");
        UIUtils.renderPlantUmlToolWindowLater(e.getEditor().getProject());
    }

    @Override
    public void caretAdded(CaretEvent e) {
        logger.debug("caretAdded");
        UIUtils.renderPlantUmlToolWindowLater(e.getEditor().getProject());
    }

    @Override
    public void caretRemoved(CaretEvent e) {
        // do nothing
    }
}
