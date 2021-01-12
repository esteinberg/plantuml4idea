package org.plantuml.idea.language.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import org.plantuml.idea.lang.PlantUmlFileImpl;
import org.plantuml.idea.lang.PlantUmlFileType;

public class PumlElementFactory {

    public static PumlWord createWord(Project project, String name) {
        final PlantUmlFileImpl file = createFile(project, name);
        return (PumlWord) file.getFirstChild();
    }

    public static PlantUmlFileImpl createFile(Project project, String text) {
        String name = "dummy.puml";
        return (PlantUmlFileImpl) PsiFileFactory.getInstance(project).createFileFromText(name, PlantUmlFileType.INSTANCE, text);
    }

    public static PsiElement createCRLF(Project project) {
        final PlantUmlFileImpl file = createFile(project, "\n");
        return file.getFirstChild();
    }

}
