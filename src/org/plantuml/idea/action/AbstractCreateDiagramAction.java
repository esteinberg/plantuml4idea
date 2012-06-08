package org.plantuml.idea.action;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.actions.CreateElementActionBase;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import org.jetbrains.annotations.NotNull;

/**
 * @author mamontov
 */
public abstract class AbstractCreateDiagramAction extends CreateElementActionBase {

    public AbstractCreateDiagramAction() {
        super("Create New Plant UML Diagram", "", null);
    }

    @NotNull
    @Override
    protected PsiElement[] invokeDialog(Project project, PsiDirectory psiDirectory) {
        MyInputValidator validator = new MyInputValidator(project, psiDirectory);
        Messages.showInputDialog(project, "Enter a new file name:", "New Diagram File", Messages.getQuestionIcon(), "", validator);
        return validator.getCreatedElements();
    }

    @NotNull
    @Override
    protected PsiElement[] create(String s, PsiDirectory psiDirectory) throws Exception {
        final FileTemplate template = FileTemplateManager.getInstance().getTemplate(getDiagramName());
        String fileName = getFileName(s);
        Project project = psiDirectory.getProject();
        psiDirectory.checkCreateFile(fileName);
        PsiFile psiFile = PsiFileFactory.getInstance(project)
                .createFileFromText(fileName, PlantumlFileType.PLANTUML_FILE_TYPE, template.getText());
        psiFile = (PsiFile) psiDirectory.add(psiFile);
        final VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile != null) {
            FileEditorManager.getInstance(project).openFile(virtualFile, true);
        }
        return new PsiElement[]{psiFile};
    }

    protected abstract String getDiagramName();

    @Override
    protected String getErrorTitle() {
        return "Error creating diagram";
    }

    @Override
    protected String getCommandName() {
        return "Create diagram file";
    }

    @Override
    protected String getActionName(PsiDirectory psiDirectory, String s) {
        return IdeBundle.message("progress.creating.file", "", "", psiDirectory.getName());
    }

    private String getFileName(String name) {
        return name + PlantumlFileType.PLANTUML_EXT;
    }
}
