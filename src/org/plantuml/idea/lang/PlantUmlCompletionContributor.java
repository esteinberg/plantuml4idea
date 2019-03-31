package org.plantuml.idea.lang;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.WordCompletionContributor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.settings.PlantUmlSettings;

import java.util.Collections;

public class PlantUmlCompletionContributor extends CompletionContributor implements DumbAware {
    private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(PlantUmlCompletionContributor.class);

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        if (PlantUmlSettings.getInstance().isAutoComplete()) {
            WordCompletionContributor.addWordCompletionVariants(result, parameters, Collections.emptySet());
        }
    }
}
