package org.plantuml.idea.rendering;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFileManager;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.NoSuchElementException;


public class RenderCache {
    public static final Logger logger = Logger.getInstance(RenderCache.class);

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

    public RenderCacheItem getCachedItem(String sourceFilePath, String source, int selectedPage, int scaledZoom, FileDocumentManager fileDocumentManager, VirtualFileManager virtualFileManager) {
        RenderCacheItem cacheItem = null;

        //error not cached in ArrayDeque
        if (displayedItem != null
                && displayedItem.getRenderResult().hasError()
                && !displayedItem.includedFilesChanged(fileDocumentManager, virtualFileManager)
                && !displayedItem.imageMissingOrSourceOrZoomChanged(source, selectedPage, scaledZoom)) {
            logger.debug("returning displayedItem (error=true, requiresRendering=false)");
            return displayedItem;
        }


        if (displayedItem != null && displayedItem.getSourceFilePath().equals(sourceFilePath) && displayedItem.getScaledZoom() == scaledZoom) {
            cacheItem = displayedItem;
            if (cacheItem.getSource().equals(source)) {
                logger.debug("returning displayedItem");
                return cacheItem;
            }
        }


        Iterator<RenderCacheItem> iterator = cacheItems.descendingIterator();
        while (iterator.hasNext()) {
            RenderCacheItem next = iterator.next();
            if (next.getSourceFilePath().equals(sourceFilePath) && next.getScaledZoom() == scaledZoom) {
                if (cacheItem == null) {
                    cacheItem = next;
                    if (cacheItem.getSource().equals(source)) {
                        break;
                    }
                } else {
                    if (next.getSource().equals(source)) {
                        cacheItem = next;
                        break;
                    }
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

    public RenderCacheItem getLast() {
        try {
            return cacheItems.getLast();
        } catch (NoSuchElementException e) {
            return null;
        }
    }
}
