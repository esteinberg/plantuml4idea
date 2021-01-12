
package org.plantuml.idea.language;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.language.psi.PumlWord;

public class PumlReferenceContributor extends PsiReferenceContributor {
    public PumlReferenceContributor() {
    }

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(PumlWord.class),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element,
                                                                 @NotNull ProcessingContext context) {
                        return new PsiReference[]{new PumlWordReference((PumlWord) element, element.getText())};
                    }
                });
    }

}
