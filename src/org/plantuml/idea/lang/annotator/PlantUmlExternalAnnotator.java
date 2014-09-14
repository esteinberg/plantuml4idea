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

/**
 * Author: Eugene Steinberg
 * Date: 9/13/14
 */
public class PlantUmlExternalAnnotator extends ExternalAnnotator<PsiFile, AnnotationResult> {
    @Nullable
    @Override
    public PsiFile collectInformation(@NotNull PsiFile file) {
        return file;
    }

    @Nullable
    @Override
    public AnnotationResult doAnnotate(PsiFile collectedInfo) {
        AnnotationResult result = null;
        if (PlantUmlSettings.getInstance().isErrorAnnotationEnabled()) {
            String text = collectedInfo.getFirstChild().getText();
            String source = PlantUml.extractSource(text, 0);
            SyntaxResult syntaxResult = SyntaxChecker.checkSyntax(source);
            if (syntaxResult.isError()) {
                String error = Joiner.on(" \n").join(syntaxResult.getErrors());
                String suggest = Joiner.on(" \n").join(syntaxResult.getSuggest());
                result = new AnnotationResult(error, suggest, syntaxResult.getErrorLinePosition());
            }
        }
        return result;
    }

    @Override
    public void apply(@NotNull PsiFile file, AnnotationResult annotationResult, @NotNull AnnotationHolder holder) {
        if (null != annotationResult) {
            Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
            if (document != null) {
                int startoffset = document.getLineStartOffset(annotationResult.getLineNumber());
                int endoffset = document.getLineEndOffset(annotationResult.getLineNumber());
                TextRange range = TextRange.create(startoffset, endoffset);
                holder.createErrorAnnotation(range,
                        annotationResult.getErrorMessage() + " " + annotationResult.getSuggestion());
            }
        }
    }
}
