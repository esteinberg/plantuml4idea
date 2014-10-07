package org.plantuml.idea.lang.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.editor.Document;

/**
 * Author: Eugene Steinberg
 * Date: 10/2/14
 */
public interface SourceAnnotation {

    void annotate(AnnotationHolder holder, Document document, int sourceOffset);

}
