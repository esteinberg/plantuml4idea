// This is a generated file. Not intended for manual editing.
package org.plantuml.idea.language.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

public class PumlVisitor extends PsiElementVisitor {

  public void visitWord(@NotNull PumlWord o) {
    visitNamedElement(o);
  }

  public void visitNamedElement(@NotNull PumlNamedElement o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
