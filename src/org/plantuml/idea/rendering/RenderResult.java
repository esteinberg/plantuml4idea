package org.plantuml.idea.rendering;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author Eugene Steinberg
 */
public class RenderResult {

    private RenderingType strategy;
    private final List<ImageItem> imageItems;
    private final int pages;
    private int rendered;
    private int updatedTitles;
    private int cached;

    public RenderResult(RenderingType strategy, int totalPages) {
        this.strategy = strategy;
        if (totalPages == 0) {
            this.imageItems = Collections.emptyList();
        } else {
            this.imageItems = new ArrayList<ImageItem>(totalPages);
        } 
        this.pages = totalPages;
    }


    public byte[] getFirstDiagramBytes() {
        if (imageItems.size() == 0) {
            return new byte[0];
        }
        return imageItems.get(0).getImageBytes();
    }

    public RenderingType getStrategy() {
        return strategy;
    }

    public List<ImageItem> getImageItems() {
        return imageItems;
    }


    public int getPages() {
        return pages;
    }

    public boolean hasError() {
        if (strategy == RenderingType.NORMAL) {
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
                .append("pages", pages)
                .append("rendered", rendered)
                .append("updatedTitles", updatedTitles)
                .append("cached", cached)
                .append("diagrams", imageItems)
                .toString();
    }

    public void addRenderedImage(ImageItem imageItem) {
        imageItems.add(imageItem);
        rendered++;
    }

    public void addUpdatedTitle(ImageItem imageItem) {
        imageItems.add(imageItem);
        updatedTitles++;
    }

    public void addCachedImage(ImageItem imageItem) {
        imageItems.add(imageItem);
        if (imageItem.hasImage()) {
            cached++;
        }
    }

    public void add(ImageItem item) {
        imageItems.add(item);
    }

    public ImageItem getImageItem(int i) {
        return imageItems.size() > i ? imageItems.get(i) : null;
    }

    public int getRendered() {
        return rendered;
    }

    public int getUpdatedTitles() {
        return updatedTitles;
    }

    public int getCached() {
        return cached;
    }

    public void add(ImageItem imageItem, boolean titleChanged) {
        imageItems.add(imageItem);
        if (titleChanged) {
            updatedTitles++;
        }
    }
}
