package org.plantuml.idea.util;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PsiUtil {
    @NotNull
    public static Project getProjectInReadAction(@NotNull final PsiElement element) {
        return ReadAction.compute(() -> element.getProject());
    }

    @Nullable
    public static VirtualFile getVirtualFile(@Nullable PsiElement element) {
        // optimisation: call isValid() on file only to reduce walks up and down
        if (element == null) {
            return null;
        }
        if (element instanceof PsiFileSystemItem) {
            return element.isValid() ? ((PsiFileSystemItem) element).getVirtualFile() : null;
        }
        final PsiFile containingFile = element.getContainingFile();
        if (containingFile == null || !containingFile.isValid()) {
            return null;
        }

        VirtualFile file = containingFile.getVirtualFile();
        if (file == null) {
            PsiFile originalFile = containingFile.getOriginalFile();
            if (originalFile != containingFile && originalFile.isValid()) {
                file = originalFile.getVirtualFile();
            }
        }
        return file;
    }

    public static PsiElement findPsiElement(@NotNull PsiElement root, @NotNull String element) {
        PsiElement target = null;
        for (PsiNameIdentifierOwner call : SyntaxTraverser.psiTraverser().withRoot(root).filter(PsiNameIdentifierOwner.class)) {
            PsiElement nameIdentifier = call.getNameIdentifier();
            if (nameIdentifier != null && nameIdentifier.textMatches(element)) {
                target = call;
                break;
            }
        }
        return target;
    }

    @Contract("null -> null;!null -> !null")
    public static IElementType getElementType(@Nullable ASTNode node) {
        return node == null ? null : node.getElementType();
    }

    @Contract("null -> null")
    public static IElementType getElementType(@Nullable PsiElement element) {
        return element == null ? null :
                element instanceof StubBasedPsiElement ? ((StubBasedPsiElement<?>) element).getElementType() :
                        element instanceof PsiFile ? ((PsiFile) element).getFileElementType() :
                                getElementType(element.getNode());
    }
}
