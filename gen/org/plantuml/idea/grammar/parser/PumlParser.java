// This is a generated file. Not intended for manual editing.
package org.plantuml.idea.grammar.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LightPsiParser;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;

import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
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
    // (item|COMMENT)*
    static boolean simpleFile(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "simpleFile")) return false;
        while (true) {
            int c = current_position_(b);
            if (!simpleFile_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "simpleFile", c)) break;
        }
        return true;
    }

    // item|COMMENT
    private static boolean simpleFile_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "simpleFile_0")) return false;
        boolean r;
        r = item(b, l + 1);
        if (!r) r = consumeToken(b, COMMENT);
        return r;
    }

}
