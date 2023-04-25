package org.plantuml.idea.grammar.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import org.plantuml.idea.lang.PlantUmlFileImpl;
import org.plantuml.idea.lang.PlantUmlFileType;

public class PumlElementFactory {

    public static PumlItem createWord(Project project, String name) {
        final PlantUmlFileImpl file = createFile(project, name);
        return (PumlItem) file.getFirstChild();
    }

    /**
     * this one works better
     */
    public static PsiElement createWord2(Project project, String name) {
        String name1 = "dummy.puml";
        final PlantUmlFileImpl file = (PlantUmlFileImpl) PsiFileFactory.getInstance(project).createFileFromText(name1, PlantUmlFileType.INSTANCE, "@start\n" + name + "\n@end");
        return (PsiElement) file.findElementAt(8);
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
