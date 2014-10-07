package org.plantuml.idea.lang.annotator;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.TextRange;

/**
 * Author: Eugene Steinberg
 * Date: 10/5/14
 */
public class SyntaxHighlightAnnotation implements SourceAnnotation {
    int startSourceOffset;
    int endSourceOffset;
    TextAttributesKey textAttributesKey;

    public SyntaxHighlightAnnotation(int startSourceOffset, int endSourceOffset, TextAttributesKey textAttributesKey) {
        this.startSourceOffset = startSourceOffset;
        this.endSourceOffset = endSourceOffset;
        this.textAttributesKey = textAttributesKey;
    }

    @Override
    public void annotate(AnnotationHolder holder, Document document, int sourceOffset) {
        TextRange fileRange = TextRange.create(startSourceOffset + sourceOffset, endSourceOffset + sourceOffset);
        Annotation infoAnnotation = holder.createInfoAnnotation(fileRange, null);
        infoAnnotation.setTextAttributes(textAttributesKey);
    }
}
