package org.plantuml.idea.preview.listener;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;
import org.plantuml.idea.rendering.LazyApplicationPoolExecutor;
import org.plantuml.idea.rendering.RenderCommand;
import org.plantuml.idea.settings.PlantUmlSettings;
import org.plantuml.idea.util.UIUtils;

import static com.intellij.codeInsight.completion.CompletionInitializationContext.DUMMY_IDENTIFIER;

public class PlantUmlDocumentListener implements DocumentListener {
    private static Logger logger = Logger.getInstance(PlantUmlDocumentListener.class);
    public FileDocumentManager instance = FileDocumentManager.getInstance();
    private PlantUmlSettings settings;
    private EditorFactory editorFactory;

    public PlantUmlDocumentListener() {
        settings = PlantUmlSettings.getInstance();
        editorFactory = EditorFactory.getInstance();
    }

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
        if (settings.isAutoRender()) {
            //#18 Strange "IntellijIdeaRulezzz" - filter code completion event.
            if (!DUMMY_IDENTIFIER.equals(event.getNewFragment().toString())) {
                Editor[] editors = editorFactory.getEditors(event.getDocument());
                for (PlantUmlPreviewPanel panel : UIUtils.getEligiblePreviews(editors)) {
                    if (panel != null) {
                        panel.processRequest(LazyApplicationPoolExecutor.Delay.RESET_DELAY, RenderCommand.Reason.SOURCE_PAGE_ZOOM);
                    }
                }
            }
        }
    }
}

