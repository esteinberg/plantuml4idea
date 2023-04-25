package org.plantuml.idea.grammar;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.grammar.psi.PumlTypes;

public class PumlSpellcheckingStrategy extends SpellcheckingStrategy {
//    public static final Tokenizer TAG_TOKENIZER = new Tokenizer() {
//        @Override
//        public void tokenize(@NotNull PsiElement psiElement, TokenConsumer tokenConsumer) {
//            String text = psiElement.getText();
//            String escaped = text.substring(1);
//            tokenConsumer.consumeToken(psiElement, escaped, false, 1, TextRange.allOf(escaped), PlainTextSplitter.getInstance());
//        }
//    };

    @NotNull
    @Override
    public Tokenizer getTokenizer(final PsiElement element) {
        final ASTNode node = element.getNode();
        if (node != null) {
            final IElementType type = node.getElementType();
            if (type == PumlTypes.COMMENT || type == PumlTypes.IDENTIFIER) {
                return TEXT_TOKENIZER;
            }
        }
        return EMPTY_TOKENIZER;
    }
}