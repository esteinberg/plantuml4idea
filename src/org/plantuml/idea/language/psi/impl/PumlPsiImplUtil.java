package org.plantuml.idea.language.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.lang.PlantUmlFileType;
import org.plantuml.idea.language.PumlWordReference;
import org.plantuml.idea.language.psi.PumlElementFactory;
import org.plantuml.idea.language.psi.PumlTypes;
import org.plantuml.idea.language.psi.PumlWord;

import javax.swing.*;

public class PumlPsiImplUtil {
    public static String getName(PumlWord element) {
        return element.getText();
    }

    public static PsiElement getNameIdentifier(PumlWord element) {
        return element.getNode().findChildByType(PumlTypes.IDENTIFIER).getPsi();
    }

    public static PsiElement setName(PumlWord element, String newName) {
        ASTNode keyNode = element.getNode().findChildByType(PumlTypes.IDENTIFIER);
        if (keyNode != null) {
            PumlWord property = PumlElementFactory.createWord(element.getProject(), newName);
            ASTNode newKeyNode = property.getFirstChild().getNode();
            element.getNode().replaceChild(keyNode, newKeyNode);
        }
        return element;
    }


    public static ItemPresentation getPresentation(final PumlWord element) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return element.getText();
            }

            @Nullable
            @Override
            public String getLocationString() {
                PsiFile containingFile = element.getContainingFile();
                return containingFile == null ? null : containingFile.getName();
            }

            @Override
            public Icon getIcon(boolean unused) {
                return PlantUmlFileType.PLANTUML_ICON;
            }
        };
    }


    public static PsiReference[] getReferences(PumlWord element) {
        return new PsiReference[]{new PumlWordReference(element, element.getText())};
    }

    public static String toString(PumlWord element) {
        return element.getClass().getSimpleName() + "(" + element.getNode().getElementType().toString() + "-" + element.getText() + ")";
    }

}
