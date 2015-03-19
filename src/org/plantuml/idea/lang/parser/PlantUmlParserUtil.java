package org.plantuml.idea.lang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.tree.IElementType;
import org.plantuml.idea.lang.psi.PlantUmlTokenTypes;

import java.util.regex.Pattern;

/**
 * @author Max Gorbunov
 */
public class PlantUmlParserUtil extends GeneratedParserUtilBase {

    public static boolean consumeUntil(PsiBuilder builder_, int level, Parser rule) {
        return consumeUntil(builder_, level, rule, null);
    }

    public static boolean consumeUntil(PsiBuilder builder_, int level, Parser rule, IElementType remapTo) {
        PsiBuilder.Marker wholeMarker = builder_.mark();
        while (!builder_.eof()) {
            PsiBuilder.Marker marker = builder_.mark();
            if (rule.parse(builder_, level)) {
                marker.rollbackTo();
                break;
            } else {
                marker.drop();
            }
            builder_.advanceLexer();
        }
        if (remapTo != null) {
            wholeMarker.collapse(remapTo);
        } else {
            wholeMarker.drop();
        }
        return true;
    }

    public static boolean consumeToken(PsiBuilder builder_, int level, Parser rule) {
        PsiBuilder.Marker marker = builder_.mark();
        if (rule.parse(builder_, level)) {
            marker.rollbackTo();
            builder_.advanceLexer();
            return true;
        } else {
            marker.rollbackTo();
            return false;
        }
    }

    public static boolean space(PsiBuilder builder_, @SuppressWarnings("UnusedParameters") int level) {
        // Need to call getTokenText() to make sure the next token is fetched, so rawLookup(-1) returns correct value
        builder_.getTokenText();
        return PlantUmlTokenTypes.WHITESPACES.contains(builder_.rawLookup(-1));
    }

    public static boolean circleAny(PsiBuilder builder_, @SuppressWarnings("UnusedParameters") int level) {
        boolean r = builder_.getTokenText() != null && builder_.getTokenText().startsWith("circle");
        if (r) {
            builder_.advanceLexer();
        }
        return r;
    }

    public static boolean xNumber(PsiBuilder builder_, @SuppressWarnings("UnusedParameters") int level) {
        boolean r = builder_.getTokenText() != null && builder_.getTokenText().startsWith("x")
                && Pattern.compile("\\d+(:?\\.\\d+)?").matcher(builder_.getTokenText().substring(1)).matches();
        if (r) {
            builder_.advanceLexer();
        }
        return r;
    }
}
