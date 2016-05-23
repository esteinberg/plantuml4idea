package org.plantuml.idea.rendering;

public class RenderingCancelledException extends RuntimeException {

    public RenderingCancelledException() {
    }

    public RenderingCancelledException(Exception e) {
        super(e);
    }
}
