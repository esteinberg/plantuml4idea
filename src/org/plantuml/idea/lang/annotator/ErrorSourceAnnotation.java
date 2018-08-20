package org.plantuml.idea.lang.annotator;

import com.google.common.base.Joiner;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Author: Eugene Steinberg
 * Date: 9/13/14
 */
public class ErrorSourceAnnotation implements SourceAnnotation {
    private Collection<String> errorMessage;
    private Collection<String> suggestion;
    private int lineNumber;

    public ErrorSourceAnnotation(Collection<String> errormsg, Collection<String> suggestion, int errorLineNumber) {
        this.errorMessage = errormsg;
        this.suggestion = suggestion;
        lineNumber = errorLineNumber;
    }

    @Override
    public String toString() {
        return "SourceAnnotationResult{" +
                "errorMessage=" + errorMessage +
                ", suggestion=" + suggestion +
                ", lineNumber=" + lineNumber +
                '}';
    }

    @Override
    public void annotate(AnnotationHolder holder, Document document, int sourceOffset) {
        int sourceStartLineNumber = document.getLineNumber(sourceOffset);
        int errorLineNumber = lineNumber + sourceStartLineNumber;
        int startoffset = document.getLineStartOffset(errorLineNumber);
        int endoffset = document.getLineEndOffset(errorLineNumber);
        TextRange range = TextRange.create(startoffset, endoffset);
        String errMessage = Joiner.on("\n").join(errorMessage);
        Annotation errorAnnotation = holder.createErrorAnnotation(range,
                errMessage);
        for (String s : cleanupSuggestions(suggestion)) {
            errorAnnotation.registerFix(new PlantUmlIntentionAction(startoffset, endoffset, s));
        }

    }

    private List<String> cleanupSuggestions(Collection<String> suggest) {
        if (suggest == null)
            return Collections.emptyList();
        LinkedList<String> suggestions = new LinkedList<String>(suggest);
        suggestions.remove("Did you mean:");
        return suggestions;
    }

    private class PlantUmlIntentionAction implements IntentionAction {
        private int startOffset;
        private int endOffset;
        private String suggestion;

        protected PlantUmlIntentionAction(int startOffset, int endOffset, String suggestion) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.suggestion = suggestion;
        }

        @NotNull
        @Override
        public String getText() {
            return "change to '" + suggestion + "'";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return "PLANTUML_INTENTIONS";
        }

        @Override
        public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
            return true;
        }

        @Override
        public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
            editor.getDocument().replaceString(startOffset, endOffset, suggestion);
        }

        @Override
        public boolean startInWriteAction() {
            return true;
        }
    }
}
