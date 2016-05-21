package org.plantuml.idea.toolwindow.listener;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.plantuml.idea.util.LazyApplicationPoolExecutor;
import org.plantuml.idea.util.UIUtils;

import static com.intellij.codeInsight.completion.CompletionInitializationContext.DUMMY_IDENTIFIER;

public class PlantUmlDocumentListener implements DocumentListener {
    private static Logger logger = Logger.getInstance(PlantUmlDocumentListener.class);
    public FileDocumentManager instance = FileDocumentManager.getInstance();

    @Override
    public void beforeDocumentChange(DocumentEvent event) {
    }

    @Override
    public void documentChanged(DocumentEvent event) {
        VirtualFile file = instance.getFile(event.getDocument());
        if (file == null) {
            return;//console
        }
        if (logger.isDebugEnabled()) {
            logger.debug("document changed ", event.getSource());
        }

        //#18 Strange "IntellijIdeaRulezzz" - filter code completion event.
        if (!DUMMY_IDENTIFIER.equals(event.getNewFragment().toString())) {
            Editor[] editors = EditorFactory.getInstance().getEditors(event.getDocument());
            for (Editor editor : editors) {
                UIUtils.renderPlantUmlToolWindowLater(editor.getProject(), LazyApplicationPoolExecutor.Delay.RESET_PRE_DELAY);
            }
        }
    }
}

