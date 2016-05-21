package org.plantuml.idea.plantuml;

import java.io.File;

public class RenderRequest {
    private final File baseDir;
    private final String source;
    private final PlantUml.ImageFormat format;
    private final int page;
    private final int zoom;
    private final Integer version;

    public RenderRequest(File baseDir, String source, PlantUml.ImageFormat format, int page, int zoom, Integer version) {
        this.baseDir = baseDir;
        this.source = source;
        this.format = format;
        this.page = page;
        this.zoom = zoom;
        this.version = version;
    }

    public RenderRequest(RenderRequest renderRequest, PlantUml.ImageFormat format) {
        this.baseDir = renderRequest.baseDir;
        this.source = renderRequest.source;
        this.format = format;
        this.page = renderRequest.page;
        this.zoom = renderRequest.zoom;
        this.version = null;
    }

    public File getBaseDir() {
        return baseDir;
    }

    public String getSource() {
        return source;
    }

    public PlantUml.ImageFormat getFormat() {
        return format;
    }

    public int getPage() {
        return page;
    }

    public int getZoom() {
        return zoom;
    }


    public Integer getVersion() {
        return version;
    }

}
