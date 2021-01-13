package org.plantuml.idea.grammar.structure;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.grammar.psi.PumlTypes;
import org.plantuml.idea.grammar.psi.impl.PumlItemImpl;
import org.plantuml.idea.lang.PlantUmlFileImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PumlStructureViewElement implements StructureViewTreeElement, SortableTreeElement {

    private final NavigatablePsiElement myElement;

    public PumlStructureViewElement(NavigatablePsiElement element) {
        this.myElement = element;
    }

    @Override
    public Object getValue() {
        return myElement;
    }

    @Override
    public void navigate(boolean requestFocus) {
        myElement.navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return myElement.canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return myElement.canNavigateToSource();
    }

    @NotNull
    @Override
    public String getAlphaSortKey() {
        String name = myElement.getName();
        return name != null ? name : "";
    }

    @NotNull
    @Override
    public ItemPresentation getPresentation() {
        ItemPresentation presentation = myElement.getPresentation();
        return presentation != null ? presentation : new PresentationData();
    }

    @NotNull
    @Override
    public TreeElement[] getChildren() {
        if (myElement instanceof PlantUmlFileImpl) {
            List<TreeElement> treeElements = new ArrayList<>();
            List<PumlItemImpl> list = PsiTreeUtil.getChildrenOfTypeAsList(myElement, PumlItemImpl.class);

            HashSet<String> strings = new HashSet<>();
            for (final PumlItemImpl item : list) {
                if (item.getFirstChild().getNode().getElementType() != PumlTypes.IDENTIFIER) {
                    continue;
                }
                String text = item.getText();
                if (!strings.contains(text)) {
                    strings.add(text);
                    if (text != null && text.length() > 0) {
                        treeElements.add(new PumlStructureViewElement(item));
                    }
                }
            }


            return treeElements.toArray(new TreeElement[0]);
        }
        return EMPTY_ARRAY;
    }

}
