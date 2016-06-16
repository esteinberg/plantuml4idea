package org.plantuml.idea.rendering;

public class PartialRenderingException extends RuntimeException {
    public PartialRenderingException() {
        super("plantuml4idea: partial rendering not supported with @newpage in included file");
    }
}
