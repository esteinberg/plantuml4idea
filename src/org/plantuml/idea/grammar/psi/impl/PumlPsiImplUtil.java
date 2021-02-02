package org.plantuml.idea.grammar.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.grammar.navigation.PumlIncludeReference;
import org.plantuml.idea.grammar.navigation.PumlItemReference;
import org.plantuml.idea.grammar.psi.PumlElementFactory;
import org.plantuml.idea.grammar.psi.PumlInclude;
import org.plantuml.idea.grammar.psi.PumlItem;
import org.plantuml.idea.lang.PlantUmlFileType;

import javax.swing.*;

public class PumlPsiImplUtil {


    public static String getName(PumlInclude element) {
        String text = element.getText();
        int i = text.indexOf(" ");
        if (i > 0) {
            text = text.substring(i).trim();
        }
        return text;
    }

    public static PsiElement setName(PumlInclude element, String newName) {
        ASTNode keyNode = element.getNode().getFirstChildNode();
        if (keyNode != null) {
            PumlItem property = PumlElementFactory.createWord(element.getProject(), newName);
            ASTNode newKeyNode = property.getFirstChild().getNode();
            element.getNode().replaceChild(keyNode, newKeyNode);
        }
        return element;
    }

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

    public static ItemPresentation getPresentation2(final PsiElement element, String line) {
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

    public static PsiReference getReference(PumlInclude element) {
        return new PumlIncludeReference(element, element.getText());
    }

    @Deprecated
    public static PsiReference[] getReferences(PumlItem element) {
        return new PsiReference[]{getReference(element)};
    }

    public static String toString(PsiElement element) {
        return element.getClass().getSimpleName() + "(" + element.getText() + ")" + element.getTextRange();
    }

}
