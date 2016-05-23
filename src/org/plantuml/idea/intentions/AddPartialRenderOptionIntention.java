package org.plantuml.idea.intentions;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.PlantUmlFileType;
import org.plantuml.idea.lang.annotator.LanguageDescriptor;

public class AddPartialRenderOptionIntention extends BaseIntentionAction {
    public static final Logger logger = Logger.getInstance(AddPartialRenderOptionIntention.class);

    public AddPartialRenderOptionIntention() {
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Enable partial rendering";
    }

    @NotNull
    protected String option() {
        return LanguageDescriptor.IDEA_PARTIAL_RENDER;
    }

    @NotNull
    @Override
    public String getText() {
        return getFamilyName();
    }

    @Override
    public boolean isAvailable(@NotNull Project project, final Editor editor, PsiFile file) {
        if (!file.getFileType().equals(PlantUmlFileType.PLANTUML_FILE_TYPE)) return false;
        int offset = editor.getCaretModel().getOffset();
        return new AddPartialRenderCommand(editor, offset).isAvailable();
    }

    @Override
    public void invoke(@NotNull Project project, final Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        new AddPartialRenderCommand(editor, editor.getCaretModel().getOffset()).invoke();
    }

    private class AddPartialRenderCommand {
        private Editor editor;
        protected int caretOffset;

        public AddPartialRenderCommand(Editor editor, int caretOffset) {
            this.editor = editor;
            this.caretOffset = caretOffset;
        }

        public boolean isAvailable() {
            return invoke(true);
        }

        public boolean invoke() {
            return invoke(false);
        }

        private boolean invoke(boolean validateOnly) {
            Document document = editor.getDocument();
            int lineNumber = document.getLineNumber(caretOffset);
            int lineStartOffset = document.getLineStartOffset(lineNumber);
            int lineEndOffset = document.getLineEndOffset(lineNumber);
            String line = document.getText(TextRange.create(lineStartOffset, lineEndOffset));
            boolean start = line.contains("@startuml")
                    || line.contains("startuml")
                    || line.contains("@startditaa")
                    || line.contains("@startdot");

            if (!validateOnly && start) {
                document.replaceString(lineEndOffset, lineEndOffset, "\n'" + option());
            }
            return start;
        }

    }

}
