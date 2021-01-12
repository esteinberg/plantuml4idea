package org.plantuml.idea.grammar;

import com.intellij.lexer.FlexAdapter;

public class PumlLexerAdapter extends FlexAdapter {

    public PumlLexerAdapter() {
        super(new PumlLexer(null));
    }

}
