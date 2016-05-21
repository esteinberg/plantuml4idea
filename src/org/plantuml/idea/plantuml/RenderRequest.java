package org.plantuml.idea.plantuml;

import java.io.File;

public class RenderRequest {
    private File baseDir;
    private String source;
    private PlantUml.ImageFormat format;
    private int page;
    private int zoom;

    /**
     * @param baseDir
     * @param source  plantUml source code
     * @param format  desired image format
     */
    public RenderRequest(File baseDir, String source, PlantUml.ImageFormat format, int page, int zoom) {
        this.baseDir = baseDir;
        this.source = source;
        this.format = format;
        this.page = page;
        this.zoom = zoom;
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

    public void setPage(int page) {
        this.page = page;
    }

    public void setFormat(PlantUml.ImageFormat format) {
        this.format = format;
    }

}
