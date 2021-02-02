// This is a generated file. Not intended for manual editing.
package org.plantuml.idea.grammar.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LightPsiParser;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;

import static org.plantuml.idea.grammar.psi.PumlParserUtil.*;
import static org.plantuml.idea.grammar.psi.PumlTypes.*;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class PumlParser implements PsiParser, LightPsiParser {

    public ASTNode parse(IElementType t, PsiBuilder b) {
        parseLight(t, b);
        return b.getTreeBuilt();
    }

    public void parseLight(IElementType t, PsiBuilder b) {
        boolean r;
        b = adapt_builder_(t, b, this, null);
        Marker m = enter_section_(b, 0, _COLLAPSE_, null);
        r = parse_root_(t, b);
        exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
    }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return simpleFile(b, l + 1);
  }

  /* ********************************************************** */
  // '!include'<<notNewLine>>*
  public static boolean include(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "include")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INCLUDE, "<include>");
    r = consumeToken(b, "!include");
    r = r && include_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // <<notNewLine>>*
  private static boolean include_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "include_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!notNewLine(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "include_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER|OTHER
  public static boolean item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "item")) return false;
    if (!nextTokenIs(b, "<item>", IDENTIFIER, OTHER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ITEM, "<item>");
    r = consumeToken(b, IDENTIFIER);
    if (!r) r = consumeToken(b, OTHER);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // (include|item|COMMENT|NEW_LINE)*
    static boolean simpleFile(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "simpleFile")) return false;
        while (true) {
            int c = current_position_(b);
            if (!simpleFile_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "simpleFile", c)) break;
        }
        return true;
    }

  // include|item|COMMENT|NEW_LINE
    private static boolean simpleFile_0(PsiBuilder b, int l) {
      if (!recursion_guard_(b, l, "simpleFile_0")) return false;
      boolean r;
      r = include(b, l + 1);
      if (!r) r = item(b, l + 1);
      if (!r) r = consumeToken(b, COMMENT);
      if (!r) r = consumeToken(b, NEW_LINE);
      return r;
    }

}
