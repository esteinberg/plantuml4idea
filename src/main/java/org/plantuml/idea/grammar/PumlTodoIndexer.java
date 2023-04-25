package org.plantuml.idea.grammar;

import com.intellij.lexer.Lexer;
import com.intellij.psi.impl.cache.impl.BaseFilterLexer;
import com.intellij.psi.impl.cache.impl.OccurrenceConsumer;
import com.intellij.psi.impl.cache.impl.todo.LexerBasedTodoIndexer;
import com.intellij.psi.search.UsageSearchContext;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.PlantUmlParserDefinition;

public class PumlTodoIndexer extends LexerBasedTodoIndexer {
    @NotNull
    @Override
    public Lexer createLexer(@NotNull OccurrenceConsumer consumer) {
        return new BaseFilterLexer(new PumlLexerAdapter(), consumer) {
            @Override
            public void advance() {
                if (PlantUmlParserDefinition.COMMENTS.contains(myDelegate.getTokenType())) {
                    scanWordsInToken(UsageSearchContext.ANY, false, false);
                    advanceTodoItemCountsInToken();
                }

                myDelegate.advance();
            }
        };
    }
}
