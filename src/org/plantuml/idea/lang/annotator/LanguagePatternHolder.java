package org.plantuml.idea.lang.annotator;

import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

import static org.plantuml.idea.lang.annotator.LanguageDescriptor.TAGS;

/**
 * Author: Eugene Steinberg
 * Date: 10/1/14
 */
public enum LanguagePatternHolder {
    INSTANCE;

    private final Joiner PipeJoiner = Joiner.on("|");

    public Pattern sourcePattern = Pattern.compile("(?:(@start(?:" + TAGS + ")(?s).*?(?:@end(?:" + TAGS + ")|$))(?s).*?)+");
    public Pattern sourcePatternMarkdown = Pattern.compile("(?:```plantuml(?s)(.*?```)(?s).*?)+");

    public final Pattern pluginSettingsPattern = createPattern(LanguageDescriptor.INSTANCE.pluginSettingsPattern, "");
    public final Pattern keywordsPattern = createPattern(LanguageDescriptor.INSTANCE.keywords, "");
    public final Pattern keywords2Pattern = createPattern(LanguageDescriptor.INSTANCE.keywords2, "");
    public final Pattern typesPattern = createPattern(LanguageDescriptor.INSTANCE.types, "");
    public final Pattern preprocPattern = createPattern2(LanguageDescriptor.INSTANCE.preproc, "");
    public final Pattern tagsPattern = Pattern.compile("@(start|end)(" + addWordStop(TAGS) + ")");
    public final Pattern lineCommentPattern = Pattern.compile("^\\s*('.*)");
    public final Pattern startBlockComment = Pattern.compile("/'");
    public final Pattern endBlockComment = Pattern.compile("'/");

    private Pattern createPattern(Collection<String> tokens, final String patternPrefix) {
        Collection<String> tokensAsWords = Collections2.transform(tokens, s -> "\\b" + s + "\\b");
        return Pattern.compile("(" + patternPrefix + PipeJoiner.join(tokensAsWords) + ")");
    }

    public Pattern createPattern2(Collection<String> tokens, final String patternPrefix) {
        Collection<String> tokensAsWords = Collections2.transform(tokens, s -> s + "\\b");
        return Pattern.compile("(" + patternPrefix + PipeJoiner.join(tokensAsWords) + ")");
    }


    @NotNull
    public static String addWordStop(String pattern) {
        String[] tokens = pattern.split("\\|");
        Collection<String> tokensAsWords = Collections2.transform(Arrays.asList(tokens), s -> s + "\\b");
        return Joiner.on("|").join(tokensAsWords);
    }

}
