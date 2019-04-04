package org.plantuml.idea.lang.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import net.sourceforge.plantuml.syntax.SyntaxChecker;
import net.sourceforge.plantuml.syntax.SyntaxResult;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.plantuml.PlantUmlIncludes;
import org.plantuml.idea.util.UIUtils;
import org.plantuml.idea.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.METADATA;
import static java.lang.System.currentTimeMillis;
import static org.plantuml.idea.lang.annotator.LanguageDescriptor.IDEA_DISABLE_SYNTAX_CHECK;

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
                sourceAnnotationResult.addAll(annotateSyntaxErrors(file, source));

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

    @Nullable
    private Collection<SourceAnnotation> annotateSyntaxErrors(PsiFile file, String source) {
        if (source.contains(IDEA_DISABLE_SYNTAX_CHECK)) {
            return Collections.emptyList();
        }
        Collection<SourceAnnotation> result = new ArrayList<SourceAnnotation>();
        long start = currentTimeMillis();
        SyntaxResult syntaxResult = checkSyntax(file, source);
        logger.debug("syntax checked in ", currentTimeMillis() - start, "ms");

        if (syntaxResult.isError()) {
            String beforeInclude = StringUtils.substringBefore(source, "!include");
            int includeLineNumber = StringUtils.splitPreserveAllTokens(beforeInclude, "\n").length;
            //todo hack because plantuml returns line number from source with inlined includes
            if (syntaxResult.getLineLocation().getPosition() < includeLineNumber) {
                ErrorSourceAnnotation errorSourceAnnotation = new ErrorSourceAnnotation(
                        syntaxResult.getErrors(),
                        null,     // syntaxResult.getSuggest(),   missing since version 1.2018.7
                        syntaxResult.getLineLocation().getPosition()
                );
                result.add(errorSourceAnnotation);
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

    private SyntaxResult checkSyntax(PsiFile file, String source) {
        File baseDir = UIUtils.getParent(file.getVirtualFile());
        if (baseDir != null) {
            Utils.setPlantUmlDir(baseDir);

            PlantUmlIncludes.commitIncludes(source, baseDir);
        }
        return SyntaxChecker.checkSyntaxFair(source);
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
