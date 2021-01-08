package org.plantuml.idea.lang.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.external.PlantUmlFacade;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.plantuml.PlantUml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.METADATA;

/**
 * Author: Eugene Steinberg
 * Date: 9/13/14
 */
public class PlantUmlExternalAnnotator extends ExternalAnnotator<PsiFile, FileAnnotationResult> {
    private static final Logger logger = Logger.getInstance(PlantUmlExternalAnnotator.class);

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
                sourceAnnotationResult.addAll(PlantUmlFacade.get().annotateSyntaxErrors(file, source));

                sourceAnnotationResult.addAll(annotateSyntaxHighlight(source,
                        LanguagePatternHolder.INSTANCE.keywordsPattern,
                        DefaultLanguageHighlighterColors.KEYWORD));

                sourceAnnotationResult.addAll(annotateSyntaxHighlight(source,
                        LanguagePatternHolder.INSTANCE.pluginSettingsPattern,
                        DefaultLanguageHighlighterColors.KEYWORD));

                sourceAnnotationResult.addAll(annotateSyntaxHighlight(source,
                        LanguagePatternHolder.INSTANCE.typesPattern,
                        DefaultLanguageHighlighterColors.LABEL));

                sourceAnnotationResult.addAll(annotateSyntaxHighlight(source,
                        LanguagePatternHolder.INSTANCE.preprocPattern,
                        DefaultLanguageHighlighterColors.METADATA));

                sourceAnnotationResult.addAll(annotateSyntaxHighlight(source,
                        LanguagePatternHolder.INSTANCE.tagsPattern,
                        METADATA));

                sourceAnnotationResult.addAll(annotateSyntaxHighlight(source,
                        LanguagePatternHolder.INSTANCE.lineCommentPattern,
                        DefaultLanguageHighlighterColors.LINE_COMMENT));

                sourceAnnotationResult.addAll(annotateBlockComments(source));

                result.add(sourceAnnotationResult);
            }
        }
        return result;
    }

    private Collection<SourceAnnotation> annotateBlockComments(String source) {
        Collection<SourceAnnotation> result = new ArrayList<SourceAnnotation>();

        Matcher matcher = LanguagePatternHolder.INSTANCE.startBlockComment.matcher(source);
        Matcher endMatcher = null;

        int start = 0;
        while (matcher.find(start)) {
            start = matcher.start();

            if (endMatcher == null) {
                endMatcher = LanguagePatternHolder.INSTANCE.endBlockComment.matcher(source);
            }
            if (endMatcher.find(start)) {
                result.add(new SyntaxHighlightAnnotation(matcher.start(), endMatcher.end(), DefaultLanguageHighlighterColors.BLOCK_COMMENT));
                start = endMatcher.end();
            }
        }
        return result;
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
