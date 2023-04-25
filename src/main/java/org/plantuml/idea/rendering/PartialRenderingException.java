package org.plantuml.idea.rendering;

public class PartialRenderingException extends RuntimeException {
    public PartialRenderingException(String message) {
        super("plantuml4idea: partial rendering not supported with @newpage in included file : " + message);
    }

    public PartialRenderingException() {
        super("plantuml4idea: partial rendering not supported with @newpage in included file");
    }
}
