package org.plantuml.idea.toolwindow.image.links;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.CaretState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.Alarm;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.grammar.psi.PumlItem;
import org.plantuml.idea.grammar.psi.PumlTypes;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.plantuml.idea.toolwindow.image.ImageContainer;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Highlighter {

    private static final Logger LOG = Logger.getInstance(Highlighter.class);
    private final PlantUmlSettings plantUmlSettings;
    private Alarm myAlarm;

    public Highlighter() {
        myAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD);
        plantUmlSettings = PlantUmlSettings.getInstance();
    }

    public void highlightImages(PlantUmlToolWindow plantUmlToolWindow, Editor editor) {
        if (editor == null || editor.getProject() == null || !plantUmlToolWindow.isToolWindowVisible()) {
            return;
        }
        myAlarm.cancelAllRequests();
        myAlarm.addRequest(() -> {
            if (editor.isDisposed()) {
                return;
            }
            highlight(plantUmlToolWindow, editor);
        }, 10);
    }

    private void highlight(PlantUmlToolWindow plantUmlToolWindow, Editor editor) {
        long start = System.currentTimeMillis();
        JPanel imagesPanel = plantUmlToolWindow.getImagesPanel();
        Component[] components = imagesPanel.getComponents();

        if (components.length > 0) {
            List<String> list;
            if (plantUmlSettings.isHighlightInImages()) {
                list = getListForHighlighting(editor);
            } else {
                list = Collections.emptyList();
            }

            for (Component component : components) {
                if (component instanceof ImageContainer) {
                    ImageContainer imageContainer = (ImageContainer) component;
                    imageContainer.highlight(list);
                }
            }
        }
        LOG.debug("highlightImages done in ", System.currentTimeMillis() - start, "ms");
    }

    @NotNull
    private java.util.List<String> getListForHighlighting(Editor editor) {
        java.util.List<CaretState> caretsAndSelections = editor.getCaretModel().getCaretsAndSelections();
        List<String> list = new ArrayList<>();
        Document document = editor.getDocument();
        if (editor.getProject() == null) {
            return Collections.emptyList();
        }
        PsiFile file = PsiDocumentManager.getInstance(editor.getProject()).getPsiFile(editor.getDocument());
        if (file == null || !file.isValid()) {
            return Collections.emptyList();
        }
        for (CaretState caretsAndSelection : caretsAndSelections) {
            LogicalPosition selectionStart = caretsAndSelection.getSelectionStart();
            LogicalPosition selectionEnd = caretsAndSelection.getSelectionEnd();
            if (selectionStart == null || selectionStart == selectionEnd) {
                LogicalPosition caretPosition = caretsAndSelection.getCaretPosition();
                int offset = editor.logicalPositionToOffset(caretPosition);
                if (document.getTextLength() == offset) {
                    offset = offset - 1;
                }
                if (StringUtils.isWhitespace(document.getText(TextRange.from(offset, 1)))) {
                    offset = offset - 1;
                }
                PsiElement elementAtOffset = PsiUtilCore.getElementAtOffset(file, offset);
                if (elementAtOffset instanceof PumlItem) {
                    String text = elementAtOffset.getText();
                    list.add(sanitize(text));
                } else if (elementAtOffset.getNode().getElementType() == PumlTypes.IDENTIFIER) {
                    String text = elementAtOffset.getText();
                    list.add(sanitize(text));
                } else {
                    String text = elementAtOffset.getText();
                    list.add(sanitize(text));
                }
            } else {
                int startOffset = editor.logicalPositionToOffset(selectionStart);
                int endOffset = editor.logicalPositionToOffset(selectionEnd);
                String text = document.getText(new TextRange(startOffset, endOffset));

                list.add(text);
            }

        }
        return list;
    }

    protected static String sanitize(String text) {
        char[] charArray = text.toCharArray();
        int start = -1;
        int end = -1;
        for (int i = 0, charArrayLength = charArray.length; i < charArrayLength; i++) {
            char c = charArray[i];
            if (Character.isLetter(c)) {
                if (start == -1) {
                    start = i;
                }
                end = i;
            }
        }
        if (start != -1) {
            return text.substring(start, end + 1);
        }
        return text;
    }

}
