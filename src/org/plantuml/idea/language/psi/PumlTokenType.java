package org.plantuml.idea.language.psi;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.PlantUmlLanguage;

public class PumlTokenType extends IElementType {

    public PumlTokenType(@NotNull @NonNls String debugName) {
        super(debugName, PlantUmlLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "PumlTokenType." + super.toString();
    }

}
