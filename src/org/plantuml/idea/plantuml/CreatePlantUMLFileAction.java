package org.plantuml.idea.plantuml;

import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.NonEmptyInputValidator;
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
                .addKind("Sequence", PlantUmlFileType.PLANTUML_ICON, "UML Sequence")
                .addKind("Use Case", PlantUmlFileType.PLANTUML_ICON, "UML Use Case")
                .addKind("Class", PlantUmlFileType.PLANTUML_ICON, "UML Class")
                .addKind("Activity", PlantUmlFileType.PLANTUML_ICON, "UML Activity")
                .addKind("Component", PlantUmlFileType.PLANTUML_ICON, "UML Component")
                .addKind("State", PlantUmlFileType.PLANTUML_ICON, "UML State")
                .addKind("Object", PlantUmlFileType.PLANTUML_ICON, "UML Object")
                .addKind("Deployment", PlantUmlFileType.PLANTUML_ICON, "UML Deployment")
                .addKind("Gantt", PlantUmlFileType.PLANTUML_ICON, "UML Gantt")
                .addKind("MindMap", PlantUmlFileType.PLANTUML_ICON, "UML MindMap")
                .addKind("Wireframe", PlantUmlFileType.PLANTUML_ICON, "Salt Wireframe")
                .addKind("Work Breakdown Structure", PlantUmlFileType.PLANTUML_ICON, "Work Breakdown Structure")
                .addKind("YAML Data", PlantUmlFileType.PLANTUML_ICON, "UML YAML Data")
                .addKind("EBNF", PlantUmlFileType.PLANTUML_ICON, "EBNF")
                .setValidator(new NonEmptyInputValidator())
        ;
    }

    @Override
    protected String getActionName(PsiDirectory directory, @NotNull String newName, String templateName) {
        return "PlantUML File";
    }
}
