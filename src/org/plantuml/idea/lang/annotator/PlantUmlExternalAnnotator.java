package org.plantuml.idea.lang.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.external.PlantUmlFacade;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.plantuml.PlantUml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

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

                List<SyntaxHighlightAnnotation> blockComments = annotateBlockComments(source);
                sourceAnnotationResult.addBlockComments(blockComments);

                annotateByLine(sourceAnnotationResult, source);

                result.add(sourceAnnotationResult);
            }
        }
        return result;
    }

    public void annotateByLine(SourceAnnotationResult result, String source) {
        Matcher keywords = LanguagePatternHolder.INSTANCE.keywordsPattern.matcher("");
        Matcher pluginSettings = LanguagePatternHolder.INSTANCE.pluginSettingsPattern.matcher("");
        Matcher types = LanguagePatternHolder.INSTANCE.typesPattern.matcher("");
        Matcher preproc = LanguagePatternHolder.INSTANCE.preprocPattern.matcher("");
        Matcher tags = LanguagePatternHolder.INSTANCE.tagsPattern.matcher("");
        Matcher commentMatcher = LanguagePatternHolder.INSTANCE.lineCommentPattern.matcher("");


        String[] strings = StringUtils.splitPreserveAllTokens(source, "\n");
        int i = 0;
        for (String line : strings) {
            commentMatcher.reset(line);
            if (commentMatcher.find()) {
                result.addWithBlockCommentCheck(new SyntaxHighlightAnnotation(i + commentMatcher.start(), i + commentMatcher.end(), DefaultLanguageHighlighterColors.LINE_COMMENT));
            } else {
                annotate(result, line, i, DefaultLanguageHighlighterColors.KEYWORD, keywords);
                annotate(result, line, i, DefaultLanguageHighlighterColors.KEYWORD, pluginSettings);
                annotate(result, line, i, DefaultLanguageHighlighterColors.LABEL, types);
                annotate(result, line, i, DefaultLanguageHighlighterColors.METADATA, preproc);
                annotate(result, line, i, METADATA, tags);
            }

            i += line.length() + 1;
        }
    }

    private void annotate(SourceAnnotationResult result, String line, int i, TextAttributesKey textAttributesKey, Matcher matcher) {
        matcher.reset(line);
        while (matcher.find()) {
            result.addWithBlockCommentCheck(new SyntaxHighlightAnnotation(i + matcher.start(), i + matcher.end(), textAttributesKey));
        }
    }

    private List<SyntaxHighlightAnnotation> annotateBlockComments(String source) {
        List<SyntaxHighlightAnnotation> result = new ArrayList<>();

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
            } else {
                break;
            }
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
