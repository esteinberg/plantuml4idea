package org.plantuml.idea.rendering;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;


/**
 * @author Eugene Steinberg
 */
public class RenderResult {

    private PlantUmlRenderer.Strategy strategy;
    private final List<ImageItem> imageItems;
    private final int pages;

    public RenderResult(PlantUmlRenderer.Strategy strategy, @NotNull List<ImageItem> imageItems, int totalPages) {
        this.strategy = strategy;
        this.imageItems = imageItems;
        this.pages = totalPages;
    }

    public byte[] getFirstDiagramBytes() {
        if (imageItems.size() == 0) {
            return new byte[0];
        }
        return imageItems.get(0).getImageBytes();
    }

    public PlantUmlRenderer.Strategy getStrategy() {
        return strategy;
    }

    public List<ImageItem> getImageItems() {
        return imageItems;
    }


    public int getPages() {
        return pages;
    }

    public boolean hasError() {
        if (strategy == PlantUmlRenderer.Strategy.NORMAL) {
            for (ImageItem imageItem : imageItems) {
                String description = imageItem.getDescription();
                if (description == null || description.isEmpty() || "(Error)".equals(description)) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("diagrams", imageItems)
                .append("pages", pages)
                .toString();
    }
}
