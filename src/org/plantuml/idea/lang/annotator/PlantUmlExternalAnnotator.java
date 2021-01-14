package org.plantuml.idea.lang.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.IntRange;
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
import static org.plantuml.idea.util.Utils.join;
import static org.plantuml.idea.util.Utils.rangesInside;

/**
 * Author: Eugene Steinberg
 * Date: 9/13/14
 */
public class PlantUmlExternalAnnotator extends ExternalAnnotator<PlantUmlExternalAnnotator.Info, FileAnnotationResult> {
    private static final Logger logger = Logger.getInstance(PlantUmlExternalAnnotator.class);
    private PlantUmlSettings plantUmlSettings;

    public PlantUmlExternalAnnotator() {
        plantUmlSettings = PlantUmlSettings.getInstance();
    }

    public static class Info {

        private final String text;
        private final VirtualFile virtualFile;

        public Info(String text, VirtualFile virtualFile) {
            this.text = text;
            this.virtualFile = virtualFile;
        }

        public Info(PsiFile file) {
            this(file.getText(), file.getVirtualFile());
        }
    }

    @Nullable
    @Override
    public Info collectInformation(@NotNull PsiFile file) {
        return new Info(file.getText(), file.getVirtualFile());
    }

    @Nullable
    @Override
    public FileAnnotationResult doAnnotate(Info file) {
        // Temporary solution to avoid execution under read action in dumb mode. Should be removed after IDEA-229905 will be fixed
        Application application = ApplicationManager.getApplication();
        if (application != null && application.isReadAccessAllowed() && !application.isUnitTestMode()) {
            return null;
        }

        FileAnnotationResult result = new FileAnnotationResult();
        if (plantUmlSettings.isErrorAnnotationEnabled() || plantUmlSettings.isKeywordHighlighting()) {
            String text = file.text;

            Map<Integer, String> sources = PlantUml.extractSources(text);

            for (Map.Entry<Integer, String> sourceData : sources.entrySet()) {
                Integer sourceOffset = sourceData.getKey();
                SourceAnnotationResult sourceAnnotationResult = new SourceAnnotationResult(sourceOffset);
                String source = sourceData.getValue();

                if (plantUmlSettings.isErrorAnnotationEnabled()) {
                    sourceAnnotationResult.addAll(PlantUmlFacade.get().annotateSyntaxErrors(source, file.virtualFile));
                    List<SyntaxHighlightAnnotation> blockComments = annotateBlockComments(source);
                    sourceAnnotationResult.addBlockComments(blockComments);
                }

                annotateByLine(sourceAnnotationResult, source);

                result.add(sourceAnnotationResult);
            }
        }
        return result;
    }

    public void annotateByLine(SourceAnnotationResult result, String source) {
        Matcher keywords = LanguagePatternHolder.INSTANCE.keywordsPattern.matcher("");
        Matcher keywords2 = LanguagePatternHolder.INSTANCE.keywords2Pattern.matcher("");
        Matcher types = LanguagePatternHolder.INSTANCE.typesPattern.matcher("");
        Matcher pluginSettings = LanguagePatternHolder.INSTANCE.pluginSettingsPattern.matcher("");
        Matcher preproc = LanguagePatternHolder.INSTANCE.preprocPattern.matcher("");
        Matcher tags = LanguagePatternHolder.INSTANCE.tagsPattern.matcher("");
        Matcher commentMatcher = LanguagePatternHolder.INSTANCE.lineCommentPattern.matcher("");


        String[] strings = StringUtils.splitPreserveAllTokens(source, "\n");
        int offset = 0;
        for (String line : strings) {
            commentMatcher.reset(line);
            if (commentMatcher.find()) {
                SyntaxHighlightAnnotation lineComment = new SyntaxHighlightAnnotation(offset + commentMatcher.start(), offset + commentMatcher.end(), DefaultLanguageHighlighterColors.LINE_COMMENT);
                result.addWithBlockCommentCheck(lineComment);
            } else {
                if (plantUmlSettings.isKeywordHighlighting()) {
                    highlightKeywords(result, keywords, types, keywords2, offset, line);
                }
                annotate(result, line, offset, null, DefaultLanguageHighlighterColors.KEYWORD, pluginSettings);
                annotate(result, line, offset, null, DefaultLanguageHighlighterColors.METADATA, preproc);
                annotate(result, line, offset, null, METADATA, tags);
            }

            offset += line.length() + 1;
        }
    }

    private void highlightKeywords(SourceAnnotationResult result, Matcher keywords, Matcher types, Matcher keywords2, int offset, String line) {
        int i = line.indexOf(":");  //it seems no keywords are after :
        if (i > 0) {
            line = line.substring(0, i);
        }
        //not reliable when mixed braces ([)...(]), but it don't need to be
        List<IntRange> excludedRanges = join(rangesInside(line, "[", "]"), rangesInside(line, "(", ")"));
        excludedRanges = join(excludedRanges, rangesInside(line, "\"", "\""));

        annotate(result, line, offset, excludedRanges, DefaultLanguageHighlighterColors.KEYWORD, keywords);
        annotate(result, line, offset, excludedRanges, DefaultLanguageHighlighterColors.KEYWORD, keywords2);
        annotate(result, line, offset, excludedRanges, DefaultLanguageHighlighterColors.KEYWORD, types);
    }

    private void annotate(SourceAnnotationResult result, String line, int offset, List<IntRange> excludedRanges, TextAttributesKey textAttributesKey, Matcher matcher) {
        matcher.reset(line);

        while (matcher.find()) {
            if (isExcluded(excludedRanges, matcher.start())) {
                continue;
            }
            SyntaxHighlightAnnotation a = new SyntaxHighlightAnnotation(offset + matcher.start(), offset + matcher.end(), textAttributesKey);
            result.addWithBlockCommentCheck(a);
        }
    }

    private boolean isExcluded(List<IntRange> excludedRanges, int start) {
        if (excludedRanges != null) {
            for (IntRange excludedRange : excludedRanges) {
                if (excludedRange.containsInteger(start)) {
                    return true;
                }
            }
        }
        return false;
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
