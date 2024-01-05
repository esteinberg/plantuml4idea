package org.plantuml.idea.rendering;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.preview.Zoom;
import org.plantuml.idea.settings.PlantUmlSettings;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.NoSuchElementException;

@Service
public class RenderCache {
    public static final Logger logger = Logger.getInstance(RenderCache.class);

    private ArrayDeque<RenderCacheItem> cacheItems;
    private int maxCacheSize;

    public RenderCache() {
        PlantUmlSettings settings = PlantUmlSettings.getInstance();
        this.maxCacheSize = settings.getCacheSizeAsInt();
        cacheItems = new ArrayDeque<RenderCacheItem>(maxCacheSize);
        ApplicationManager.getApplication().getMessageBus().connect()
                .subscribe(PlantUmlSettings.SettingsChangedListener.TOPIC, new PlantUmlSettings.SettingsChangedListener() {
                    @Override
                    public void onSettingsChange(@NotNull PlantUmlSettings settings) {
                        clear();
                        setMaxCacheSize(settings.getCacheSizeAsInt());
                    }
                });
    }

    @NotNull
    public static RenderCache getInstance() {
        return ApplicationManager.getApplication().getService(RenderCache.class);
    }

    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
        while (cacheItems.size() > maxCacheSize) {
            RenderCacheItem renderCacheItem = cacheItems.removeFirst();
//            if (renderCacheItem != displayedItem) {
//                renderCacheItem.dispose();
//            }
        }
    }

    public RenderCacheItem getCachedItem(String sourceFilePath, String source, int selectedPage, Zoom zoom, FileDocumentManager fileDocumentManager, VirtualFileManager virtualFileManager, RenderCacheItem displayedItem) {
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

        if (displayedItem != null && displayedItem.getSourceFilePath().equals(sourceFilePath) && displayedItem.getZoom().equals(zoom)) {
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
//            renderCacheItem.dispose();
        }

        if (maxCacheSize > 0) {
            cacheItems.add(cacheItem);
        }
    }


    public boolean isOlderRequest(RenderCacheItem cachedItem, RenderCacheItem displayedItem) {
        if (displayedItem != null) {
            return displayedItem.getVersion() > cachedItem.getVersion();
        } else {
            return false;
        }
    }


    public void removeFromCache(RenderCacheItem cachedItem) {
        logger.debug("force removing from cache ", cachedItem);
        cacheItems.remove(cachedItem);
//        cachedItem.dispose();
    }

    public boolean isSameFile(RenderCacheItem cachedItem, RenderCacheItem displayedItem) {
        if (displayedItem != null && cachedItem != null) {
            return displayedItem.getSourceFilePath().equals(cachedItem.getSourceFilePath());
        }
        return false;
    }

    public void clear() {
        while (true) {
            RenderCacheItem poll = cacheItems.poll();
            if (poll == null) break;
//            poll.dispose();
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
