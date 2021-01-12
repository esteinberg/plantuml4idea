package org.plantuml.idea.language;

import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class PumlNamesValidator implements NamesValidator {
    @Override
    public boolean isKeyword(@NotNull String name, Project project) {
        return false;
    }

    @Override
    public boolean isIdentifier(@NotNull String name, Project project) {
        return !name.isEmpty();
    }
}