package org.plantuml.idea.lang.annotator;

/**
 * Author: Eugene Steinberg
 * Date: 9/13/14
 */
public class AnnotationResult {
    private String errorMessage;
    private String suggestion;
    private int lineNumber;

    public AnnotationResult(String errormsg, String suggestion, int lineNumber) {
        this.errorMessage = errormsg;
        this.suggestion = suggestion;
        this.lineNumber = lineNumber;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getSuggestion() {
        return suggestion;
    }

    @Override
    public String toString() {
        return "AnnotationResult{" +
                "errorMessage='" + errorMessage + '\'' +
                ", suggestion='" + suggestion + '\'' +
                ", lineNumber=" + lineNumber +
                '}';
    }
}
