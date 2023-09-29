package org.plantuml.idea.grammar.navigation;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.grammar.PumlPsiUtil;
import org.plantuml.idea.grammar.psi.PumlItem;
import org.plantuml.idea.grammar.psi.PumlTypes;
import org.plantuml.idea.lang.PlantUmlFileType;
import org.plantuml.idea.preview.image.links.MyMouseAdapter;
import org.plantuml.idea.util.PsiUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class PumlItemReference extends PsiReferenceBase<PumlItem> {

    private static final Logger LOG = Logger.getInstance(PumlItemReference.class);

    private final String key;

    public PumlItemReference(PumlItem element, String text) {
        super(element);
        key = text;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
//        if (key.contains(".")) {
//            PsiElement prevSibling = myElement.getPrevSibling();
//            System.err.println();
//            VirtualFile virtualFile = getElement().getContainingFile().getVirtualFile();
//            VirtualFile fileByRelativePath = virtualFile.getParent().findFileByRelativePath(key);
//            if (fileByRelativePath != null) {
//                return PsiManager.getInstance(getElement().getProject()).findFile(fileByRelativePath);
//            }
//        }
        if ("[[]".equals(key)) {
            return null;
        }

        PumlItem declarationInFile = PumlPsiUtil.findDeclarationInFile(getElement().getContainingFile(), getElement(), key);

        if (declarationInFile == null) {
            return resolveFile();
        }

        return declarationInFile;
    }

    /**
     * just like {@link MyMouseAdapter#openFile(String)}
     */
    private PsiElement resolveFile() {
        PsiElement target = null;
        if (key.startsWith("[[") && key.endsWith("]")) {
            String targetFilePath = key.substring(2);
            if (targetFilePath.endsWith("]")) {
                targetFilePath = targetFilePath.substring(0, targetFilePath.length() - 1);
            }
            if (StringUtils.isEmpty(targetFilePath)) {
                return null;
            }
            targetFilePath = StringUtils.substringBefore(targetFilePath, " ");
            String[] split = targetFilePath.split("#");
            targetFilePath = split[0];
            String element = split.length >= 2 ? split[1] : null;

            Project project = PsiUtil.getProjectInReadAction(myElement);
            PsiFile containingFile = myElement.getContainingFile();
            VirtualFile sourceFile = PsiUtil.getVirtualFile(containingFile);

            PsiManager psiManager = PsiManager.getInstance(project);
            LocalFileSystem localFileSystem = LocalFileSystem.getInstance();

            if (sourceFile != null) {
                VirtualFile parent = sourceFile.getParent();
                if (parent != null) {
                    target = resolveFile(new File(parent.getPath(), targetFilePath), localFileSystem, psiManager);
                }
            }
            if (target == null) {
                target = resolveFile(new File(targetFilePath), localFileSystem, psiManager);
            }

            if (target == null) {
                Module module = ModuleUtilCore.findModuleForFile(containingFile);
                if (module != null) {
                    VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
                    for (VirtualFile contentRoot : contentRoots) {
                        target = resolveFile(new File(contentRoot.getPath(), targetFilePath), localFileSystem, psiManager);
                        if (target != null) {
                            break;
                        }
                    }
                }
            }
            if (target != null && element != null) {
                PsiElement psiElement = PsiUtil.findPsiElement(target, element);
                if (psiElement != null) {
                    target = psiElement;
                }
            }
        }
        return target;
    }

    @Nullable
    private static PsiFile resolveFile(File file, LocalFileSystem localFileSystem, @NotNull PsiManager psiManager) {
        PsiFile simpleFile = null;
        VirtualFile sourceFile = localFileSystem.findFileByPath(file.getPath());
        if (sourceFile != null && sourceFile.exists()) {
            simpleFile = psiManager.findFile(sourceFile);
        }
        return simpleFile;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        PumlItem[] childrenOfType = PsiTreeUtil.getChildrenOfType(myElement.getContainingFile(), PumlItem.class);
        HashSet<String> added = new HashSet<>();
        List<LookupElement> variants = new ArrayList<>();

        if (childrenOfType != null) {
            for (final PumlItem item : childrenOfType) {
                if (item.getNode().getFirstChildNode().getElementType() != PumlTypes.IDENTIFIER) {
                    continue;
                }
                String text = item.getText();
                if (!added.contains(text)) {
                    added.add(text);
                    if (text != null && text.length() > 0) {
                        variants.add(LookupElementBuilder
                                        .create(item).withIcon(PlantUmlFileType.PLANTUML_ICON)
//                                        .withTypeText(item.getContainingFile().getName())
                                        .withCaseSensitivity(true)
                                        .withBoldness(true)
                                        .withLookupStrings(Arrays.asList(text, sanitize(text)))
                                        .withInsertHandler(new LookupElementInsertHandler())
                        );
                    }
                }

            }
        }
        return variants.toArray();
    }

    @Nullable
    private String sanitize(String text) {
        text = StringUtils.removeStart(text, "\"");
        text = StringUtils.removeStart(text, "[");
        text = StringUtils.removeStart(text, "(");
        return text;
    }

    static class LookupElementInsertHandler implements InsertHandler<LookupElement> {
        @Override
        public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement lookupElement) {
            Editor editor = context.getEditor();
            Document document = editor.getDocument();
            if (context.getCompletionChar() == Lookup.REPLACE_SELECT_CHAR) {
                int startOffset = context.getStartOffset();
                int tailOffset = context.getTailOffset();
                String text = document.getText(TextRange.create(startOffset, tailOffset));
                if (startOffset == 0) {
                    return;
                }
                String before = document.getText(TextRange.create(startOffset - 1, startOffset));

                String substring = text.substring(0, 1);
                if (!StringUtils.isAlphanumeric(substring) && before.equals(substring)) {
                    document.deleteString(startOffset - 1, startOffset);
                }
            }
        }
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        PumlItem element = getElement();
        element.setName(newElementName);
        return element;
    }

    public static class PumlElementManipulator extends AbstractElementManipulator<PumlItem> {
        @Override
        public @Nullable
        PumlItem handleContentChange(@NotNull PumlItem PumlItem, @NotNull TextRange textRange, String s) throws IncorrectOperationException {
            PsiElement psiElement = PumlItem.setName(s);
            return (PumlItem) psiElement;
        }
    }
}
