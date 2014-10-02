package org.plantuml.idea.lang.annotator;

import java.util.Collection;

/**
 * Author: Eugene Steinberg
 * Date: 9/13/14
 */
public class SourceAnnotationResult {
    private Collection<String> errorMessage;
    private Collection<String> suggestion;
    private int errorLineNumber;

    public SourceAnnotationResult(Collection<String> errormsg, Collection<String> suggestion, int errorLineNumber) {
        this.errorMessage = errormsg;
        this.suggestion = suggestion;
        this.errorLineNumber = errorLineNumber;
    }


    public Collection<String> getErrorMessage() {
        return errorMessage;
    }

    public Collection<String> getSuggestion() {
        return suggestion;
    }

    public int getErrorLineNumber() {
        return errorLineNumber;
    }

    @Override
    public String toString() {
        return "SourceAnnotationResult{" +
                "errorMessage=" + errorMessage +
                ", suggestion=" + suggestion +
                ", errorLineNumber=" + errorLineNumber +
                '}';
    }
}
