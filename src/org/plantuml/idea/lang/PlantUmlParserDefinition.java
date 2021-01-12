package org.plantuml.idea.lang;

import com.intellij.lang.ASTFactory;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.EmptyLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.language.PumlLexerAdapter;
import org.plantuml.idea.language.parser.PumlParser;
import org.plantuml.idea.language.psi.PumlTypes;

/**
 * @author Eugene Steinberg
 */
public class PlantUmlParserDefinition implements ParserDefinition {

    public static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);
    public static final TokenSet COMMENTS = TokenSet.create(PumlTypes.COMMENT);

    public static final IFileElementType FILE = new IFileElementType(PlantUmlLanguage.INSTANCE);


    private static final IFileElementType PLANTUML_FILE_ELEMENT_TYPE = new IFileElementType(PlantUmlLanguage.INSTANCE) {
        @Override
        public ASTNode parseContents(ASTNode chameleon) {
            final CharSequence chars = chameleon.getChars();
            return ASTFactory.leaf(PlainTextTokenTypes.PLAIN_TEXT, chars);
        }
    };
    private PlantUmlSettings plantUmlSettings;

    public PlantUmlParserDefinition() {
        plantUmlSettings = PlantUmlSettings.getInstance();
    }

    @Override
    @NotNull
    public Lexer createLexer(Project project) {
        if (enabled()) {
            return new PumlLexerAdapter();
        } else {
            return new EmptyLexer();
        }
    }


    @Override
    @NotNull
    public PsiParser createParser(Project project) {
        if (enabled()) {
            return new PumlParser();
        } else {
            throw new UnsupportedOperationException("Not supported");
        }
    }

    @Override
    public IFileElementType getFileNodeType() {
        if (enabled()) {
            return FILE;
        } else {
            return PLANTUML_FILE_ELEMENT_TYPE;
        }
    }

    @Override
    @NotNull
    public TokenSet getWhitespaceTokens() {
        return WHITE_SPACES;
    }

    @Override
    @NotNull
    public TokenSet getCommentTokens() {
        return COMMENTS;
    }

    @Override
    @NotNull
    public TokenSet getStringLiteralElements() {
        return TokenSet.EMPTY;
    }

    @NotNull
    @Override
    public PsiElement createElement(ASTNode node) {
        if (enabled()) {
            return PumlTypes.Factory.createElement(node);
        } else {
            return PsiUtilCore.NULL_PSI_ELEMENT;
        }
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new PlantUmlFileImpl(viewProvider);
    }

    @Override
    public SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }

    private boolean enabled() {
        return plantUmlSettings.isUseGrammar();
    }
}
