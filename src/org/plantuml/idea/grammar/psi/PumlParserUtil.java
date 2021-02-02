package org.plantuml.idea.grammar.psi;

import com.intellij.lang.SyntaxTreeBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;

public class PumlParserUtil extends GeneratedParserUtilBase {
    public static boolean notNewLine(SyntaxTreeBuilder b, @SuppressWarnings("UnusedParameters") int level) {
        if (b.getTokenType() == PumlTypes.NEW_LINE) return false;
        if (b.getTokenType() == null) return false;
        b.advanceLexer();

        return true;
    }
}
