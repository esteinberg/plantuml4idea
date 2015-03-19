package org.plantuml.idea.lang.parser;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;

import java.io.Reader;

/**
 * @author Max Gorbunov
 */
public class LexerFactory {

    public static Lexer createLexer() {
        return new FlexAdapter(new _PlantUmlLexer((Reader) null));
    }
}
