package org.plantuml.idea.lang.psi;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.PlantUmlLanguage;

/**
 * @author Max Gorbunov
 */
public class PlantUmlElementType extends IElementType {
    public PlantUmlElementType(@NotNull @NonNls String debugName) {
        super(debugName, PlantUmlLanguage.INSTANCE);
    }
}
