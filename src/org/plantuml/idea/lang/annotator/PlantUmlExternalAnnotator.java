package org.plantuml.idea.lang.annotator;

import com.google.common.base.Joiner;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Author: Eugene Steinberg
 * Date: 9/13/14
 */
public class PlantUmlExternalAnnotator extends ExternalAnnotator<PsiFile, FileAnnotationResult> {
    @Nullable
    @Override
    public PsiFile collectInformation(@NotNull PsiFile file) {
        return file;
    }

    @Nullable
    @Override
    public FileAnnotationResult doAnnotate(PsiFile collectedInfo) {
        FileAnnotationResult result = new FileAnnotationResult();

        if (PlantUmlSettings.getInstance().isErrorAnnotationEnabled()) {
            String text = collectedInfo.getFirstChild().getText();

            Map<Integer, String> sources = PlantUml.extractSources(text);

            for (Map.Entry<Integer, String> sourceData : sources.entrySet()) {
                SyntaxResult syntaxResult = checkSyntax(sourceData.getValue());

//                if (syntaxResult.isError()) {
                SourceAnnotationResult sourceAnnotationResult = new SourceAnnotationResult(
                        syntaxResult.getErrors(),
                        syntaxResult.getSuggest(),
                        syntaxResult.getErrorLinePosition()
                );
                result.put(sourceData.getKey(), sourceAnnotationResult);
//                }
            }


        }
        return result;
    }

    private SyntaxResult checkSyntax(String source) {
        return SyntaxChecker.checkSyntax(source);
    }

    @Override
    public void apply(@NotNull PsiFile file, FileAnnotationResult annotationResult, @NotNull AnnotationHolder holder) {
        if (null != annotationResult) {
            Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
            if (document != null) {
                for (Map.Entry<Integer, SourceAnnotationResult> arEntry : annotationResult.getAnnotationResultMap().entrySet()) {
                    annotateSource(arEntry.getValue(), holder, document, arEntry.getKey());
                }

            }
        }
    }

    private void annotateSource(SourceAnnotationResult annotationResult, AnnotationHolder holder, Document document, int baseOffset) {
        int sourceStartLineNumber = document.getLineNumber(baseOffset);
        int startoffset = document.getLineStartOffset(annotationResult.getErrorLineNumber() + sourceStartLineNumber);
        int endoffset = document.getLineEndOffset(annotationResult.getErrorLineNumber() + sourceStartLineNumber);
        TextRange range = TextRange.create(startoffset, endoffset);
        String errorMessage = Joiner.on("\n").join(annotationResult.getErrorMessage());
        Annotation errorAnnotation = holder.createErrorAnnotation(range,
                errorMessage);
        for (String s : cleanupSuggestions(annotationResult.getSuggestion())) {
            errorAnnotation.registerFix(new PlantUmlIntentionAction(startoffset, endoffset, s));
        }

        String source = document.getText();

        Matcher matcher = LanguagePatternHolder.INSTANCE.keywordsPattern.matcher(source);
        while (matcher.find()) {
            highlightToken(holder, matcher.group(), matcher.start());
        }


    }

    private void highlightToken(AnnotationHolder holder, String token, int idx) {
        TextRange range;
        range = TextRange.create(idx, idx + token.length());
        Annotation infoAnnotation = holder.createInfoAnnotation(range, "");
        infoAnnotation.setTextAttributes(DefaultLanguageHighlighterColors.KEYWORD);
    }

    private List<String> cleanupSuggestions(Collection<String> suggest) {
        if (suggest == null)
            return new LinkedList<String>();
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
