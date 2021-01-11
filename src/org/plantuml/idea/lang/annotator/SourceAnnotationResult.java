package org.plantuml.idea.lang.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.editor.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Author: Eugene Steinberg
 * Date: 10/5/14
 */
public class SourceAnnotationResult {
    private int sourceOffset;

    private final Collection<SourceAnnotation> annotations = new ArrayList<SourceAnnotation>();
    private List<SyntaxHighlightAnnotation> blockComments;

    public SourceAnnotationResult(int sourceOffset) {
        this.sourceOffset = sourceOffset;
    }

    public void addAll(Collection<? extends SourceAnnotation> sourceAnnotations) {
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

    public void addWithBlockCommentCheck(SyntaxHighlightAnnotation syntaxHighlightAnnotation) {
        if (!inBlockComment(syntaxHighlightAnnotation)) {
            annotations.add(syntaxHighlightAnnotation);
        }
    }

    private boolean inBlockComment(SyntaxHighlightAnnotation syntaxHighlightAnnotation) {
        for (int i = 0, blockCommentsSize = blockComments.size(); i < blockCommentsSize; i++) {
            SyntaxHighlightAnnotation blockComment = blockComments.get(i);
            int startSourceOffset = syntaxHighlightAnnotation.startSourceOffset;
            if (blockComment.getStartSourceOffset() < startSourceOffset && startSourceOffset < blockComment.getEndSourceOffset()) {
                return true;
            }
        }
        return false;
    }

    public void addBlockComments(List<SyntaxHighlightAnnotation> blockComments) {
        this.blockComments = blockComments;
        addAll(blockComments);
    }
}