package org.plantuml.idea.lang;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.codeInsight.editorActions.smartEnter.SmartEnterProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.grammar.psi.PumlTypes;
import org.plantuml.idea.settings.PlantUmlSettings;

import static java.lang.Character.isWhitespace;

public class PlantUmlTypedHandlerDelegate extends TypedHandlerDelegate {

    private static PlantUmlSettings settings;

    public PlantUmlTypedHandlerDelegate() {
        settings = PlantUmlSettings.getInstance();
    }

    @Override
    public @NotNull
    Result charTyped(char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        if (file instanceof PlantUmlFileImpl) {
            processPair(c, editor, file);
        }
        return Result.CONTINUE;
    }

    public static void processPair(char c,
                                   @NotNull Editor editor,
                                   @NotNull PsiFile file) {
        if (!settings.isInsertPair()) return;
        String insert;
        if (c == '(') {
            insert = ")";
        } else if (c == '[') {
            insert = "]";
//        } else if (c == '<') {
//            insert = ">";
        } else if (c == '{') {
            insert = "}";
        } else if (c == '"') {
            insert = "\"";
        } else {
            return;
        }

        SmartEnterProcessor.commitDocument(editor);
        int offset = editor.getCaretModel().getOffset();

        CharSequence sequence = editor.getDocument().getCharsSequence();
        if (!isWhitespace(sequence.charAt(offset))) return;
        if (offset - 2 >= 0 && !isWhitespace(sequence.charAt(offset - 2))) return;

        PsiElement element = file.findElementAt(offset - 1);
        if (element == null) return;
        if (element.getNode().getElementType() == PumlTypes.IDENTIFIER) {
            return;
        }
        editor.getDocument().insertString(offset, insert);
    }
}
