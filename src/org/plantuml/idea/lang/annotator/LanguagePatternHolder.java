package org.plantuml.idea.lang.annotator;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Author: Eugene Steinberg
 * Date: 10/1/14
 */
public enum LanguagePatternHolder {
    INSTANCE;

    private final Joiner PipeJoiner = Joiner.on("|");

    public final Pattern keywordsPattern = createPattern(LanguageDescriptor.INSTANCE.keywords, "");
    public final Pattern typesPattern = createPattern(LanguageDescriptor.INSTANCE.types, "");
    public final Pattern preprocPattern = createPattern(LanguageDescriptor.INSTANCE.preproc, "[@|!]");

    private Pattern createPattern(Collection<String> tokens, final String patternPrefix) {
        Collection<String> tokensAsWords = Collections2.transform(tokens, new Function<String, String>() {
            @Override
            public String apply(String s) {
                return "\\b" + s + "\\b";
            }
        });
        return Pattern.compile("(" + patternPrefix + PipeJoiner.join(tokensAsWords) + ")");
    }
}
