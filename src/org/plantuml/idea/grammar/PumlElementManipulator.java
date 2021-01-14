package org.plantuml.idea.grammar;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.grammar.psi.PumlItem;

public class PumlElementManipulator extends AbstractElementManipulator<PumlItem> {
    @Override
    public @Nullable
    PumlItem handleContentChange(@NotNull PumlItem PumlItem, @NotNull TextRange textRange, String s) throws IncorrectOperationException {
        PsiElement psiElement = PumlItem.setName(s);
        return (PumlItem) psiElement;
    }
}
