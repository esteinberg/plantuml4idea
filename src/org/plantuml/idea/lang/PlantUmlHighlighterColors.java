package org.plantuml.idea.lang;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;

/**
 * @author Max Gorbunov
 */
public class PlantUmlHighlighterColors {
    public static final TextAttributesKey METADATA = TextAttributesKey.createTextAttributesKey("PLANTUML.METADATA",
            DefaultLanguageHighlighterColors.METADATA);
    public static final TextAttributesKey KEYWORD = TextAttributesKey.createTextAttributesKey("PLANTUML.KEYWORD",
            DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey STRING = TextAttributesKey.createTextAttributesKey("PLANTUML.STRING",
            DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey IDENTIFIER = TextAttributesKey.createTextAttributesKey("PLANTUML.IDENTIFIER",
            DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey COMMENT = TextAttributesKey.createTextAttributesKey("PLANTUML.COMMENT",
            DefaultLanguageHighlighterColors.DOC_COMMENT);
    public static final TextAttributesKey NUMBER = TextAttributesKey.createTextAttributesKey("PLANTUML.NUMBER",
            DefaultLanguageHighlighterColors.NUMBER);
}
