package org.plantuml.idea.rendering;

import com.intellij.openapi.util.io.FileUtil;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;


/**
 * @author Eugene Steinberg
 */
public class RenderResult {

    public static final String TITLE_ONLY = "TITLE ONLY";

    private RenderingType strategy;
    private final List<ImageItem> imageItems;
    private final int pages;
    private int rendered;
    private int updatedTitles;
    private int cached;
    private LinkedHashMap<File, Long> includedFiles = new LinkedHashMap<>();

    public RenderResult(RenderingType strategy, int totalPages) {
        this.strategy = strategy;
        if (totalPages == 0) {
            this.imageItems = Collections.emptyList();
        } else {
            this.imageItems = new ArrayList<ImageItem>(totalPages);
        }
        this.pages = totalPages;
    }

    public Map<File, Long> getIncludedFiles() {
        return includedFiles;
    }

    public void setIncludedFiles(LinkedHashMap<File, Long> includedFiles) {
        this.includedFiles = includedFiles;
    }

    public void addIncludedFiles(LinkedHashMap<File, Long> map) {
        includedFiles.putAll(map);
    }

    public byte[] getFirstDiagramBytes() {
        for (ImageItem imageItem : imageItems) {
            if (TITLE_ONLY.equals(imageItem.getDescription())) {
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
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
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


    public boolean includedFilesContains(File file) {
        for (File f : includedFiles.keySet()) {
            if (FileUtil.filesEqual(f, file)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    public String resultMessage(long totalTime) {
        int rendered = getRendered();
        int updatedTitles = getUpdatedTitles();
        int cached = getCached();
        String message = totalTime + "ms ["
                + rendered + ","
                + updatedTitles + ","
                + cached + "]";
        return message;
    }

}
