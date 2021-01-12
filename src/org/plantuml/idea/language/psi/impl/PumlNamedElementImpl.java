package org.plantuml.idea.language.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.language.psi.PumlNamedElement;

public abstract class PumlNamedElementImpl extends ASTWrapperPsiElement implements PumlNamedElement {

    public PumlNamedElementImpl(@NotNull ASTNode node) {
        super(node);
    }

}
