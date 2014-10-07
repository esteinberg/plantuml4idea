package org.plantuml.idea.lang.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import net.sourceforge.plantuml.syntax.SyntaxChecker;
import net.sourceforge.plantuml.syntax.SyntaxResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.plantuml.PlantUml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public FileAnnotationResult doAnnotate(PsiFile file) {
        FileAnnotationResult result = new FileAnnotationResult();

        if (PlantUmlSettings.getInstance().isErrorAnnotationEnabled()) {
            String text = file.getFirstChild().getText();

            Map<Integer, String> sources = PlantUml.extractSources(text);

            for (Map.Entry<Integer, String> sourceData : sources.entrySet()) {
                Integer sourceOffset = sourceData.getKey();

                SourceAnnotationResult sourceAnnotationResult = new SourceAnnotationResult(sourceOffset);

                String source = sourceData.getValue();

                sourceAnnotationResult.addAll(annotateSyntaxErrors(source));

                sourceAnnotationResult.addAll(annotateSyntaxHighlight(source,
                        LanguagePatternHolder.INSTANCE.keywordsPattern,
                        DefaultLanguageHighlighterColors.KEYWORD));

                sourceAnnotationResult.addAll(annotateSyntaxHighlight(source,
                        LanguagePatternHolder.INSTANCE.typesPattern,
                        DefaultLanguageHighlighterColors.LABEL));

                sourceAnnotationResult.addAll(annotateSyntaxHighlight(source,
                        LanguagePatternHolder.INSTANCE.preprocPattern,
                        DefaultLanguageHighlighterColors.METADATA));

                result.add(sourceAnnotationResult);
            }


        }
        return result;
    }

    @Nullable
    private Collection<SourceAnnotation> annotateSyntaxErrors(String source) {
        Collection<SourceAnnotation> result = new ArrayList<SourceAnnotation>();
        SyntaxResult syntaxResult = checkSyntax(source);
        if (syntaxResult.isError()) {
            ErrorSourceAnnotation errorSourceAnnotation = new ErrorSourceAnnotation(
                    syntaxResult.getErrors(),
                    syntaxResult.getSuggest(),
                    syntaxResult.getErrorLinePosition()
            );
            result.add(errorSourceAnnotation);
        }
        return result;
    }

    private SyntaxResult checkSyntax(String source) {
        return SyntaxChecker.checkSyntax(source);
    }

    private Collection<SourceAnnotation> annotateSyntaxHighlight(String source, Pattern pattern, TextAttributesKey textAttributesKey) {
        Collection<SourceAnnotation> result = new ArrayList<SourceAnnotation>();
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            result.add(new SyntaxHighlightAnnotation(matcher.start(), matcher.end(), textAttributesKey));
        }
        return result;
    }

    @Override
    public void apply(@NotNull PsiFile file, FileAnnotationResult fileAnnotationResult, @NotNull AnnotationHolder holder) {
        if (null != fileAnnotationResult) {
            Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
            if (document != null) {
                fileAnnotationResult.annotate(holder, document);
            }
        }
    }


}
