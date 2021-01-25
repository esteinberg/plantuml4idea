package org.plantuml.idea.rendering;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.plantuml.idea.toolwindow.Zoom;

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
            RenderCacheItem renderCacheItem = cacheItems.removeFirst();
            if (renderCacheItem != displayedItem) {
                renderCacheItem.dispose();
            }
        }
    }

    public RenderCacheItem getCachedItem(String sourceFilePath, String source, int selectedPage, Zoom zoom, FileDocumentManager fileDocumentManager, VirtualFileManager virtualFileManager) {
        RenderCacheItem cacheItem = null;

        //error not cached in ArrayDeque
        if (displayedItem != null
                && displayedItem.getRenderResult().hasError()
                && !displayedItem.includedFilesChanged(fileDocumentManager, virtualFileManager)
                && !displayedItem.imageMissingOrZoomChanged(selectedPage, zoom)
                && !displayedItem.sourceChanged(source)
        ) {
            logger.debug("returning displayedItem (error=true, requiresRendering=false)");
            return displayedItem;
        }


        if (displayedItem != null && displayedItem.getSourceFilePath().equals(sourceFilePath) && displayedItem.equals(zoom)) {
            cacheItem = displayedItem;
            if (cacheItem.getSource().equals(source)) {
                logger.debug("returning displayedItem");
                return cacheItem;
            }
        }


        Iterator<RenderCacheItem> iterator = cacheItems.descendingIterator();
        while (iterator.hasNext()) {
            RenderCacheItem next = iterator.next();
            if (next.getSourceFilePath().equals(sourceFilePath) && next.getZoom().equals(zoom)) {
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
            RenderCacheItem renderCacheItem = cacheItems.removeFirst();
            renderCacheItem.dispose();
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
        cachedItem.dispose();
    }

    public boolean isSameFile(RenderCacheItem cachedItem) {
        if (displayedItem != null && cachedItem != null) {
            return displayedItem.getSourceFilePath().equals(cachedItem.getSourceFilePath());
        }
        return false;
    }

    public void clear() {
        while (true) {
            RenderCacheItem poll = cacheItems.poll();
            if (poll == null) break;
            poll.dispose();
        }
    }

    public RenderCacheItem getLast() {
        try {
            return cacheItems.getLast();
        } catch (NoSuchElementException e) {
            return null;
        }
    }
}
