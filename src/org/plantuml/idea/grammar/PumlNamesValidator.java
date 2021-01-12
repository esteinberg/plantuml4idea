package org.plantuml.idea.grammar;

import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static org.apache.commons.lang.StringUtils.containsNone;

public class PumlNamesValidator implements NamesValidator {
    @Override
    public boolean isKeyword(@NotNull String name, Project project) {
        return false;
    }

    @Override
    public boolean isIdentifier(@NotNull String name, Project project) {
        return (!name.isEmpty() && containsNone(name, new char[]{' ', '\t'}))
                || (name.startsWith("[") && name.endsWith("]"))
                || (name.startsWith("(") && name.endsWith(")"));
    }
}