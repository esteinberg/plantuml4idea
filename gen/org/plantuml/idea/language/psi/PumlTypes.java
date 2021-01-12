// This is a generated file. Not intended for manual editing.
package org.plantuml.idea.language.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.plantuml.idea.language.psi.impl.PumlWordImpl;

public interface PumlTypes {

  IElementType WORD = new PumlElementType("WORD");

  IElementType COMMENT = new PumlTokenType("COMMENT");
  IElementType IDENTIFIER = new PumlTokenType("IDENTIFIER");
  IElementType NEW_LINE_INDENT = new PumlTokenType("NEW_LINE_INDENT");
  IElementType WHITE_SPACE = new PumlTokenType("WHITE_SPACE");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == WORD) {
        return new PumlWordImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
