package org.plantuml.idea.language;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.PlantUmlFileType;
import org.plantuml.idea.language.psi.PumlWord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleUtil {

    public static List<PumlWord> findDeclarationOrUsagesInFile(PsiFile containingFile, @NotNull PumlWord element, String key) {
        List<PumlWord> result = new ArrayList<>();
        PumlWord[] properties = PsiTreeUtil.getChildrenOfType(containingFile, PumlWord.class);
        boolean returnFirst = true;
        boolean firstMatch = true;

        if (properties != null) {
            for (PumlWord property : properties) {
                if (isSame(key, property)) {
                    if (firstMatch && property == element) {
                        returnFirst = false;
                        continue;
                    }

                    result.add(property);
                    if (returnFirst) {
                        break;
                    }
                    firstMatch = false;
                }
            }
        }
        return result;
    }

    static final Pattern SANITIZER = Pattern.compile("([a-zA-Z0-9].*[a-zA-Z0-9])");

    public static boolean isSame(String p1, PumlWord property) {
        String p2 = property.getText();
        if (p1.equals(p2)) {
            return true;
        }
        Matcher m = SANITIZER.matcher(p1);
        String key = null;
        if (m.find()) {
            key = m.group();
        }
        if (key == null) {
            return false;
        }

        String key2 = null;
        m.reset(p2);
        if (m.find()) {
            key2 = m.group();
        }
        return key.equals(key2);
    }

    public static List<PumlWord> find(Project project, String key) {
        List<PumlWord> result = new ArrayList<>();
        Collection<VirtualFile> virtualFiles =
                FileTypeIndex.getFiles(PlantUmlFileType.INSTANCE, GlobalSearchScope.allScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            PsiFile simpleFile = (PsiFile) PsiManager.getInstance(project).findFile(virtualFile);
            if (simpleFile != null) {
                PumlWord[] properties = PsiTreeUtil.getChildrenOfType(simpleFile, PumlWord.class);
                if (properties != null) {
                    for (PumlWord property : properties) {
                        if (isSame(key, property)) {
                            result.add(property);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static List<PumlWord> findAll(Project project) {
        List<PumlWord> result = new ArrayList<>();
        Collection<VirtualFile> virtualFiles =
                FileTypeIndex.getFiles(PlantUmlFileType.INSTANCE, GlobalSearchScope.allScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            PsiFile simpleFile = (PsiFile) PsiManager.getInstance(project).findFile(virtualFile);
            if (simpleFile != null) {
                PumlWord[] properties = PsiTreeUtil.getChildrenOfType(simpleFile, PumlWord.class);
                if (properties != null) {
                    Collections.addAll(result, properties);
                }
            }
        }
        return result;
    }

}
