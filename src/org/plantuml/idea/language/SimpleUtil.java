package org.plantuml.idea.language;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.plantuml.idea.lang.PlantUmlFileType;
import org.plantuml.idea.language.psi.PumlWord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SimpleUtil {

    public static List<PumlWord> findFirstInFile(PsiFile containingFile, String key) {
        List<PumlWord> result = new ArrayList<>();
        PumlWord[] properties = PsiTreeUtil.getChildrenOfType(containingFile, PumlWord.class);
        if (properties != null) {
            for (PumlWord property : properties) {
                if (key.equals(property.getText())) {
                    result.add(property);
                    break;

                }
            }
        }
        return result;
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
                        if (key.equals(property.getText())) {
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
