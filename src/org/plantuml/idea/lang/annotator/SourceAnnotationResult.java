package org.plantuml.idea.lang.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.editor.Document;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Author: Eugene Steinberg
 * Date: 10/5/14
 */
public class SourceAnnotationResult {
    private int sourceOffset;

    private final Collection<SourceAnnotation> annotations = new ArrayList<SourceAnnotation>();

    public SourceAnnotationResult(int sourceOffset) {
        this.sourceOffset = sourceOffset;
    }

    public void addAll(Collection<SourceAnnotation> sourceAnnotations) {
        annotations.addAll(sourceAnnotations);
    }

    public void annotate(AnnotationHolder holder, Document document) {
        for (SourceAnnotation annotation : annotations) {
            annotation.annotate(holder, document, sourceOffset);
        }
    }

    public Collection<SourceAnnotation> getAnnotations() {
        return annotations;
    }
}
