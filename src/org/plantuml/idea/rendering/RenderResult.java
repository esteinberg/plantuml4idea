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

    public RenderResult(@NotNull List<Diagram> diagrams, int totalPages) {
        this.diagrams = diagrams;
        this.pages = totalPages;
    }

    public byte[] getFirstDiagramBytes() {
        if (diagrams.size() == 0) {
            return new byte[0];
        }
        return diagrams.get(0).getDiagramBytes();
    }


    public List<Diagram> getDiagrams() {
        return diagrams;
    }


    public int getPages() {
        return pages;
    }

    public boolean hasError() {
        for (Diagram diagram : diagrams) {
            String description = diagram.getDescription();
            if (description == null || description.isEmpty() || "(Error)".equals(description)) {
                return true;
            }
        }
        return false;
    }


    public static class Diagram {

        private final int page;
        private final String description;
        private final byte[] diagramBytes;
        private final String pageSource;
        private final String documentSource;

        public Diagram(String documentSource, String pageSource, int page, String description, byte[] diagramBytes) {
            this.pageSource = pageSource;
            this.documentSource = documentSource;
            this.page = page;
            this.description = description;
            this.diagramBytes = diagramBytes;
        }

        public Diagram(int page, Diagram description) {
            this.page = page;
            this.description = description.description;
            this.pageSource = description.pageSource;
            this.documentSource = description.documentSource;
            this.diagramBytes = description.diagramBytes;
        }

        public String getDocumentSource() {
            return documentSource;
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

        public String getPageSource() {
            return pageSource;
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
                .toString();
    }
}
