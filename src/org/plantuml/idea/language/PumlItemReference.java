package org.plantuml.idea.language;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.lang.PlantUmlFileType;
import org.plantuml.idea.language.psi.PumlItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PumlItemReference extends PsiReferenceBase<PumlItem> {

    private final String key;

    public PumlItemReference(PumlItem element, String text) {
        super(element);
        key = text;
    }


    @Nullable
    @Override
    public PsiElement resolve() {
        return PumlPsiUtil.findDeclarationInFile(getElement().getContainingFile(), getElement(), key);
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        return super.isReferenceTo(element);
    }

    @Override
    public boolean isSoft() {
        return super.isSoft();
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        PumlItem[] childrenOfType = PsiTreeUtil.getChildrenOfType(myElement.getContainingFile(), PumlItem.class);
        HashSet<String> strings = new HashSet<>();
        List<LookupElement> variants = new ArrayList<>();

        if (childrenOfType != null) {
            for (final PumlItem item : childrenOfType) {
                String text = item.getText();
                if (!strings.contains(text)) {
                    strings.add(text);
                    if (text != null && text.length() > 0) {
                        variants.add(LookupElementBuilder
                                .create(item).withIcon(PlantUmlFileType.PLANTUML_ICON)
                                .withTypeText(item.getContainingFile().getName())
                        );
                    }
                }

            }
        }
        return variants.toArray();
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        PumlItem element = getElement();
        element.setName(newElementName);
        return element;
    }
}
