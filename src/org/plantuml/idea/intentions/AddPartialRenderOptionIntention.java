package org.plantuml.idea.intentions;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.PlantUmlFileType;
import org.plantuml.idea.lang.annotator.LanguageDescriptor;

public class AddPartialRenderOptionIntention extends BaseIntentionAction {

    @NotNull
    @Override
    public String getFamilyName() {
        return "Enable partial rendering";
    }

    @NotNull
    @Override
    public String getText() {
        return getFamilyName();
    }

    @Override
    public boolean isAvailable(@NotNull Project project, final Editor editor, PsiFile file) {
        if (!file.getFileType().equals(PlantUmlFileType.INSTANCE)) return false;
        int offset = editor.getCaretModel().getOffset();
        return new AddOptionIntentionCommand(editor, offset, LanguageDescriptor.IDEA_PARTIAL_RENDER).isAvailable();
    }

    @Override
    public void invoke(@NotNull Project project, final Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        new AddOptionIntentionCommand(editor, editor.getCaretModel().getOffset(), LanguageDescriptor.IDEA_PARTIAL_RENDER).invoke();
    }

}
