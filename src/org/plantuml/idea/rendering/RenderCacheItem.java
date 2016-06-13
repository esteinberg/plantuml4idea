package org.plantuml.idea.rendering;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.List;
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
    private final List<String> titles;
    private final RenderResult renderResult;
    private final ImageItem[] imageItems;
    private int requestedPage;

    public RenderCacheItem(@NotNull RenderRequest renderRequest, String sourceFilePath, String source, File baseDir, int zoom, int requestedPage, Map<File, Long> includedFiles, RenderResult renderResult, ImageItem[] imageItems, Integer version, List<String> titles) {
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
        this.titles = titles;
    }

    public RenderRequest getRenderRequest() {
        return renderRequest;
    }

    public List<String> getTitles() {
        return titles;
    }

    public boolean renderRequired(@NotNull Project project, int page) {
        if (this.requestedPage != page) {
            return true;
        }
        if (imageMissing(page)) {
            return true;
        }
        return includedFilesChanged(project);
    }

    public boolean renderRequired(@NotNull Project project, String source, int page) {
        if (this.requestedPage != page) {
            return true;
        }
        if (!this.source.equals(source)) {
            return true;
        }
        if (imageMissing(page)) {
            return true;
        }

        return includedFilesChanged(project);
    }

    private boolean imageSourceChanged(int page, String source) {
        if (page == -1) {
            for (int i = 0; i < imageItems.length; i++) {
                ImageItem imageItem = imageItems[i];
                if (imageItem != null && !source.equals(imageItem.getDocumentSource())) {
                    return true;
                }
            }
        } else {
            if (imageItems.length > page && !imageItems[page].getDocumentSource().equals(source)) {
                return true;
            }
        }
        return false;
    }

    private boolean imageMissing(int page) {
        if (page == -1) {
            for (int i = 0; i < imageItems.length; i++) {
                ImageItem imageItem = imageItems[i];
                if (imageItem == null) {
                    return true;
                }
            }
        } else {
            if (imageItems.length > page && imageItems[page] == null) {
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

    public boolean isIncludedFileChanged(@NotNull VirtualFile file) {
        File key = new File(file.getPath());
        Long aLong = includedFiles.get(key);
        if (aLong != null) {
            FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
            boolean changed = RenderCache.isChanged(fileDocumentManager, key, aLong, fileDocumentManager.getDocument(file));
            if (changed) {
                return true;
            }
        }
        return false;
    }

    private boolean includedFilesChanged(@NotNull Project project) {
        boolean result = false;
        Editor selectedTextEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (selectedTextEditor != null) {
            FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
            VirtualFile file = fileDocumentManager.getFile(selectedTextEditor.getDocument());
            if (file != null) {
                String path = file.getPath();
                if (includedFiles != null) {
                    File key = new File(path);
                    Long timestamp = includedFiles.get(key);
                    if (timestamp != null && RenderCache.isChanged(fileDocumentManager, key, timestamp, selectedTextEditor.getDocument())) {
                        result = true;
                    }
                }
            }
        }
        return result;
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


}
