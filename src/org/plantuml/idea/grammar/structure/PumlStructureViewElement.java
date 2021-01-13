package org.plantuml.idea.grammar.structure;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.grammar.psi.PumlItem;
import org.plantuml.idea.grammar.psi.PumlTypes;
import org.plantuml.idea.grammar.psi.impl.PumlItemImpl;
import org.plantuml.idea.grammar.psi.impl.PumlPsiImplUtil;
import org.plantuml.idea.lang.PlantUmlFileImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PumlStructureViewElement implements StructureViewTreeElement, SortableTreeElement {

    private final NavigatablePsiElement myElement;
    private final Document document;

    public PumlStructureViewElement(NavigatablePsiElement element, Document document) {
        this.myElement = element;
        this.document = document;
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
        if (myElement instanceof PumlItem) {
            return PumlPsiImplUtil.getPresentation2((PumlItem) myElement, document);
        }
        ItemPresentation presentation = myElement.getPresentation();
        return presentation != null ? presentation : new PresentationData();
    }

    @NotNull
    @Override
    public TreeElement[] getChildren() {
        Document document = PsiDocumentManager.getInstance(myElement.getProject()).getDocument(myElement.getContainingFile());
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
                        treeElements.add(new PumlStructureViewElement(item, document));
                    }
                }
            }


            return treeElements.toArray(new TreeElement[0]);
        }
        return EMPTY_ARRAY;
    }

}
