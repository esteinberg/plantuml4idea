package org.plantuml.idea.language;

import com.intellij.lexer.FlexAdapter;

public class PumlLexerAdapter extends FlexAdapter {

    public PumlLexerAdapter() {
        super(new _PumlLexer(null));
    }

}
