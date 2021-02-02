package org.plantuml.idea.grammar.navigation;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.grammar.psi.PumlElementFactory;
import org.plantuml.idea.grammar.psi.PumlInclude;
import org.plantuml.idea.grammar.psi.impl.PumlIncludeImpl;
import org.plantuml.idea.lang.PlantUmlFileImpl;

import java.util.ArrayList;
import java.util.List;

public class PumlIncludeReference extends PsiReferenceBase<PumlInclude> {

    private static final Logger LOG = Logger.getInstance(PumlIncludeReference.class);

    private final String text;

    public PumlIncludeReference(PumlInclude element, String text) {
        super(element);
        this.text = text;
    }

    @Override
    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        VirtualFile elementDir = getElement().getContainingFile().getVirtualFile().getParent();
        VirtualFile changedFile = element.getContainingFile().getVirtualFile();
        String relativePath = FileUtilRt.getRelativePath(elementDir.getPath(), changedFile.getPath(), '/');
        if (relativePath == null) {
            return null;
        }
        return MyAbstractElementManipulator.handleElementMove(getElement(), relativePath);

    }

    @Nullable
    @Override
    public PsiElement resolve() {
        int i = text.indexOf(" ");
        if (i > 0) {
            String path = text.substring(i).trim();
            VirtualFile virtualFile = getElement().getContainingFile().getVirtualFile();
            VirtualFile fileByRelativePath = virtualFile.getParent().findFileByRelativePath(path);
            if (fileByRelativePath != null) {
                return PsiManager.getInstance(getElement().getProject()).findFile(fileByRelativePath);
            }
        }
        return null;
    }

    @Override
    public @NotNull
    TextRange getRangeInElement() {
        return super.getRangeInElement();
    }

    @Override
    protected TextRange calculateDefaultRangeInElement() {
        return super.calculateDefaultRangeInElement();
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        List<LookupElement> variants = new ArrayList<>();
        return variants.toArray();
    }

    public static class MyAbstractElementManipulator extends AbstractElementManipulator<PumlIncludeImpl> {


        @Override
        public PumlIncludeImpl handleContentChange(@NotNull PumlIncludeImpl element, String newContent) throws IncorrectOperationException {
            return super.handleContentChange(element, newContent);
        }

        @Override
        public @NotNull
        TextRange getRangeInElement(@NotNull PumlIncludeImpl element) {
            int i = element.getFirstChild().getTextLength();
            if (i > 0 && i < element.getTextLength()) {
                return new TextRange(i + 1, element.getTextLength());
            }
            return super.getRangeInElement(element);
        }

        @Override
        public @Nullable
        PumlIncludeImpl handleContentChange(@NotNull PumlIncludeImpl element, @NotNull TextRange range, String newContent) throws IncorrectOperationException {
            String oldText = element.getText();
            String newText;
            int slashIndex = oldText.lastIndexOf("/");
            if (!newContent.contains("/") && slashIndex > 0) {
                int endIndex = Math.min(oldText.length(), slashIndex + 1);
                newText = oldText.substring(0, endIndex) + newContent;
            } else {
                newText = oldText.substring(0, oldText.indexOf(" ") + 1) + newContent;
            }
            PlantUmlFileImpl file = PumlElementFactory.createFile(element.getProject(), newText);
            PumlIncludeImpl firstChild = (PumlIncludeImpl) file.getFirstChild();
            return (PumlIncludeImpl) element.replace(firstChild);
        }

        public static PsiElement handleElementMove(@NotNull PumlInclude element, String newContent) {
            String oldText = element.getText();
            String newText;
            newText = oldText.substring(0, oldText.indexOf(" ") + 1) + newContent;
            PlantUmlFileImpl file = PumlElementFactory.createFile(element.getProject(), newText);
            PumlIncludeImpl firstChild = (PumlIncludeImpl) file.getFirstChild();
            return (PumlIncludeImpl) element.replace(firstChild);
        }

    }
}
