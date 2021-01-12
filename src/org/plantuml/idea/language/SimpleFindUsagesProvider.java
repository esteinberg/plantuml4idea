package org.plantuml.idea.language;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.language.psi.PumlItem;
import org.plantuml.idea.language.psi.PumlTypes;

public class SimpleFindUsagesProvider implements FindUsagesProvider {

    @Nullable
    @Override
    public WordsScanner getWordsScanner() {
        return new DefaultWordsScanner(new PumlLexerAdapter(),
                TokenSet.create(PumlTypes.IDENTIFIER),
                TokenSet.create(PumlTypes.COMMENT),
                TokenSet.EMPTY);
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        boolean b = psiElement instanceof PumlItem
                && psiElement.getReference().resolve() == null;  //only declaration returns null
        return b;
    }

    @Nullable
    @Override
    public String getHelpId(@NotNull PsiElement psiElement) {
        return null;
    }

    @NotNull
    @Override
    public String getType(@NotNull PsiElement element) {
        if (element instanceof PumlItem) {
            return "PumlItem";
        } else {
            return "";
        }
    }

    @NotNull
    @Override
    public String getDescriptiveName(@NotNull PsiElement element) {
        if (element instanceof PumlItem) {
            return element.getText();
        } else {
            return "";
        }
    }

    @NotNull
    @Override
    public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        if (element instanceof PumlItem) {
            return element.getText();
        } else {
            return "";
        }
    }

}
