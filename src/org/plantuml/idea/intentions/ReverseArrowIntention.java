package org.plantuml.idea.intentions;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretAction;
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
        return "Reverse arrows";
    }

    @NotNull
    @Override
    public String getText() {
        return getFamilyName();
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        if (!file.getFileType().equals(PlantUmlFileType.PLANTUML_FILE_TYPE)) return false;

        return new ReverseArrowCommand(editor).invoke(true);
    }

    @Override
    public void invoke(@NotNull Project project, final Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        editor.getCaretModel().runForEachCaret(new CaretAction() {
            @Override
            public void perform(Caret caret) {
                new ReverseArrowCommand(editor).invoke(false);
            }
        });
    }

    private class ReverseArrowCommand {
        private Editor editor;

        public ReverseArrowCommand(Editor editor) {
            this.editor = editor;
        }

        public boolean invoke(boolean validateOnly) {
            Caret currentCaret = editor.getCaretModel().getCurrentCaret();
            int caretOffset = currentCaret.getOffset();
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
            int end1 = arrow.getStart();
            int end2 = arrow.getEnd();
            if (logger.isDebugEnabled()) {
                logger.debug("result: isArrow=" + arrow.isValidArrow() + ", end1=" + end1 + ",end2=" + end2);
            }

            if (!validateOnly) {
                char[] reverse = ArrowUtils.cutArrowAndReverse(chars, end1, end2);
                document.replaceString(lineStartOffset + end1, lineStartOffset + end2 + 1, new String(reverse));
            }
            return arrow.isValidArrow();
        }

    }

}
