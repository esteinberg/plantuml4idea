package org.plantuml.idea.rendering;

public enum RenderingType {
    PARTIAL,
    NORMAL;

    public boolean renderingTypeChanged(RenderCacheItem cachedItem) {
        return cachedItem != null && cachedItem.getRenderResult().getStrategy() != this;
    }
}
