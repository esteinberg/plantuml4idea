package org.plantuml.idea.intentions;

import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.annotator.LanguageDescriptor;

public class AddNoSyntaxCheckOptionIntention extends AddPartialRenderOptionIntention {
    @NotNull
    @Override
    public String getFamilyName() {
        return "Disable Syntax Check";
    }

    @Override
    @NotNull
    protected String option() {
        return LanguageDescriptor.IDEA_DISABLE_SYNTAX_CHECK;
    }

}
