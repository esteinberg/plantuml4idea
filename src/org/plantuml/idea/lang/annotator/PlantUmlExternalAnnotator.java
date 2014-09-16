package org.plantuml.idea.lang.annotator;

import com.google.common.base.Joiner;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import net.sourceforge.plantuml.syntax.SyntaxChecker;
import net.sourceforge.plantuml.syntax.SyntaxResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.plantuml.PlantUml;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Author: Eugene Steinberg
 * Date: 9/13/14
 */
public class PlantUmlExternalAnnotator extends ExternalAnnotator<PsiFile, Map<Integer, AnnotationResult>> {
    @Nullable
    @Override
    public PsiFile collectInformation(@NotNull PsiFile file) {
        return file;
    }

    @Nullable
    @Override
    public Map<Integer, AnnotationResult> doAnnotate(PsiFile collectedInfo) {
        Map<Integer, AnnotationResult> result = new LinkedHashMap<Integer, AnnotationResult>();
        if (PlantUmlSettings.getInstance().isErrorAnnotationEnabled()) {
            String text = collectedInfo.getFirstChild().getText();
            Map<Integer, String> sources = PlantUml.extractSources(text);
            for (Map.Entry<Integer, String> sourceData : sources.entrySet()) {
                AnnotationResult annotationResult = getAnnotationResult(sourceData.getValue());
                if (annotationResult != null) {
                    result.put(sourceData.getKey(), annotationResult);
                }
            }


        }
        return result;
    }

    private AnnotationResult getAnnotationResult(String source) {
        AnnotationResult result = null;
        SyntaxResult syntaxResult = SyntaxChecker.checkSyntax(source);
        if (syntaxResult.isError()) {
            String error = Joiner.on(" \n").join(syntaxResult.getErrors());
            String suggest = Joiner.on(" \n").join(syntaxResult.getSuggest());
            result = new AnnotationResult(error, suggest, syntaxResult.getErrorLinePosition());
        }
        return result;
    }

    @Override
    public void apply(@NotNull PsiFile file, Map<Integer, AnnotationResult> annotationResult, @NotNull AnnotationHolder holder) {
        if (null != annotationResult) {
            Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
            if (document != null) {
                for (Map.Entry<Integer, AnnotationResult> arEntry : annotationResult.entrySet()) {
                    annotateSource(arEntry.getValue(), holder, document, arEntry.getKey());
                }

            }
        }
    }

    private void annotateSource(AnnotationResult annotationResult, AnnotationHolder holder, Document document, int baseOffset) {
        int sourceStartLineNumber = document.getLineNumber(baseOffset);
        int startoffset = document.getLineStartOffset(annotationResult.getLineNumber() + sourceStartLineNumber);
        int endoffset = document.getLineEndOffset(annotationResult.getLineNumber() + sourceStartLineNumber);
        TextRange range = TextRange.create(startoffset, endoffset);
        holder.createErrorAnnotation(range,
                annotationResult.getErrorMessage() + " " + annotationResult.getSuggestion());
    }
}
