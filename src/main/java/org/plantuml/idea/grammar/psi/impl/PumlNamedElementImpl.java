package org.plantuml.idea.grammar.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.grammar.psi.PumlNamedElement;

public abstract class PumlNamedElementImpl extends ASTWrapperPsiElement implements PumlNamedElement {

    public PumlNamedElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @NotNull
    public SearchScope getUseScope() {
        return new LocalSearchScope(this.getContainingFile());
    }
}
