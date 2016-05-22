package org.plantuml.idea.rendering;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;


/**
 * @author Eugene Steinberg
 */
public class RenderResult {

    private final List<Diagram> diagrams;
    private final int pages;
    private final RenderRequest renderRequest;

    public RenderResult(@NotNull List<Diagram> diagrams, int totalPages, @NotNull RenderRequest renderRequest) {
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
        private String description;
        private byte[] diagramBytes;

        public Diagram(int page, String description, byte[] diagramBytes) {
            this.page = page;
            this.description = description;
            this.diagramBytes = diagramBytes;
        }

        public String getDescription() {
            return description;
        }

        public int getPage() {
            return page;
        }

        public byte[] getDiagramBytes() {
            return diagramBytes;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("page", page)
                    .append("description", description)
                    .append("diagramBytesLength", diagramBytes == null ? "null" : diagramBytes.length)
                    .toString();
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("diagrams", diagrams)
                .append("pages", pages)
                .append("renderRequest", renderRequest)
                .toString();
    }
}
