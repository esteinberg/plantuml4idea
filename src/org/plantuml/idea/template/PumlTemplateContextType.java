package org.plantuml.idea.template;

import com.intellij.codeInsight.template.TemplateActionContext;
import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.PlantUmlLanguage;

public class PumlTemplateContextType extends TemplateContextType {
    public PumlTemplateContextType() {
        super("PUML", "PlantUML");
    }

    @Override
    public boolean isInContext(@NotNull TemplateActionContext templateActionContext) {
        PsiFile file = templateActionContext.getFile();
        return file.getLanguage().isKindOf(PlantUmlLanguage.INSTANCE);
    }
}
