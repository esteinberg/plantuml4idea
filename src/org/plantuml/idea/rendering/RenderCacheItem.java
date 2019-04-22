package org.plantuml.idea.rendering;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

public class RenderCacheItem {


    private Integer version;
    private final String sourceFilePath;
    /**
     * source is also cached in imagesWithData
     */
    private final String source;
    private final File baseDir;
    private final int zoom;
    private final Map<File, Long> includedFiles;
    private final RenderRequest renderRequest;
    private final String[] titles;
    private final RenderResult renderResult;
    private final ImageItem[] imageItems;
    private int requestedPage;

    public RenderCacheItem(@NotNull RenderRequest renderRequest, String sourceFilePath, String source, File baseDir, int zoom, int requestedPage, Map<File, Long> includedFiles, RenderResult renderResult, ImageItem[] imageItems, Integer version) {
        this.sourceFilePath = sourceFilePath;
        this.source = source;
        this.baseDir = baseDir;
        this.zoom = zoom;
        this.requestedPage = requestedPage;
        this.includedFiles = includedFiles;
        this.renderResult = renderResult;
        this.imageItems = imageItems;
        this.version = version;
        this.renderRequest = renderRequest;
        this.titles = new String[imageItems.length];
        for (int i = 0; i < imageItems.length; i++) {
            ImageItem imageItem = imageItems[i];
            titles[i] = imageItem != null ? imageItem.getTitle() : null;
        }
    }


    public RenderRequest getRenderRequest() {
        return renderRequest;
    }

    public String[] getTitles() {
        return titles;
    }

    public boolean imageMissingOrZoomChanged(int page, int zoom) {
        if (imageMissing(page)) {
            return true;
        }
        if (zoom != this.zoom) {
            return true;
        }
        return false;
    }

    public boolean imageMissingOrSourceChanged(String source, int page) {
        if (imageMissing(page)) {
            return true;
        }
        if (!this.source.equals(source)) {
            return true;
        }
        return false;
    }

    private boolean imageMissing(int page) {
        if (page == -1) {
            for (int i = 0; i < imageItems.length; i++) {
                if (!hasImage(i)) {
                    return true;
                }
            }
        } else {
            if (!hasImage(page)) {
                return true;
            }
        }
        return false;
    }


    public String getSourceFilePath() {
        return sourceFilePath;
    }

    public String getSource() {
        return source;
    }

    public File getBaseDir() {
        return baseDir;
    }

    public RenderResult getRenderResult() {
        return renderResult;
    }

    public ImageItem[] getImageItems() {
        return imageItems;
    }

    public boolean includedFilesChanged(FileDocumentManager fileDocumentManager, VirtualFileManager virtualFileManager) {
        boolean result = false;
        if (includedFiles != null) {
            for (Map.Entry<File, Long> fileLongEntry : includedFiles.entrySet()) {
                File file = fileLongEntry.getKey();
                Long timestamp = fileLongEntry.getValue();
                VirtualFile virtualFile = virtualFileManager.findFileByUrl("file://" + file.getAbsolutePath());
                Document document = null;
                if (virtualFile != null) {
                    document = fileDocumentManager.getDocument(virtualFile);
                }
                if (timestamp != null && isChanged(fileDocumentManager, file, timestamp, document)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    private static boolean isChanged(FileDocumentManager fileDocumentManager, File file, Long timestamp, Document document) {
        return timestamp < file.lastModified() || (document != null && fileDocumentManager.isDocumentUnsaved(document));
    }
           
    public boolean isIncludedFile(@Nullable VirtualFile file) {
        if (file == null) {
            return false;
        }
        File key = new File(file.getPath());
        Long aLong = includedFiles.get(key);
        return aLong != null;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Integer getVersion() {
        return version;
    }

    public int getZoom() {
        return zoom;
    }

    public int getRequestedPage() {
        return requestedPage;
    }

    public void setRequestedPage(int requestedPage) {
        this.requestedPage = requestedPage;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("version", version)
                .append("source", source)
                .append("renderRequest", renderRequest)
                .append("sourceFilePath", sourceFilePath)
                .append("baseDir", baseDir)
                .append("zoom", zoom)
                .append("page", requestedPage)
                .append("includedFiles", includedFiles)
                .append("imageResult", renderResult)
                .append("imagesWithData", Arrays.toString(imageItems))
                .toString();
    }

    public String getImagesItemPageSource(int page) {
        if (imageItems.length > page) {
            ImageItem imageItem = imageItems[page];
            if (imageItem != null) {
                return imageItem.getPageSource();
            }
        }
        return null;
    }

    @Nullable
    public ImageItem getImageItem(int page) {
        return imageItems.length > page ? imageItems[page] : null;
    }

    public boolean hasImage(int i) {
        ImageItem imageItem = getImageItem(i);
        if (imageItem != null) {
            return imageItem.hasImage();
        }
        return false;
    }

    public boolean titleChaged(String s, int i) {
        ImageItem imageItem = getImageItem(i);
        if (imageItem != null) {
            String title = imageItem.getTitle();
            if (title == null && s == null) {
                return false;
            }
            return !StringUtils.equals(s, title);
        }
        return true;
    }
}
