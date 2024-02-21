package org.plantuml.idea.rendering;

import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.plantuml.ImageFormat;
import org.plantuml.idea.preview.Zoom;
import org.plantuml.idea.util.UIUtils;

import java.io.File;

public class RenderRequest {
    private final String sourceFilePath;
    @NotNull
    private final String source;
    @NotNull
    private final ImageFormat format;
    private final int page;
    @NotNull
    private Zoom zoom;
    private final Integer version;
    private boolean renderUrlLinks;
    private RenderCommand.Reason reason;
    protected boolean testRequest = false;
    private boolean disableSvgZoom;
    private Project project;

    public RenderRequest(String sourceFilePath,
                         @NotNull String source,
                         @NotNull ImageFormat format,
                         int page,
                         @NotNull
                         Zoom zoom,
                         Integer version,
                         boolean renderUrlLinks,
                         RenderCommand.Reason reason, Project project) {
        this.sourceFilePath = sourceFilePath;
        this.source = source;
        this.format = format;
        this.page = page;
        this.zoom = zoom;
        this.version = version;
        this.renderUrlLinks = renderUrlLinks;
        this.reason = reason;
        this.project = project;
    }

    public RenderRequest(@NotNull RenderRequest renderRequest,
                         @NotNull ImageFormat format) {
        this.project = renderRequest.project;
        this.sourceFilePath = renderRequest.sourceFilePath;
        this.source = renderRequest.source;
        this.format = format;
        this.page = renderRequest.page;
        this.zoom = renderRequest.zoom;
        this.testRequest = renderRequest.testRequest;
        this.version = null;
    }

    public void setZoom(@NotNull Zoom zoom) {
        this.zoom = zoom;
    }

    public Project getProject() {
        return project;
    }

    @NotNull
    public String getSource() {
        return source;
    }

    public String getSourceFilePath() {
        return sourceFilePath;
    }

    public File getSourceFile() {
        return new File(sourceFilePath);
    }

    @NotNull
    public ImageFormat getFormat() {
        return format;
    }

    public int getPage() {
        return page;
    }

    @NotNull
    public Zoom getZoom() {
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

    public boolean isTestRequest() {
        return testRequest;
    }

    public void setTestRequest(boolean testRequest) {
        this.testRequest = testRequest;
    }

    public boolean requestedRefreshOrIncludesChanged() {
        return reason == RenderCommand.Reason.REFRESH || reason == RenderCommand.Reason.INCLUDES;
    }

    public File getBaseDir() {
        return UIUtils.getParent(new File(sourceFilePath));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("sourceFilePath", sourceFilePath)
                .append("format", format)
                .append("page", page)
                .append("scaledZoom", zoom)
                .append("renderUrlLinks", renderUrlLinks)
                .append("reason", reason)
                .append("version", version)
                .append("useSettings", testRequest)
                .append("disableSvgZoom", disableSvgZoom)
                .toString();
    }


    public void disableSvgZoom() {
        disableSvgZoom = true;
    }

    public boolean isDisableSvgZoom() {
        return disableSvgZoom;
    }

}
