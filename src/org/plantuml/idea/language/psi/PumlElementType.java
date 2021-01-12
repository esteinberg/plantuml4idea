package org.plantuml.idea.language.psi;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.PlantUmlLanguage;

public class PumlElementType extends IElementType {

    public PumlElementType(@NotNull @NonNls String debugName) {
        super(debugName, PlantUmlLanguage.INSTANCE);
    }

}
