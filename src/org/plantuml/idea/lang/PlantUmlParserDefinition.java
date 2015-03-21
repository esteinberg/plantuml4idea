package org.plantuml.idea.lang;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.*;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.parser.LexerFactory;
import org.plantuml.idea.lang.parser.PlantUmlParser;
import org.plantuml.idea.lang.psi.PlantUmlTokenTypes;
import org.plantuml.idea.lang.settings.PlantUmlSettings;

/**
 * @author Max Gorbunov
 */
public class PlantUmlParserDefinition implements ParserDefinition {
    public static final IFileElementType FILE = new IFileElementType(Language.findInstance(PlantUmlLanguage.class));

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return LexerFactory.createLexer();
    }

    @Override
    public PsiParser createParser(Project project) {
        final PlantUmlParser plantUmlParser = new PlantUmlParser();
        return new PsiParser() {
            @NotNull
            @Override
            public ASTNode parse(IElementType root, PsiBuilder builder) {
                if (PlantUmlSettings.getInstance().isErrorAnnotationEnabled()) {
                    return plantUmlParser.parse(root, builder);
                }
                PsiBuilder.Marker marker = builder.mark();
                while (!builder.eof()) {
                    builder.advanceLexer();
                }
                marker.done(PlantUmlTokenTypes.MULTI_LINE_COMMENT_TEXT);
                return builder.getTreeBuilt();
            }
        };
    }

    @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }

    @NotNull
    @Override
    public TokenSet getWhitespaceTokens() {
        return PlantUmlTokenTypes.WHITESPACES;
    }

    @NotNull
    @Override
    public TokenSet getCommentTokens() {
        return PlantUmlTokenTypes.COMMENTS;
    }

    @NotNull
    @Override
    public TokenSet getStringLiteralElements() {
        return PlantUmlTokenTypes.STRING_LITERALS;
    }

    @NotNull
    @Override
    public PsiElement createElement(ASTNode node) {
        return PlantUmlTokenTypes.Factory.createElement(node);
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new PsiFileBase(viewProvider, PlantUmlLanguage.INSTANCE) {
            @NotNull
            @Override
            public FileType getFileType() {
                return PlantUmlFileType.PLANTUML_FILE_TYPE;
            }
        };
    }

    @Override
    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MUST;    // TODO: needed for code generation
    }
}
