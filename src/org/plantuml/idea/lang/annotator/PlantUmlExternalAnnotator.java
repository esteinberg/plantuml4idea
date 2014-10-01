package org.plantuml.idea.lang.annotator;

import com.google.common.base.Joiner;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import net.sourceforge.plantuml.syntax.SyntaxChecker;
import net.sourceforge.plantuml.syntax.SyntaxResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.plantuml.PlantUml;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Author: Eugene Steinberg
 * Date: 9/13/14
 */
public class PlantUmlExternalAnnotator extends ExternalAnnotator<PsiFile, Map<Integer, SyntaxResult>> {
    @Nullable
    @Override
    public PsiFile collectInformation(@NotNull PsiFile file) {
        return file;
    }

    @Nullable
    @Override
    public Map<Integer, SyntaxResult> doAnnotate(PsiFile collectedInfo) {
        Map<Integer, SyntaxResult> result = new LinkedHashMap<Integer, SyntaxResult>();
        if (PlantUmlSettings.getInstance().isErrorAnnotationEnabled()) {
            String text = collectedInfo.getFirstChild().getText();
            Map<Integer, String> sources = PlantUml.extractSources(text);
            for (Map.Entry<Integer, String> sourceData : sources.entrySet()) {
                SyntaxResult annotationResult = getAnnotationResult(sourceData.getValue());
                if (annotationResult.isError()) {
                    result.put(sourceData.getKey(), annotationResult);
                }
            }


        }
        return result;
    }

    private SyntaxResult getAnnotationResult(String source) {
        return SyntaxChecker.checkSyntax(source);
    }

    @Override
    public void apply(@NotNull PsiFile file, Map<Integer, SyntaxResult> annotationResult, @NotNull AnnotationHolder holder) {
        if (null != annotationResult) {
            Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
            if (document != null) {
                for (Map.Entry<Integer, SyntaxResult> arEntry : annotationResult.entrySet()) {
                    annotateSource(arEntry.getValue(), holder, document, arEntry.getKey());
                }

            }
        }
    }

    private void annotateSource(SyntaxResult annotationResult, AnnotationHolder holder, Document document, int baseOffset) {
        int sourceStartLineNumber = document.getLineNumber(baseOffset);
        int errorLine = annotationResult.getErrorLinePosition() + sourceStartLineNumber;
        if (errorLine < document.getLineCount()) {
            int startoffset = document.getLineStartOffset(errorLine);
            int endoffset = document.getLineEndOffset(errorLine);
            TextRange range = TextRange.create(startoffset, endoffset);
            String errorMessage = Joiner.on("\n").join(annotationResult.getErrors());
            Annotation errorAnnotation = holder.createErrorAnnotation(range,
                    errorMessage);
            for (String s : cleanupSuggestions(annotationResult.getSuggest())) {
                errorAnnotation.registerFix(new PlantUmlIntentionAction(startoffset, endoffset, s));
            }
        }

    }

    private List<String> cleanupSuggestions(List<String> suggest) {
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
