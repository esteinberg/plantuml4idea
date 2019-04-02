package org.plantuml.idea.lang.annotator;

import org.junit.Test;

import java.util.regex.Matcher;

import static org.junit.Assert.*;

public class LanguagePatternHolderTest {

    private static final String TEXT = "@startuml\n" +
            "' This is a comment on a single line\n" +
            "Bob->Alice : hello\n" +
            "/' It is a comment \n" +
            "for\n" +
            "multiple lines'/\n" +
            "@enduml";

    @Test
    public void testLineCommentPattern() {
        Matcher matcher = LanguagePatternHolder.INSTANCE.lineCommentPattern.matcher(TEXT);

        assertTrue(matcher.find());
    }

    @Test
    public void testBlockCommentPattern() {
        Matcher matcher = LanguagePatternHolder.INSTANCE.blockCommentPattern.matcher(TEXT);

        assertTrue(matcher.find());
    }
}