package org.plantuml.idea.lang.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.editor.Document;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Author: Eugene Steinberg
 * Date: 9/30/14
 */
public class FileAnnotationResult {
    private Collection<SourceAnnotationResult> sourceAnnotationResults = new ArrayList<SourceAnnotationResult>();

    public boolean add(SourceAnnotationResult sourceAnnotationResult) {
        return sourceAnnotationResults.add(sourceAnnotationResult);
    }

    public void annotate(AnnotationHolder holder, Document document) {
        for (SourceAnnotationResult sourceAnnotationResult : sourceAnnotationResults) {
            sourceAnnotationResult.annotate(holder, document);
        }
    }
}
