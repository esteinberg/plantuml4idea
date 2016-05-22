package org.plantuml.idea.rendering;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.plantuml.idea.util.ImageWithUrlData;

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
    private int page;
    private final Map<File, Long> includedFiles;
    private final RenderResult imageResult;
    private final ImageWithUrlData[] imagesWithData;

    public RenderCacheItem(String sourceFilePath, String source, File baseDir, int zoom, int page, Map<File, Long> includedFiles, RenderResult imageResult, ImageWithUrlData[] imagesWithData, Integer version) {
        this.sourceFilePath = sourceFilePath;
        this.source = source;
        this.baseDir = baseDir;
        this.zoom = zoom;
        this.page = page;
        this.includedFiles = includedFiles;
        this.imageResult = imageResult;
        this.imagesWithData = imagesWithData;
        this.version = version;
    }

    public boolean renderRequired(Project project, String source, int page) {
        if (!this.source.equals(source)) {
            return true;
        }
        if (imageMissing(page)) {
            return true;
        }
        if (imageSourceChanged(page, source)) {
            return true;
        }

        return includedFilesChanged(project);
    }

    private boolean imageSourceChanged(int page, String source) {
        if (page == -1) {
            for (int i = 0; i < imagesWithData.length; i++) {
                ImageWithUrlData imageWithUrlData = imagesWithData[i];
                if (imageWithUrlData != null && !source.equals(imageWithUrlData.getSource())) {
                    return true;
                }
            }
        } else {
            if (imagesWithData.length > page && !imagesWithData[page].getSource().equals(source)) {
                return true;
            }
        }
        return false;
    }

    private boolean imageMissing(int page) {
        if (page == -1) {
            for (int i = 0; i < imagesWithData.length; i++) {
                ImageWithUrlData imageWithUrlData = imagesWithData[i];
                if (imageWithUrlData == null) {
                    return true;
                }
            }
        } else {
            if (imagesWithData.length > page && imagesWithData[page] == null) {
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

    public RenderResult getImageResult() {
        return imageResult;
    }

    public ImageWithUrlData[] getImagesWithData() {
        return imagesWithData;
    }

    public boolean isIncludedFileChanged(VirtualFile file) {
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

    private boolean includedFilesChanged(Project project) {
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

    public boolean isIncludedFile(VirtualFile file) {
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

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("version", version)
                .append("sourceFilePath", sourceFilePath)
                .append("baseDir", baseDir)
                .append("zoom", zoom)
                .append("page", page)
                .append("includedFiles", includedFiles)
                .append("imageResult", imageResult)
                .append("imagesWithData", Arrays.toString(imagesWithData))
                .toString();
    }
}
