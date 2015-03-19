package org.plantuml.idea.lang.psi;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.PlantUmlLanguage;

/**
 * @author Max Gorbunov
 */
public class PlantUmlTokenType extends IElementType {
    public PlantUmlTokenType(@NotNull @NonNls String debugName) {
        super(debugName, PlantUmlLanguage.INSTANCE);
    }
}
