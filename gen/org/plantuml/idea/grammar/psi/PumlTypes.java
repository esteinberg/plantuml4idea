// This is a generated file. Not intended for manual editing.
package org.plantuml.idea.grammar.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.plantuml.idea.grammar.psi.impl.PumlItemImpl;

public interface PumlTypes {

  IElementType ITEM = new PumlElementType("ITEM");

  IElementType COMMENT = new PumlTokenType("COMMENT");
  IElementType IDENTIFIER = new PumlTokenType("IDENTIFIER");
  IElementType NEW_LINE_INDENT = new PumlTokenType("NEW_LINE_INDENT");
  IElementType WHITE_SPACE = new PumlTokenType("WHITE_SPACE");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ITEM) {
        return new PumlItemImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
