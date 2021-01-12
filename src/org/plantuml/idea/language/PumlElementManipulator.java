package org.plantuml.idea.language;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.language.psi.PumlWord;

public class PumlElementManipulator extends AbstractElementManipulator<PumlWord> {
    @Override
    public @Nullable
    PumlWord handleContentChange(@NotNull PumlWord pumlWord, @NotNull TextRange textRange, String s) throws IncorrectOperationException {
        PsiElement psiElement = pumlWord.setName(s);
        return (PumlWord) psiElement;
    }
}
