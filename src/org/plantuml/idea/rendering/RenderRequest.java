package org.plantuml.idea.rendering;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.plantuml.PlantUml;

import java.io.File;

public class RenderRequest {
    @NotNull
    private final File baseDir;
    @NotNull
    private final String source;
    @NotNull
    private final PlantUml.ImageFormat format;
    private final int page;
    private final int zoom;
    private final Integer version;
    private boolean renderUrlLinks;
    private RenderCommand.Reason reason;
    protected boolean useSettings = true;

    public RenderRequest(@NotNull File baseDir,
                         @NotNull String source,
                         @NotNull PlantUml.ImageFormat format,
                         int page,
                         int zoom,
                         Integer version,
                         boolean renderUrlLinks,
                         RenderCommand.Reason reason) {
        this.baseDir = baseDir;
        this.source = source;
        this.format = format;
        this.page = page;
        this.zoom = zoom;
        this.version = version;
        this.renderUrlLinks = renderUrlLinks;
        this.reason = reason;
    }

    public RenderRequest(@NotNull RenderRequest renderRequest,
                         @NotNull PlantUml.ImageFormat format) {
        this.baseDir = renderRequest.baseDir;
        this.source = renderRequest.source;
        this.format = format;
        this.page = renderRequest.page;
        this.zoom = renderRequest.zoom;
        this.useSettings = renderRequest.useSettings;
        this.version = null;
    }

    @NotNull
    public File getBaseDir() {
        return baseDir;
    }

    @NotNull
    public String getSource() {
        return source;
    }

    @NotNull
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

    public boolean isRenderUrlLinks() {
        return renderUrlLinks;
    }

    public RenderCommand.Reason getReason() {
        return reason;
    }

    public boolean isUseSettings() {
        return useSettings;
    }

    public void setUseSettings(boolean useSettings) {
        this.useSettings = useSettings;
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("baseDir", baseDir)
                .append("format", format)
                .append("page", page)
                .append("zoom", zoom)
                .append("renderUrlLinks", renderUrlLinks)
                .append("reason", reason)
                .append("version", version)
                .append("useSettings", useSettings)
                .toString();
    }

    public boolean requestedRefreshOrIncludesChanged() {
        return reason == RenderCommand.Reason.REFRESH || reason == RenderCommand.Reason.INCLUDES;
    }

}
