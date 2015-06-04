package org.plantuml.idea.intentions;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.application.ex.ApplicationInfoEx;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.BuildNumber;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.PlantUmlFileType;

public class ReverseArrowIntention extends BaseIntentionAction {
    public static final Logger logger = Logger.getInstance(ReverseArrowIntention.class);
    protected boolean supportsMultiCaret;

    public ReverseArrowIntention() {
        BuildNumber build = ApplicationInfoEx.getInstanceEx().getBuild();
        //from 13.1 (135.475)
        supportsMultiCaret = build.getBaselineVersion() >= 135 && build.getBuildNumber() >= 475;
    }

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
        if (supportsMultiCaret) {
            boolean available = false;
            for (Caret caret : editor.getCaretModel().getAllCarets()) {
                if (caret.isValid()) {
                    available = new ReverseArrowCommand(editor, caret.getOffset()).isAvailable();
                    if (available) {
                        break;
                    }
                }
            }
            return available;
        } else {
            return new ReverseArrowCommand(editor, editor.getCaretModel().getOffset()).isAvailable();
        }
    }

    @Override
    public void invoke(@NotNull Project project, final Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        if (supportsMultiCaret) {
            for (Caret caret : editor.getCaretModel().getAllCarets()) {
                if (caret.isValid()) {
                    new ReverseArrowCommand(editor, caret.getOffset()).invoke();
                }
            }
        } else {
            new ReverseArrowCommand(editor, editor.getCaretModel().getOffset()).invoke();
        }
    }

    private class ReverseArrowCommand {
        private Editor editor;
        protected int caretOffset;

        public ReverseArrowCommand(Editor editor, int caretOffset) {
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
            int caretOffsetWithinLine = caretOffset - lineStartOffset;
            char[] chars = line.toCharArray();

            if (logger.isDebugEnabled()) {
                logger.debug("invoking caretOffsetWithinLine=" + caretOffsetWithinLine + ", line='" + line + "'");
            }

            Arrow arrow = Arrow.from(caretOffsetWithinLine, chars);
            int start = arrow.getStart();
            int end = arrow.getEnd();
            if (logger.isDebugEnabled()) {
                logger.debug("result: isValid=" + arrow.isValid() + ", start=" + start + ", end=" + end);
            }

            if (!validateOnly && arrow.isValid()) {
                char[] reverse = ArrowUtils.cutArrowAndReverse(chars, start, end);
                document.replaceString(lineStartOffset + start, lineStartOffset + end + 1, new String(reverse));
            }
            return arrow.isValid();
        }

    }

}
