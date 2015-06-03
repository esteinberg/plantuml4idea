package org.plantuml.idea.intentions;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.PlantUmlFileType;

public class ReverseArrowIntention extends BaseIntentionAction {
    public static final Logger logger = Logger.getInstance(ReverseArrowIntention.class);

    @NotNull
    @Override
    public String getFamilyName() {
        return "Reverse arrow";
    }

    @NotNull
    @Override
    public String getText() {
        return getFamilyName();
    }

    @Override
    public boolean isAvailable(@NotNull Project project, final Editor editor, PsiFile file) {
        if (!file.getFileType().equals(PlantUmlFileType.PLANTUML_FILE_TYPE)) return false;
        boolean available = false;
        for (Caret caret : editor.getCaretModel().getAllCarets()) {
            if (caret.isValid()) {
                available = new ReverseArrowCommand(editor, caret).isAvailable();
                if (available) {
                    break;
                }
            }
        }
        return available;
    }

    @Override
    public void invoke(@NotNull Project project, final Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        for (Caret caret : editor.getCaretModel().getAllCarets()) {
            if (caret.isValid()) {
                new ReverseArrowCommand(editor, caret).invoke();
            }
        }
    }

    private class ReverseArrowCommand {
        private Editor editor;
        private Caret caret;

        public ReverseArrowCommand(Editor editor, Caret caret) {
            this.editor = editor;
            this.caret = caret;
        }

        public boolean isAvailable() {
            return invoke(true);
        }

        public boolean invoke() {
            return invoke(false);
        }

        private boolean invoke(boolean validateOnly) {
            int caretOffset = caret.getOffset();
            Document document = editor.getDocument();
            int lineNumber = document.getLineNumber(caretOffset);
            int lineStartOffset = document.getLineStartOffset(lineNumber);
            int lineEndOffset = document.getLineEndOffset(lineNumber);
            String text = document.getText(TextRange.create(lineStartOffset, lineEndOffset));
            int textOffset = caretOffset - lineStartOffset;
            char[] chars = text.toCharArray();

            if (logger.isDebugEnabled()) {
                logger.debug("invoking textOffset=" + textOffset + ", text='" + text + "'");
            }

            Arrow arrow = new Arrow(textOffset, chars).invoke();
            int start = arrow.getStart();
            int end = arrow.getEnd();
            if (logger.isDebugEnabled()) {
                logger.debug("result: isArrow=" + arrow.isValidArrow() + ", start=" + start + ",end=" + end);
            }

            if (!validateOnly && arrow.isValidArrow()) {
                char[] reverse = ArrowUtils.cutArrowAndReverse(chars, start, end);
                document.replaceString(lineStartOffset + start, lineStartOffset + end + 1, new String(reverse));
            }
            return arrow.isValidArrow();
        }

    }

}
