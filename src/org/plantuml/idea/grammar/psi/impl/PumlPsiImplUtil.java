package org.plantuml.idea.grammar.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.grammar.PumlItemReference;
import org.plantuml.idea.grammar.psi.PumlElementFactory;
import org.plantuml.idea.grammar.psi.PumlItem;
import org.plantuml.idea.lang.PlantUmlFileType;

import javax.swing.*;

public class PumlPsiImplUtil {
    public static String getName(PumlItem element) {
        return element.getText();
    }

    public static PsiElement getNameIdentifier(PumlItem element) {
        return (PsiElement) element.getNode().getFirstChildNode();
    }

    public static PsiElement setName(PumlItem element, String newName) {
        ASTNode keyNode = element.getNode().getFirstChildNode();
        if (keyNode != null) {
            PumlItem property = PumlElementFactory.createWord(element.getProject(), newName);
            ASTNode newKeyNode = property.getFirstChild().getNode();
            element.getNode().replaceChild(keyNode, newKeyNode);
        }
        return element;
    }


    public static ItemPresentation getPresentation(final PumlItem element) {
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

    public static ItemPresentation getPresentation2(final PumlItem element, String line) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return element.getText();
            }

            @Nullable
            @Override
            public String getLocationString() {
                return line;
            }

            @Override
            public Icon getIcon(boolean unused) {
                return PlantUmlFileType.PLANTUML_ICON;
            }
        };
    }

    public static PsiReference getReference(PumlItem element) {
        return new PumlItemReference(element, element.getText());
    }

    @Deprecated
    public static PsiReference[] getReferences(PumlItem element) {
        return new PsiReference[]{getReference(element)};
    }

    public static String toString(PumlItem element) {
        return element.getClass().getSimpleName() + "(" + element.getText() + ")" + element.getTextRange();
    }

}
