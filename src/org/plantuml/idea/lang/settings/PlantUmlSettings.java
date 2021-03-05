package org.plantuml.idea.lang.settings;

import com.intellij.notification.Notifications;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.plantuml.ImageFormat;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.plantuml.idea.util.UIUtils;
import org.plantuml.idea.util.Utils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.plantuml.idea.util.UIUtils.notification;

/**
 * @author Max Gorbunov
 * @author Eugene Steinberg
 */
@State(name = "PlantUmlSettingsNew", storages = {@Storage("plantuml.xml")})
public class PlantUmlSettings implements PersistentStateComponent<PlantUmlSettings> {
    private static final Logger LOG = Logger.getInstance(PlantUmlSettings.class);

    private static final int CACHE_SIZE_DEFAULT_VALUE = 5;
    private static final int RENDER_DELAY_DEFAULT_VALUE = 100;
    private static final int SVG_SIZE = 16384;
    public static final String DEFAULT_SERVER = "http://www.plantuml.com/plantuml";

    private String dotExecutable = "";
    private boolean errorAnnotationEnabled = true;
    private boolean autoHide = true;
    private String renderDelay = String.valueOf(RENDER_DELAY_DEFAULT_VALUE);
    private String cacheSize = String.valueOf(CACHE_SIZE_DEFAULT_VALUE);
    private boolean autoRender = true;
    private boolean autoComplete = true;
    private boolean usePreferentiallyGRAPHIZ_DOT = false;
    private String encoding = "UTF-8";
    private String config = "";

    private boolean renderLinks = true;
    private boolean showUrlLinksBorder;
    private boolean linkOpensSearchBar = true;

    private String plantuml_limit_size;
    private String includedPaths;
    private boolean doNotDisplayErrors = false;

    private static boolean migratedCfg = false;
    private String customPlantumlJarPath;

    private boolean switchToBundledAfterUpdate = true;
    private boolean useBundled = true;
    private String lastBundledVersion;
    private String defaultExportFileFormat = "PNG";
    private boolean usePageTitles = true;
    private boolean useGrammar = true;
    private boolean keywordHighlighting = true;
    private boolean insertPair = true;

    private boolean displaySvg = true;
    boolean showChessboard = true;
    private boolean highlightInImages = false;
    private String maxSvgSize = String.valueOf(SVG_SIZE);
    private boolean svgPreviewScaling = true;
    private String serverPrefix = DEFAULT_SERVER;
    private boolean remoteRendering;
    private boolean useProxy;
    private String serverClipboardLinkType = "uml";

    public static PlantUmlSettings getInstance() {
        if (Utils.isUnitTest()) {
            return new PlantUmlSettings();
        }
        PlantUmlSettings service = ServiceManager.getService(PlantUmlSettings.class);
        if (!migratedCfg) {
            CfgSettings cfg = CfgSettings.getInstance();
            if (cfg.settings != null) {
                XmlSerializerUtil.copyBean(cfg.settings, service);
                service.applyState();
                cfg.settings = null;
            }
            migratedCfg = true;
        }
        return service;
    }

    /**
     * @see #getDefaultExportFileFormatEnum
     */
    @Deprecated
    public String getDefaultExportFileFormat() {
        if (this.defaultExportFileFormat == null) {
            return ImageFormat.PNG.name();
        }
        return this.defaultExportFileFormat;
    }

    public ImageFormat getDefaultExportFileFormatEnum() {
        return ImageFormat.from(defaultExportFileFormat);
    }

    public void setDefaultExportFileFormat(String defaultExportFileFormat) {
        this.defaultExportFileFormat = defaultExportFileFormat;
    }

    public boolean isShowChessboard() {
        return showChessboard;
    }

    public void setShowChessboard(boolean showChessboard) {
        this.showChessboard = showChessboard;
    }

    public String getLastBundledVersion() {
        return lastBundledVersion;
    }

    public void setLastBundledVersion(String lastBundledVersion) {
        this.lastBundledVersion = lastBundledVersion;
    }

    public boolean isRenderLinks() {
        return renderLinks;
    }

    public void setRenderLinks(boolean renderLinks) {
        this.renderLinks = renderLinks;
    }

    public String getDotExecutable() {
        return dotExecutable;
    }

    public void setDotExecutable(String dotExecutable) {
        this.dotExecutable = dotExecutable;
    }

    public boolean isErrorAnnotationEnabled() {
        return errorAnnotationEnabled;
    }

    public void setErrorAnnotationEnabled(boolean errorAnnotationEnabled) {
        this.errorAnnotationEnabled = errorAnnotationEnabled;
    }

    public boolean isAutoHide() {
        return autoHide;
    }

    public void setAutoHide(boolean autoHide) {
        this.autoHide = autoHide;
    }

    public String getCacheSize() {
        return cacheSize;
    }

    public int getCacheSizeAsInt() {
        return Utils.asInt(cacheSize, CACHE_SIZE_DEFAULT_VALUE);
    }

    public float getMaxSvgSizeAsFloat() {
        return Utils.asInt(maxSvgSize, SVG_SIZE);
    }

    public void setCacheSize(String cacheSize) {
        this.cacheSize = String.valueOf(Math.max(0, Utils.asInt(cacheSize, CACHE_SIZE_DEFAULT_VALUE)));
    }

    public String getRenderDelay() {
        return renderDelay;
    }

    public int getRenderDelayAsInt() {
        return Utils.asInt(renderDelay, RENDER_DELAY_DEFAULT_VALUE);
    }

    public void setRenderDelay(String renderDelay) {
        this.renderDelay = String.valueOf(Math.max(0, Utils.asInt(renderDelay, RENDER_DELAY_DEFAULT_VALUE)));
    }

    public void setAutoRender(boolean autoRender) {
        this.autoRender = autoRender;
    }

    public boolean isAutoRender() {
        return autoRender;
    }

    public boolean isAutoComplete() {
        return autoComplete;
    }

    public void setAutoComplete(boolean autoComplete) {
        this.autoComplete = autoComplete;
    }

    @Nullable
    @Override
    public PlantUmlSettings getState() {
        return this;
    }

    @Override
    public void loadState(PlantUmlSettings state) {
        XmlSerializerUtil.copyBean(state, this);
        applyState();
    }

    public void checkVersion(String version) {
        try {
            if (switchToBundledAfterUpdate && !useBundled && lastBundledVersion != null) {
                if (Objects.equals(lastBundledVersion, version)) {
                    //do nothing
                } else {
                    useBundled = true;
                    SwingUtilities.invokeLater(() -> Notifications.Bus.notify(notification().createNotification("Switching to a bundled PlantUML v" + version, MessageType.INFO)));
                }
            }
            lastBundledVersion = version;
        } catch (Throwable e) {
            LOG.error(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(o, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public void applyState() {
        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
            PlantUmlToolWindow toolWindow = UIUtils.getPlantUmlToolWindow(project);
            if (toolWindow != null) {
                toolWindow.applyNewSettings(this);
            }
        }
    }

    public boolean isUsePreferentiallyGRAPHIZ_DOT() {
        return usePreferentiallyGRAPHIZ_DOT;
    }

    public void setUsePreferentiallyGRAPHIZ_DOT(boolean usePreferentiallyGRAPHIZ_DOT) {
        this.usePreferentiallyGRAPHIZ_DOT = usePreferentiallyGRAPHIZ_DOT;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(final String config) {
        this.config = config;
    }

    public List<String> getConfigAsList() {
        if (StringUtils.isBlank(config)) {
            return new ArrayList<>();
        }
        String[] split = config.split("\n");
        return new ArrayList<>(Arrays.asList(split));
    }

    public boolean isShowUrlLinksBorder() {
        return showUrlLinksBorder;
    }

    public void setShowUrlLinksBorder(final boolean showUrlLinksBorder) {
        this.showUrlLinksBorder = showUrlLinksBorder;
    }

    public String getPLANTUML_LIMIT_SIZE() {
        return plantuml_limit_size;
    }

    public void setPLANTUML_LIMIT_SIZE(final String plantuml_limit_size) {
        this.plantuml_limit_size = plantuml_limit_size;
    }

    public String getIncludedPaths() {
        return includedPaths;
    }

    public void setIncludedPaths(final String includedPaths) {
        this.includedPaths = includedPaths;
    }

    public boolean isDoNotDisplayErrors() {
        return doNotDisplayErrors;
    }

    public void setDoNotDisplayErrors(boolean doNotDisplayErrors) {
        this.doNotDisplayErrors = doNotDisplayErrors;
    }

    public String getCustomPlantumlJarPath() {
        return customPlantumlJarPath;
    }

    public void setCustomPlantumlJarPath(final String customPlantumlJarPath) {
        this.customPlantumlJarPath = customPlantumlJarPath;
    }

    public boolean isSwitchToBundledAfterUpdate() {
        return switchToBundledAfterUpdate;
    }

    public void setSwitchToBundledAfterUpdate(final boolean switchToBundledAfterUpdate) {
        this.switchToBundledAfterUpdate = switchToBundledAfterUpdate;
    }

    public boolean isUseBundled() {
        return useBundled;
    }

    public void setUseBundled(boolean useBundled) {
        this.useBundled = useBundled;
    }

    public boolean isUsePageTitles() {
        return usePageTitles;
    }

    public void setUsePageTitles(final boolean usePageTitles) {
        this.usePageTitles = usePageTitles;
    }

    public boolean isUseGrammar() {
        return useGrammar;
    }

    public void setUseGrammar(final boolean useGrammar) {
        this.useGrammar = useGrammar;
    }

    public boolean isKeywordHighlighting() {
        return keywordHighlighting;
    }

    public void setKeywordHighlighting(final boolean keywordHighlighting) {
        this.keywordHighlighting = keywordHighlighting;
    }

    public boolean isInsertPair() {
        return insertPair;
    }

    public void setInsertPair(final boolean insertPair) {
        this.insertPair = insertPair;
    }

    public boolean isLinkOpensSearchBar() {
        return linkOpensSearchBar;
    }

    public void setLinkOpensSearchBar(final boolean linkOpensSearchBar) {
        this.linkOpensSearchBar = linkOpensSearchBar;
    }

    public boolean isDisplaySvg() {
        return displaySvg;
    }

    public void setDisplaySvg(final boolean displaySvg) {
        this.displaySvg = displaySvg;
    }

    public boolean isHighlightInImages() {
        return highlightInImages;
    }

    public void setHighlightInImages(final boolean highlightInImages) {
        this.highlightInImages = highlightInImages;
    }

    public String getMaxSvgSize() {
        return maxSvgSize;
    }

    public void setMaxSvgSize(final String maxSvgSize) {
        this.maxSvgSize = maxSvgSize;
    }

    public boolean isSvgPreviewScaling() {
        return svgPreviewScaling;
    }

    public void setSvgPreviewScaling(final boolean svgPreviewScaling) {
        this.svgPreviewScaling = svgPreviewScaling;
    }

    public String getServerPrefix() {
        if (TextUtils.isBlank(serverPrefix)) {
            serverPrefix = DEFAULT_SERVER;
        }
        if (!serverPrefix.startsWith("http")) {
            serverPrefix = "http://" + serverPrefix;
        }
        if (serverPrefix.endsWith("/")) {
            serverPrefix = serverPrefix.substring(0, serverPrefix.length() - 1);
        }
        return serverPrefix;
    }

    public void setServerPrefix(final String serverPrefix) {
        this.serverPrefix = serverPrefix;
    }

    public boolean isRemoteRendering() {
        return remoteRendering;
    }

    public void setRemoteRendering(final boolean remoteRendering) {
        this.remoteRendering = remoteRendering;
    }

    public boolean isUseProxy() {
        return useProxy;
    }

    public void setUseProxy(final boolean useProxy) {
        this.useProxy = useProxy;
    }

    public String getServerClipboardLinkType() {
        return serverClipboardLinkType;
    }

    public void setServerClipboardLinkType(final String serverClipboardLinkType) {
        this.serverClipboardLinkType = serverClipboardLinkType;
    }
}
