package org.plantuml.idea.rendering;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

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
		for (ImageItem imageItem : imageItems) {
			if (PlantUmlNormalRenderer.TITLE_ONLY.equals(imageItem.getDescription())) {
				continue;
			}
			return imageItem.getImageBytes();
		}
		return null;
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
                if (imageItem.hasError()) return true;
            }
        } else {
            //PartialRenderingException hack
            return imageItems.size() == 1 && imageItems.get(0).hasError();
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
        cached++;
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

    @NotNull
    public ImageItem[] getImageItemsAsArray() {
        return getImageItems().toArray(new ImageItem[getImageItems().size()]);
    }
}
