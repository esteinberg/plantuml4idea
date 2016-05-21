package org.plantuml.idea.toolwindow;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.plantuml.idea.plantuml.PlantUmlResult;
import org.plantuml.idea.util.ImageWithUrlData;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;

public class RenderCache {
    private ArrayDeque<RenderCacheItem> cacheItems;
    private int maxCacheSize;
    RenderCacheItem displayedItem;

    public RenderCache(int maxCacheSize) {
        cacheItems = new ArrayDeque<RenderCacheItem>(maxCacheSize);
        this.maxCacheSize = maxCacheSize;
    }

    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
        while (cacheItems.size() > maxCacheSize) {
            cacheItems.removeFirst();
        }
    }

    public RenderCacheItem getCachedItem(String sourceFilePath, String source, int page, int zoom) {
        RenderCacheItem cacheItem = null;
        boolean checkCurrentItemSourceEquals = true;
        Iterator<RenderCacheItem> iterator = cacheItems.descendingIterator();
        while (iterator.hasNext()) {
            RenderCacheItem next = iterator.next();
            if (next.sourceFilePath.equals(sourceFilePath) && next.zoom == zoom) {
                if (cacheItem == null) {
                    cacheItem = next;
                } else {
                    if (checkCurrentItemSourceEquals && cacheItem.source.equals(source)) {
                        break;
                    } else if (next.source.equals(source)) {
                        cacheItem = next;
                        break;
                    }
                    checkCurrentItemSourceEquals = false;
                }
            }
        }
        return cacheItem;
    }

    public void addToCache(RenderCacheItem cacheItem) {
        if (cacheItems.size() > 0 && cacheItems.size() + 1 >= maxCacheSize) {
            cacheItems.removeFirst();
        }
        cacheItems.add(cacheItem);
    }

    public boolean isDisplayed(RenderCacheItem cachedItem, int page) {
        return displayedItem == cachedItem && cachedItem.page == page;
    }

    public boolean isOlderRequest(RenderCacheItem cachedItem) {
        if (displayedItem != null) {
            return displayedItem.version > cachedItem.version;
        } else {
            return false;
        }
    }

    public static class RenderCacheItem {


        private Integer version;
        private final String sourceFilePath;
        private final String source;
        private final File baseDir;
        private final int zoom;
        private final int page;
        private final Map<File, Long> includedFiles;
        private final PlantUmlResult imageResult;
        private final ImageWithUrlData[] imagesWithData;

        public RenderCacheItem(String sourceFilePath, String source, File baseDir, int zoom, int page, Map<File, Long> includedFiles, PlantUmlResult imageResult, ImageWithUrlData[] imagesWithData, Integer version) {
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

            return includedFilesChanged(project);
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
                if (imagesWithData.length < page || imagesWithData[page] == null) {
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

        public PlantUmlResult getImageResult() {
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
                boolean changed = isChanged(fileDocumentManager, key, aLong, fileDocumentManager.getDocument(file));
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
                        if (timestamp != null && isChanged(fileDocumentManager, key, timestamp, selectedTextEditor.getDocument())) {
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
    }

    public static boolean isChanged(FileDocumentManager fileDocumentManager, File file, Long timestamp, Document document) {
        return timestamp < file.lastModified() || (document != null && fileDocumentManager.isDocumentUnsaved(document));
    }


}
