package org.plantuml.idea.lang;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.util.Utils;

public class PlantUmlCompletionAutoPopupHandler extends TypedHandlerDelegate {
    private static final Logger LOG = Logger.getInstance(PlantUmlCompletionAutoPopupHandler.class);

    @NotNull
    @Override
    public TypedHandlerDelegate.Result checkAutoPopup(char charTyped, @NotNull final Project project, @NotNull final Editor editor, @NotNull final PsiFile file) {
        if (Utils.isPlantUmlFileType(file)) {
            if (charTyped == '!' || charTyped == '@') {
                AutoPopupController.getInstance(project).scheduleAutoPopup(editor);
                return Result.STOP;
            }
        }

        return Result.CONTINUE;
    }

}
