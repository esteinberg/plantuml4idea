// This is a generated file. Not intended for manual editing.
package org.plantuml.idea.grammar.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.grammar.psi.PumlInclude;
import org.plantuml.idea.grammar.psi.PumlVisitor;

public class PumlIncludeImpl extends ASTWrapperPsiElement implements PumlInclude {

    public PumlIncludeImpl(@NotNull ASTNode node) {
        super(node);
    }

    public void accept(@NotNull PumlVisitor visitor) {
        visitor.visitInclude(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof PumlVisitor) accept((PumlVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    public PsiReference getReference() {
        return PumlPsiImplUtil.getReference(this);
    }

    @Override
    public String toString() {
        return PumlPsiImplUtil.toString(this);
    }

}
