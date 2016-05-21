package org.plantuml.idea.plantuml;

import java.util.Arrays;
import java.util.List;


/**
 * @author Eugene Steinberg
 */
public class PlantUmlResult {

    private final List<Diagram> diagrams;
    private final int pages;
    private final RenderRequest renderRequest;

    public PlantUmlResult(List<Diagram> diagrams, int totalPages, RenderRequest renderRequest) {
        this.diagrams = diagrams;
        this.pages = totalPages;
        this.renderRequest = renderRequest;
    }

    public byte[] getFirstDiagramBytes() {
        if (diagrams.size() == 0) {
            return new byte[0];
        }
        return diagrams.get(0).getDiagramBytes();
    }

    public RenderRequest getRenderRequest() {
        return renderRequest;
    }

    public List<Diagram> getDiagrams() {
        return diagrams;
    }


    public int getPages() {
        return pages;
    }

    public static class Diagram {

        private int page;
        private byte[] diagramBytes;

        public Diagram(int page, byte[] diagramBytes) {
            this.page = page;
            this.diagramBytes = diagramBytes;
        }

        public int getPage() {
            return page;
        }

        public byte[] getDiagramBytes() {
            return diagramBytes;
        }

        @Override
        public String toString() {
            return "Diagram{" + "diagramBytes=" + Arrays.toString(diagramBytes) + '}';
        }
    }

}
