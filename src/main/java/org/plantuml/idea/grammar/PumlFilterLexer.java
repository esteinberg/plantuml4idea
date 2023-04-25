// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.plantuml.idea.grammar;

import com.intellij.lexer.Lexer;
import com.intellij.psi.impl.cache.impl.BaseFilterLexer;
import com.intellij.psi.impl.cache.impl.OccurrenceConsumer;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.tree.IElementType;
import org.plantuml.idea.lang.PlantUmlParserDefinition;

final class PumlFilterLexer extends BaseFilterLexer {
    PumlFilterLexer(final Lexer originalLexer, final OccurrenceConsumer table) {
        super(originalLexer, table);
    }

    @Override
    public void advance() {
        final IElementType tokenType = getDelegate().getTokenType();

        if (PlantUmlParserDefinition.COMMENTS.contains(tokenType)) {
            scanWordsInToken(UsageSearchContext.IN_COMMENTS | UsageSearchContext.IN_PLAIN_TEXT, false, false);
            advanceTodoItemCountsInToken();
        } else {
            scanWordsInToken(UsageSearchContext.IN_CODE | UsageSearchContext.IN_FOREIGN_LANGUAGES | UsageSearchContext.IN_PLAIN_TEXT, false, false);
        }

        getDelegate().advance();
    }
}
