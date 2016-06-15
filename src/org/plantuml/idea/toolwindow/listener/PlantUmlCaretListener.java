package org.plantuml.idea.toolwindow.listener;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.rendering.LazyApplicationPoolExecutor;
import org.plantuml.idea.rendering.RenderCommand;
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
        if (settings.isAutoRender()) {
            UIUtils.renderPlantUmlToolWindowLater(e.getEditor().getProject(), LazyApplicationPoolExecutor.Delay.POST_DELAY, RenderCommand.Reason.CARET);
        }
    }

    @Override
    public void caretAdded(CaretEvent e) {
        if (instance.getFile(e.getEditor().getDocument()) == null) {
            return;//console            
        }
        logger.debug("caretAdded");
        if (settings.isAutoRender()) {
            UIUtils.renderPlantUmlToolWindowLater(e.getEditor().getProject(), LazyApplicationPoolExecutor.Delay.POST_DELAY, RenderCommand.Reason.CARET);
        }
    }

    @Override
    public void caretRemoved(CaretEvent e) {
        // do nothing
    }
}
