package org.plantuml.idea.lang.annotator;

import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.util.ui.TwoColorsIcon;
import com.intellij.xml.util.ColorIconCache;
import net.sourceforge.plantuml.graphic.*;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.PlantUmlHighlighterColors;
import org.plantuml.idea.lang.psi.PlantUmlDiagram;
import org.plantuml.idea.lang.psi.PlantUmlTokenTypes;

import javax.swing.*;

/**
 * @author Max Gorbunov
 */
public class PlantUmlAnnotator implements Annotator {
    private static final HtmlColorSetSimple HTML_COLOR_SET = new HtmlColorSetSimple();

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        PlantUmlDiagram diagram = findDiagram(element);
        if (diagram != null) {
            ASTNode firstPragma = diagram.getNode().findChildByType(PlantUmlTokenTypes.PRAGMA);
            if (firstPragma == null) {
                annotateDefaultSyntax(element, holder);
            } else if ("!pragma nosyntax".equals(firstPragma.getText())) {
                if (element == firstPragma) {
                    // Clear highlighting
                    int offset = firstPragma.getStartOffset() + firstPragma.getTextLength();
                    TextRange range = TextRange.from(offset, diagram.getLastChild().getTextOffset() - offset);
                    holder.createInfoAnnotation(range, null).setEnforcedTextAttributes(TextAttributes.ERASE_MARKER);
                }
            } else if ("!pragma syntax common".equals(firstPragma.getText())) {
                annotateChangeTextAttributes(element, holder);
                annotateColorGutter(element, holder);
            } else if ("!pragma syntax sequence".equals(firstPragma.getText())) {
                annotateChangeTextAttributes(element, holder);
                annotateColorGutter(element, holder);
            } else if ("!pragma syntax class".equals(firstPragma.getText())) {
                annotateChangeTextAttributes(element, holder);
                annotateColorGutter(element, holder);
            } else if ("!pragma syntax component".equals(firstPragma.getText())) {
                annotateChangeTextAttributes(element, holder);
                annotateColorGutter(element, holder);
            } else {
                annotateDefaultSyntax(element, holder);
            }
        }
    }

    private void annotateDefaultSyntax(PsiElement element, AnnotationHolder holder) {
        annotateChangeTextAttributes(element, holder);
        annotateColorGutter(element, holder);
        if (element.getNode().getElementType() == PlantUmlTokenTypes.START_UML) {
            Annotation warning = holder.createWarningAnnotation(element,
                    "Diagram type detection is not supported. Please specify desired syntax with pragma.");
            warning.setTooltip("Add one of the syntax pragma statements:\n\n"
                    + "!pragma nosyntax\n"
                    + "!pragma syntax class\n"
                    + "!pragma syntax common\n"
                    + "!pragma syntax component\n"
                    + "!pragma syntax sequence");
        }
    }

    private PlantUmlDiagram findDiagram(PsiElement element) {
        if (element == null || element instanceof PlantUmlDiagram) {
            return (PlantUmlDiagram) element;
        }
        return findDiagram(element.getParent());
    }

    private void annotateChangeTextAttributes(PsiElement element, AnnotationHolder holder) {
        if (element.getNode().getElementType() == PlantUmlTokenTypes.CHAMELEON_TEXT) {
            replaceTextAttributes(element, holder, PlantUmlHighlighterColors.STRING.getDefaultAttributes());
        } else if (element.getNode().getElementType() == PlantUmlTokenTypes.HASH_COLOR) {
            replaceTextAttributes(element, holder, PlantUmlHighlighterColors.NUMBER.getDefaultAttributes());
        }
        // TODO: highlight string escape sequences
    }

    private void replaceTextAttributes(PsiElement element, AnnotationHolder holder, TextAttributes textAttributes) {
        // First, erase attributes
        holder.createInfoAnnotation(element, null).setEnforcedTextAttributes(TextAttributes.ERASE_MARKER);
        // Then apply what we want
        holder.createInfoAnnotation(element, null).setEnforcedTextAttributes(textAttributes);
    }

    private void annotateColorGutter(PsiElement element, AnnotationHolder holder) {
        if (element.getNode().getElementType() == PlantUmlTokenTypes.HASH_COLOR) {
            try {
                String colorText = element.getText();
                HtmlColor color = HTML_COLOR_SET.getColorIfValid(colorText.substring(1));
                if (color != null && color instanceof HtmlColorSimple) {
                    final Icon icon = ColorIconCache.getIconCache().getIcon(((HtmlColorSimple) color).getColor999(), 8);
                    holder.createInfoAnnotation(element, null).setGutterIconRenderer(new GutterIconRenderer() {
                        @NotNull
                        @Override
                        public Icon getIcon() {
                            return icon;
                        }

                        @Override
                        public boolean equals(Object obj) {
                            return false;
                        }

                        @Override
                        public int hashCode() {
                            return 0;
                        }

                        // TODO: add color picker when clicked
                    });
                } else if (color != null && color instanceof HtmlColorGradient) {
                    HtmlColorSimple color1 = (HtmlColorSimple) ((HtmlColorGradient) color).getColor1();
                    HtmlColorSimple color2 = (HtmlColorSimple) ((HtmlColorGradient) color).getColor2();
                    final Icon icon = new TwoColorsIcon(8, color1.getColor999(), color2.getColor999());
                    holder.createInfoAnnotation(element, null).setGutterIconRenderer(new GutterIconRenderer() {
                        @NotNull
                        @Override
                        public Icon getIcon() {
                            return icon;
                        }

                        @Override
                        public boolean equals(Object obj) {
                            return false;
                        }

                        @Override
                        public int hashCode() {
                            return 0;
                        }

                        // TODO: add color picker when clicked
                    });
                } else {
                    holder.createWarningAnnotation(element, "Invalid color");
                }
            } catch (Exception ignored) {
            }
        }
    }
}
