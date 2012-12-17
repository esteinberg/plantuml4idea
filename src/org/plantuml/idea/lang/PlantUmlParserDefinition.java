package org.plantuml.idea.lang;

import com.intellij.lang.ASTFactory;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.EmptyLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PlainTextTokenTypes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Steinberg
 */
public class PlantUmlParserDefinition implements ParserDefinition {
    private static final IFileElementType PLANTUML_FILE_ELEMENT_TYPE = new IFileElementType(PlantUmlLanguage.INSTANCE) {
        public ASTNode parseContents(ASTNode chameleon) {
            final CharSequence chars = chameleon.getChars();
            return ASTFactory.leaf(PlainTextTokenTypes.PLAIN_TEXT, chars);
        }
    };

    @NotNull
    public Lexer createLexer(Project project) {
        return new EmptyLexer();
    }

    @NotNull
    public PsiParser createParser(Project project) {
        throw new UnsupportedOperationException("Not supported");
    }

    public IFileElementType getFileNodeType() {
        return PLANTUML_FILE_ELEMENT_TYPE;
    }

    @NotNull
    public TokenSet getWhitespaceTokens() {
        return TokenSet.EMPTY;
    }

    @NotNull
    public TokenSet getCommentTokens() {
        return TokenSet.EMPTY;
    }

    @NotNull
    public TokenSet getStringLiteralElements() {
        return TokenSet.EMPTY;
    }

    @NotNull
    public PsiElement createElement(ASTNode node) {
        return PsiUtilCore.NULL_PSI_ELEMENT;
    }

    public PsiFile createFile(FileViewProvider viewProvider) {
        return new PlantUmlFileImpl(viewProvider);
    }

    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }
}
