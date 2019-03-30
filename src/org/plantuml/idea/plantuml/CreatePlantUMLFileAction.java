package org.plantuml.idea.plantuml;

import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.ide.actions.CreateHtmlFileAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.PlantUmlFileType;

public class CreatePlantUMLFileAction extends CreateFileFromTemplateAction implements DumbAware {

    public CreatePlantUMLFileAction() {
        super("PlantUML File", "Creates new PlantUML file", PlantUmlFileType.PLANTUML_ICON);
    }

    @Override
    protected String getDefaultTemplateProperty() {
        return "DefaultPlantUmlFileTemplate";
    }

    @Override
    protected void buildDialog(Project project, PsiDirectory directory, CreateFileFromTemplateDialog.Builder builder) {
        builder
                .setTitle("New PlantUML File")
                .addKind("Sequence", PlantUmlFileType.PLANTUML_ICON, "UML Sequence.puml")
                .addKind("Use Case", PlantUmlFileType.PLANTUML_ICON, "UML Use Case.puml")
                .addKind("Class", PlantUmlFileType.PLANTUML_ICON, "UML Class.puml")
                .addKind("Activity", PlantUmlFileType.PLANTUML_ICON, "UML Activity.puml")
                .addKind("Component", PlantUmlFileType.PLANTUML_ICON, "UML Component.puml")
                .addKind("State", PlantUmlFileType.PLANTUML_ICON, "UML State.puml")
                .addKind("Object", PlantUmlFileType.PLANTUML_ICON, "UML Object.puml")
                .addKind("Gantt", PlantUmlFileType.PLANTUML_ICON, "UML Gantt.puml")
                .addKind("MindMap", PlantUmlFileType.PLANTUML_ICON, "UML MindMap.puml")
                .addKind("Wireframe", PlantUmlFileType.PLANTUML_ICON, "Salt Wireframe.puml")
                .addKind("Work Breakdown Structure", PlantUmlFileType.PLANTUML_ICON, "Work Breakdown Structure.puml")
        ;
    }

    @Override
    protected String getActionName(PsiDirectory directory, @NotNull String newName, String templateName) {
        return "PlantUML File";
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CreateHtmlFileAction;
    }
}
