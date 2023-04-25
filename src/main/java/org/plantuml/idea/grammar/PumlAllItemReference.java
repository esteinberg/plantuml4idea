package org.plantuml.idea.grammar;

import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.grammar.psi.PumlItem;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class PumlAllItemReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {


    private final String key;

    public PumlAllItemReference(PsiElement element, String text) {
        super(element);
        key = text;
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        List<ResolveResult> results = new ArrayList<>();
        List<PumlItem> declarationOrUsagesInFile = PumlPsiUtil.findDeclarationInAllFiles(getElement().getProject(), key);
        for (PumlItem pumlItem : declarationOrUsagesInFile) {
            results.add(new PsiElementResolveResult(pumlItem));
        }
        return results.toArray(new ResolveResult[0]);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }


}
