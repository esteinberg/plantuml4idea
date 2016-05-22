package org.plantuml.idea.toolwindow.listener;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import org.plantuml.idea.rendering.LazyApplicationPoolExecutor;
import org.plantuml.idea.util.UIUtils;

public class PlantUmlCaretListener implements CaretListener {
    private static Logger logger = Logger.getInstance(PlantUmlCaretListener.class);
    public FileDocumentManager instance = FileDocumentManager.getInstance();

    @Override
    public void caretPositionChanged(final CaretEvent e) {
        if (instance.getFile(e.getEditor().getDocument()) == null) {
            return;//console            
        }
        logger.debug("caretPositionChanged");
        UIUtils.renderPlantUmlToolWindowLater(e.getEditor().getProject(), LazyApplicationPoolExecutor.Delay.POST_DELAY);
    }

    @Override
    public void caretAdded(CaretEvent e) {
        if (instance.getFile(e.getEditor().getDocument()) == null) {
            return;//console            
        }
        logger.debug("caretAdded");
        UIUtils.renderPlantUmlToolWindowLater(e.getEditor().getProject(), LazyApplicationPoolExecutor.Delay.POST_DELAY);
    }

    @Override
    public void caretRemoved(CaretEvent e) {
        // do nothing
    }
}
