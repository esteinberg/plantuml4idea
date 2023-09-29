package org.plantuml.idea.lang.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.external.PlantUmlFacade;
import org.plantuml.idea.plantuml.SourceExtractor;
import org.plantuml.idea.settings.PlantUmlSettings;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.METADATA;
import static org.plantuml.idea.util.Utils.join;
import static org.plantuml.idea.util.Utils.rangesInside;

/**
 * Author: Eugene Steinberg
 * Date: 9/13/14
 */
public class PlantUmlExternalAnnotator extends ExternalAnnotator<PlantUmlExternalAnnotator.Info, FileAnnotationResult> {
	private static final Logger logger = Logger.getInstance(PlantUmlExternalAnnotator.class);
	public static final Pattern VARIABLES = Pattern.compile("!?\\$\\w+");
	public static final Pattern ALL_PROCS = Pattern.compile("!\\w+");
	public static final Pattern CUSTOM_PROCEDURE = Pattern.compile("procedure (\\w+)");
	public static final Pattern CUSTOM_FUNCTION = Pattern.compile("function (\\w+)");
	private PlantUmlSettings plantUmlSettings;

	public PlantUmlExternalAnnotator() {
		plantUmlSettings = PlantUmlSettings.getInstance();
	}

	public static class Info {

		@Nullable
		private final Project project;
		private final String text;
		private final VirtualFile virtualFile;

		public Info(@Nullable Project project, String text, VirtualFile virtualFile) {
			this.project = project;
			this.text = text;
			this.virtualFile = virtualFile;
		}

		public Info(PsiFile file) {
			this(null, file.getText(), file.getVirtualFile());
		}
	}


	@Override
	public @Nullable Info collectInformation(@NotNull PsiFile file, @NotNull Editor editor, boolean hasErrors) {
		return new Info(editor.getProject(), file.getText(), file.getVirtualFile());
	}

	@Nullable
	@Override
	public FileAnnotationResult doAnnotate(Info file) {
//        // Temporary solution to avoid execution under read action in dumb mode. Should be removed after IDEA-229905 will be fixed
//        Application application = ApplicationManager.getApplication();
//        if (application != null && application.isReadAccessAllowed() && !application.isUnitTestMode()) {
//            return null;
//        }

		FileAnnotationResult result = new FileAnnotationResult();
		if (plantUmlSettings.isErrorAnnotationEnabled() || plantUmlSettings.isKeywordHighlighting()) {
			String text = file.text;

			Map<Integer, String> sources = SourceExtractor.extractSources(text);

			for (Map.Entry<Integer, String> sourceData : sources.entrySet()) {
				Integer sourceOffset = sourceData.getKey();
				SourceAnnotationResult sourceAnnotationResult = new SourceAnnotationResult(sourceOffset);
				String source = sourceData.getValue();

				if (plantUmlSettings.isErrorAnnotationEnabled()) {
					sourceAnnotationResult.addAll(PlantUmlFacade.get().annotateSyntaxErrors(file.project, source, file.virtualFile));
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
		Matcher functions = LanguagePatternHolder.INSTANCE.functionsPattern.matcher("");
		Matcher tags = LanguagePatternHolder.INSTANCE.tagsPattern.matcher("");
		Matcher commentMatcher = LanguagePatternHolder.INSTANCE.lineCommentPattern.matcher("");
		Matcher procedures = CUSTOM_PROCEDURE.matcher("");
		Matcher customFunctions = CUSTOM_FUNCTION.matcher("");
		Set<String> proceduresSet = new HashSet<>();


		String[] lines = StringUtils.splitPreserveAllTokens(source, "\n");
		int offset = 0;
		for (String line : lines) {
			commentMatcher.reset(line);
			if (commentMatcher.find()) {
				SyntaxHighlightAnnotation lineComment = new SyntaxHighlightAnnotation(offset + commentMatcher.start(), offset + commentMatcher.end(), DefaultLanguageHighlighterColors.LINE_COMMENT);
				result.addWithBlockCommentCheck(lineComment);
			} else {
				if (plantUmlSettings.isKeywordHighlighting()) {
					highlightKeywords(result, keywords, types, keywords2, offset, line);
					highlightCustomProcedureDefinitions(result, offset, line, procedures, proceduresSet);
					highlightCustomProcedureDefinitions(result, offset, line, customFunctions, proceduresSet);
				}
				annotate(result, line, offset, null, DefaultLanguageHighlighterColors.KEYWORD, pluginSettings);
//                annotate(result, line, offset, null, DefaultLanguageHighlighterColors.KEYWORD, preproc); //not needed
				annotate(result, line, offset, null, DefaultLanguageHighlighterColors.METADATA, functions);
				annotate(result, line, offset, null, METADATA, tags);
			}

			offset += line.length() + 1;
		}

		highlightCustomProceduresUsage(result, proceduresSet, lines);
	}

	private void highlightCustomProcedureDefinitions(SourceAnnotationResult result, int offset, String line, Matcher matcher, Set<String> proceduresSet) {
		matcher.reset(line);
		while (matcher.find()) {
			if (matcher.groupCount() > 0) {
				for (int i = 1; i <= matcher.groupCount(); i++) {
					String group = matcher.group(i);
					proceduresSet.add(group);
					SyntaxHighlightAnnotation a = new SyntaxHighlightAnnotation(offset + matcher.start(i), offset + matcher.end(i), DefaultLanguageHighlighterColors.METADATA);
					result.addWithBlockCommentCheck(a);
				}
			}
		}
	}

	private void highlightCustomProceduresUsage(SourceAnnotationResult result, Set<String> proceduresSet, String[] lines) {
		if (!proceduresSet.isEmpty()) {
			Pattern pattern = LanguagePatternHolder.createPattern(proceduresSet, "");
			int offset = 0;
			for (String line : lines) {
				annotate(result, line, offset, null, DefaultLanguageHighlighterColors.METADATA, pattern.matcher(""));
				offset += line.length() + 1;
			}
		}
	}

	private void highlightKeywords(SourceAnnotationResult result, Matcher keywords, Matcher types, Matcher keywords2, int offset, String line) {
		annotate(result, line, offset, List.of(), DefaultLanguageHighlighterColors.STATIC_FIELD, VARIABLES.matcher(""));
		annotate(result, line, offset, List.of(), DefaultLanguageHighlighterColors.KEYWORD, ALL_PROCS.matcher(""));

		int i = line.indexOf(":");  //it seems no keywords are after :
		if (i > 0) {
			line = line.substring(0, i);
		}
		//not reliable when mixed braces ([)...(]), but it don't need to be
        List<kotlin.ranges.IntRange> excludedRanges = join(rangesInside(line, "[", "]"), rangesInside(line, "(", ")"));
		excludedRanges = join(excludedRanges, rangesInside(line, "\"", "\""));

		annotate(result, line, offset, excludedRanges, DefaultLanguageHighlighterColors.KEYWORD, keywords);
		annotate(result, line, offset, excludedRanges, DefaultLanguageHighlighterColors.KEYWORD, keywords2);
		annotate(result, line, offset, excludedRanges, DefaultLanguageHighlighterColors.KEYWORD, types);
	}

    private void annotate(SourceAnnotationResult result, String line, int offset, List<kotlin.ranges.IntRange> excludedRanges, TextAttributesKey textAttributesKey, Matcher matcher) {
		matcher.reset(line);

		while (matcher.find()) {
			if (isExcluded(excludedRanges, matcher.start())) {
				continue;
			}
			SyntaxHighlightAnnotation a = new SyntaxHighlightAnnotation(offset + matcher.start(), offset + matcher.end(), textAttributesKey);
			result.addWithBlockCommentCheck(a);
		}
	}

    private boolean isExcluded(List<kotlin.ranges.IntRange> excludedRanges, int start) {
		if (excludedRanges != null) {
            for (kotlin.ranges.IntRange excludedRange : excludedRanges) {
                if (excludedRange.contains(start)) {
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
