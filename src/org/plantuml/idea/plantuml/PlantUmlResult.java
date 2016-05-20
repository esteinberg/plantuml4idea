package org.plantuml.idea.plantuml;

import java.util.Arrays;

/**
 * @author Eugene Steinberg
 */
public class PlantUmlResult {

    private static final Diagram[] NO_DIAGRAMS = new Diagram[0];

    private Diagram[] diagrams;
    private String description;
    private String error;
    private int pages;
    private RenderRequest renderRequest;

    public PlantUmlResult(Diagram[] diagrams, String description, int totalPages, RenderRequest renderRequest) {
        this.diagrams = diagrams;
        this.description = description;
        this.pages = totalPages;
        this.renderRequest = renderRequest;
    }

    public PlantUmlResult(String description, String error, int pages) {
        this.diagrams = NO_DIAGRAMS;
        this.description = description;
        this.error = error;
        this.pages = pages;
    }

    public byte[] getFirstDiagramBytes() {
        if (diagrams.length == 0) {
            return new byte[0];
        }
        return diagrams[0].getDiagramBytes();
    }

    public RenderRequest getRenderRequest() {
        return renderRequest;
    }

    public Diagram[] getDiagrams() {
        return diagrams;
    }

    public void setDiagrams(Diagram[] diagrams) {
        this.diagrams = diagrams;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isError() {
        return (description == null || description.isEmpty() || "(Error)".equals(description));
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public static class Diagram {

        private byte[] diagramBytes;

        public Diagram(byte[] diagramBytes) {
            this.diagramBytes = diagramBytes;
        }

        public byte[] getDiagramBytes() {
            return diagramBytes;
        }

        @Override
        public String toString() {
            return "Diagram{" + "diagramBytes=" + Arrays.toString(diagramBytes) + '}';
        }
    }

    @Override
    public String toString() {
        return "PlantUmlResult{" +
                "diagrams=" + Arrays.toString(diagrams) +
                ", description='" + description + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
