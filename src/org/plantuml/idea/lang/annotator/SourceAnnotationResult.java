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
    private int[][] skip;

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

    public void addWithBlockCommentCheck(SyntaxHighlightAnnotation syntaxHighlightAnnotation) {
        if (!inBlockComment(syntaxHighlightAnnotation)) {
            annotations.add(syntaxHighlightAnnotation);
        }
    }

    private boolean inBlockComment(SyntaxHighlightAnnotation syntaxHighlightAnnotation) {
        for (int i = 0, skipLength = skip.length; i < skipLength; i++) {
            int[] range = skip[i];
            int startSourceOffset = syntaxHighlightAnnotation.startSourceOffset;
            if (range[0] < startSourceOffset && startSourceOffset < range[1]) {
                return true;
            }
        }
        return false;
    }

    public void addBlockComments(List<SourceAnnotation> blockComments) {
        skip = new int[blockComments.size()][2];
        for (int i = 0, blockCommentsSize = blockComments.size(); i < blockCommentsSize; i++) {
            SourceAnnotation blockComment = blockComments.get(i);
            SyntaxHighlightAnnotation b = (SyntaxHighlightAnnotation) blockComment;
            int startSourceOffset = b.getStartSourceOffset();
            int endSourceOffset = b.getEndSourceOffset();
            skip[i][0] = startSourceOffset;
            skip[i][1] = endSourceOffset;
        }
        addAll(blockComments);
    }
}