package org.plantuml.idea.lang;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.annotator.LanguageDescriptor;
import org.plantuml.idea.lang.settings.PlantUmlSettings;

import java.util.Collections;
import java.util.List;

public class PlantUmlCompletionContributor extends CompletionContributor implements DumbAware {
    private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(PlantUmlCompletionContributor.class);

    public PlantUmlCompletionContributor() {
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement(),
                new SimpleProvider(LanguageDescriptor.INSTANCE.keywords));
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement(),
                new SimpleProvider(LanguageDescriptor.INSTANCE.types));
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement(),
                new SimpleProvider(LanguageDescriptor.INSTANCE.keywordsWithoutHighlight));

    }

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        int offset = parameters.getOffset();
        Document document = parameters.getEditor().getDocument();
        final int lineStartOffset = document.getLineStartOffset(document.getLineNumber(offset));
        String text = document.getText(TextRange.create(lineStartOffset, offset));
        if (text.matches("\\s*!\\S*$")) {
            for (String s : LanguageDescriptor.INSTANCE.preproc) {
                result.addElement(LookupElementBuilder.create(s.substring(1)).withPresentableText(s).withCaseSensitivity(true).bold());
            }
        } else {
            super.fillCompletionVariants(parameters, result);
            if (PlantUmlSettings.getInstance().isAutoComplete()) {
                WordCompletionContributor.addWordCompletionVariants(result, parameters, Collections.emptySet());
            }
        }


    }

    class SimpleProvider extends CompletionProvider<CompletionParameters> {
        private final List<String> myItems;

        SimpleProvider(List<String> items) {
            myItems = items;
        }

        @Override
        public void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
            for (String item : myItems) {
                result.addElement(LookupElementBuilder.create(item).withCaseSensitivity(true).withItemTextItalic(true));
            }
        }
    }

}
