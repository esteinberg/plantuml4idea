// This is a generated file. Not intended for manual editing.
package org.plantuml.idea.language.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.language.psi.PumlItem;
import org.plantuml.idea.language.psi.PumlVisitor;

public class PumlItemImpl extends PumlNamedElementImpl implements PumlItem {

  public PumlItemImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PumlVisitor visitor) {
    visitor.visitItem(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PumlVisitor) accept((PumlVisitor) visitor);
    else super.accept(visitor);
  }

  @Override
  public String getName() {
    return PumlPsiImplUtil.getName(this);
  }

  @Override
  public PsiElement setName(String newName) {
    return PumlPsiImplUtil.setName(this, newName);
  }

  @Override
  public PsiElement getNameIdentifier() {
    return PumlPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  public ItemPresentation getPresentation() {
    return PumlPsiImplUtil.getPresentation(this);
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
