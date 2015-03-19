package org.plantuml.idea.lang;

import com.intellij.lexer.LayeredLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.PlainTextSyntaxHighlighterFactory;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.parser.LexerFactory;
import org.plantuml.idea.lang.psi.PlantUmlTokenTypes;
import org.plantuml.idea.lang.settings.PlantUmlSettings;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Max Gorbunov
 */
public class HighlighterFactory extends SyntaxHighlighterFactory {
    public static final Map<IElementType, TextAttributesKey> MAP = new HashMap<IElementType, TextAttributesKey>();

    public static final SyntaxHighlighterBase SYNTAX_HIGHLIGHTER = new SyntaxHighlighterBase() {
        {
            fillMap(MAP, PlantUmlTokenTypes.META, PlantUmlHighlighterColors.METADATA);
            fillMap(MAP, PlantUmlTokenTypes.KEYWORDS, PlantUmlHighlighterColors.KEYWORD);
            fillMap(MAP, PlantUmlHighlighterColors.STRING, PlantUmlTokenTypes.STRING_LITERAL);
            fillMap(MAP, PlantUmlHighlighterColors.IDENTIFIER, PlantUmlTokenTypes.IDENTIFIER);
            fillMap(MAP, PlantUmlTokenTypes.COMMENTS, PlantUmlHighlighterColors.COMMENT);
            fillMap(MAP, PlantUmlTokenTypes.NUMBERS, PlantUmlHighlighterColors.NUMBER);
        }

        @NotNull
        @Override
        public Lexer getHighlightingLexer() {
            LayeredLexer lexer = new LayeredLexer(LexerFactory.createLexer());
            lexer.registerSelfStoppingLayer(null, new IElementType[]{PlantUmlTokenTypes.START_UML},
                    new IElementType[]{PlantUmlTokenTypes.END_UML});
            return lexer;
        }

        @NotNull
        @Override
        public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
            if (MAP.containsKey(tokenType)) {
                return new TextAttributesKey[]{MAP.get(tokenType)};
            }
            return new TextAttributesKey[0];
        }
    };

    @NotNull
    @Override
    public SyntaxHighlighter getSyntaxHighlighter(Project project, VirtualFile virtualFile) {
        if (PlantUmlSettings.getInstance().isErrorAnnotationEnabled()) {
            return SYNTAX_HIGHLIGHTER;
        } else {
            return new PlainTextSyntaxHighlighterFactory().getSyntaxHighlighter(project, virtualFile);
        }
    }
}
