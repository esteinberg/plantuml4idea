package org.plantuml.idea.intentions;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;

class AddOptionIntentionCommand {
    private Editor editor;
    protected int caretOffset;
    private String option;

    public AddOptionIntentionCommand(Editor editor, int caretOffset, String option) {
        this.editor = editor;
        this.caretOffset = caretOffset;
        this.option = option;
    }

    public boolean isAvailable() {
        return invoke(true);
    }

    public boolean invoke() {
        return invoke(false);
    }

    private boolean invoke(boolean validateOnly) {
        Document document = editor.getDocument();
        int lineNumber = document.getLineNumber(caretOffset);
        int lineStartOffset = document.getLineStartOffset(lineNumber);
        int lineEndOffset = document.getLineEndOffset(lineNumber);
        String line = document.getText(TextRange.create(lineStartOffset, lineEndOffset));
        boolean start = line.trim().startsWith("@start");

        if (!validateOnly && start) {
            document.replaceString(lineEndOffset, lineEndOffset, "\n'" + option);
        }
        return start;
    }

}
