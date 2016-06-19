package org.plantuml.idea.rendering;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Iterator;

import static org.plantuml.idea.intentions.ReverseArrowIntention.logger;

public class RenderCache {
    private ArrayDeque<RenderCacheItem> cacheItems;
    private int maxCacheSize;
    private RenderCacheItem displayedItem;

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

    public RenderCacheItem getCachedItem(String sourceFilePath, String source, int selectedPage, int zoom, FileEditorManager fileEditorManager, FileDocumentManager fileDocumentManager) {
        RenderCacheItem cacheItem = null;
        boolean checkCurrentItemSourceEquals = true;
        Iterator<RenderCacheItem> iterator = cacheItems.descendingIterator();

        //error not cached in ArrayDeque
        if (displayedItem != null
                && displayedItem.getRenderResult().hasError()
                && !displayedItem.renderRequired(source, selectedPage, fileEditorManager, fileDocumentManager)) {
            logger.debug("returning displayedItem (error=true, requiresRendering=false)");
            return displayedItem;
        }


        while (iterator.hasNext()) {
            RenderCacheItem next = iterator.next();
            if (next.getSourceFilePath().equals(sourceFilePath) && next.getZoom() == zoom) {
                if (cacheItem == null) {
                    cacheItem = next;
                } else {
                    if (checkCurrentItemSourceEquals && cacheItem.getSource().equals(source)) {
                        break;
                    } else if (next.getSource().equals(source)) {
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
        if (cacheItems.size() > 0 && cacheItems.size() + 1 > maxCacheSize) {
            cacheItems.removeFirst();
        }
        cacheItems.add(cacheItem);
    }

    public boolean isDisplayed(RenderCacheItem cachedItem, int page) {
        return displayedItem == cachedItem && cachedItem.getRequestedPage() == page;
    }

    public RenderCacheItem getDisplayedItem() {
        return displayedItem;
    }

    public void setDisplayedItem(RenderCacheItem displayedItem) {
        this.displayedItem = displayedItem;
    }

    public boolean isOlderRequest(RenderCacheItem cachedItem) {
        if (displayedItem != null) {
            return displayedItem.getVersion() > cachedItem.getVersion();
        } else {
            return false;
        }
    }

    public static boolean isChanged(FileDocumentManager fileDocumentManager, File file, Long timestamp, Document document) {
        return timestamp < file.lastModified() || (document != null && fileDocumentManager.isDocumentUnsaved(document));
    }


    public void removeFromCache(RenderCacheItem cachedItem) {
        logger.debug("force removing from cache " + cachedItem);
        cacheItems.remove(cachedItem);
        if (displayedItem == cachedItem) {
            displayedItem = null;
        }
    }

    public boolean isSameFile(RenderCacheItem cachedItem) {
        if (displayedItem != null && cachedItem != null) {
            return displayedItem.getSourceFilePath().equals(cachedItem.getSourceFilePath());
        }
        return false;
    }

    public void clear() {
        cacheItems.clear();
    }
}
