package org.plantuml.idea.language;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.lang.PlantUmlFileType;
import org.plantuml.idea.language.psi.PumlWord;

import java.util.ArrayList;
import java.util.List;

public class PumlWordReference extends PsiReferenceBase<PumlWord> implements PsiPolyVariantReference {

    private final String key;

    public PumlWordReference(PumlWord element, String text) {
        super(element);
        key = text;
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        Project project = myElement.getProject();
        final List<PumlWord> properties = SimpleUtil.findFirstInFile(getElement().getContainingFile(), key);
        List<ResolveResult> results = new ArrayList<>();
        for (PumlWord property : properties) {
            results.add(new PsiElementResolveResult(property));
        }
        return results.toArray(new ResolveResult[results.size()]);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        Project project = myElement.getProject();
        List<PumlWord> properties = SimpleUtil.findAll(project);
        List<LookupElement> variants = new ArrayList<>();
        for (final PumlWord property : properties) {
            if (property.getText() != null && property.getText().length() > 0) {
                variants.add(LookupElementBuilder
                        .create(property).withIcon(PlantUmlFileType.PLANTUML_ICON)
                        .withTypeText(property.getContainingFile().getName())
                );
            }
        }
        return variants.toArray();
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        PumlWord element = getElement();
        element.setName(newElementName);
        return element;
    }
}
