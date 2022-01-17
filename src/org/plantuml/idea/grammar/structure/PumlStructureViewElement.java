package org.plantuml.idea.grammar.structure;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.grammar.psi.PumlInclude;
import org.plantuml.idea.grammar.psi.PumlItem;
import org.plantuml.idea.grammar.psi.PumlTypes;
import org.plantuml.idea.grammar.psi.impl.PumlItemImpl;
import org.plantuml.idea.grammar.psi.impl.PumlPsiImplUtil;
import org.plantuml.idea.lang.PlantUmlFileImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PumlStructureViewElement implements StructureViewTreeElement, SortableTreeElement {
    private static final Logger LOG = Logger.getInstance(PumlStructureViewElement.class);

    private final NavigatablePsiElement myElement;
    private final Document document;
    private String line;

    public PumlStructureViewElement(NavigatablePsiElement element, Document document) {
        this.myElement = element;
        this.document = document;
        try {
            if (element.isValid()) {
                line = "line: " + document.getLineNumber(element.getTextOffset()) + 1;
            }
        } catch (Throwable e) {
            //old document, ignore, (maybe #isValid solves it, dunno)
            LOG.debug(e);
        }
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
            return PumlPsiImplUtil.getPresentation2(myElement, line);
        }
        if (myElement instanceof PumlInclude) {
            return PumlPsiImplUtil.getPresentation2(myElement, line);
        }
        ItemPresentation presentation = myElement.getPresentation();
        return presentation != null ? presentation : new PresentationData();
    }

    @NotNull
    @Override
    public TreeElement[] getChildren() {
        if (myElement instanceof PlantUmlFileImpl) {
            List<TreeElement> treeElements = new ArrayList<>();
            List<PsiElement> list = PsiTreeUtil.getChildrenOfAnyType(myElement, PumlInclude.class, PumlItem.class);

            HashSet<String> strings = new HashSet<>();
            for (final PsiElement item : list) {
                if (item instanceof PumlItemImpl) {
                    if (item.getFirstChild().getNode().getElementType() != PumlTypes.IDENTIFIER) {
                        continue;
                    }
                }
                String text = item.getText();
                if (!strings.contains(text)) {
                    strings.add(text);
                    if (text != null && text.length() > 0) {
                        treeElements.add(new PumlStructureViewElement((NavigatablePsiElement) item, document));
                    }
                }
            }

            return treeElements.toArray(new TreeElement[0]);
        }
        return EMPTY_ARRAY;
    }

}
