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
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.external.Classloaders;
import org.plantuml.idea.external.PlantUmlFacade;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.plantuml.idea.util.UIUtils;
import org.plantuml.idea.util.Utils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.plantuml.idea.util.UIUtils.NOTIFICATION;

/**
 * @author Max Gorbunov
 * @author Eugene Steinberg
 */
@State(name = "PlantUmlSettingsNew", storages = {@Storage("plantuml.xml")})
public class PlantUmlSettings implements PersistentStateComponent<PlantUmlSettings> {
    private static final Logger LOG = Logger.getInstance(PlantUmlSettings.class);

    private static final int CACHE_SIZE_DEFAULT_VALUE = 5;
    private static final int RENDER_DELAY_DEFAULT_VALUE = 100;

    private String dotExecutable = "";
    private boolean errorAnnotationEnabled = true;
    private boolean autoHide = true;
    private boolean renderUrlLinks = false;
    private String renderDelay = String.valueOf(RENDER_DELAY_DEFAULT_VALUE);
    private String cacheSize = String.valueOf(CACHE_SIZE_DEFAULT_VALUE);
    private boolean autoRender = true;
    private boolean autoComplete = true;
    private boolean usePreferentiallyGRAPHIZ_DOT = false;
    private String encoding = "UTF-8";
    private String config = "";
    private boolean showUrlLinksBorder;
    private String plantuml_limit_size;
    private String includedPaths;
    private boolean doNotDisplayErrors = false;

    private static boolean migratedCfg = false;
    private String customPlantumlJarPath;

    private boolean switchToBundledAfterUpdate = true;
    private boolean useBundled = true;
    private String lastBundledVersion;
    private boolean usePageTitles = true;
    private boolean useGrammar = true;
    private boolean keywordHighlighting = true;
    private boolean insertPair = true;


    public static PlantUmlSettings getInstance() {
        if (Classloaders.isUnitTest()) {
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

    public String getLastBundledVersion() {
        return lastBundledVersion;
    }

    public void setLastBundledVersion(String lastBundledVersion) {
        this.lastBundledVersion = lastBundledVersion;
    }

    public boolean isRenderUrlLinks() {
        return renderUrlLinks;
    }

    public void setRenderUrlLinks(boolean renderUrlLinks) {
        this.renderUrlLinks = renderUrlLinks;
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

        try {
            PlantUmlFacade bundled = PlantUmlFacade.getBundled();
            String version = bundled.version();
            if (switchToBundledAfterUpdate && !useBundled && lastBundledVersion != null && !Objects.equals(lastBundledVersion, version)) {
                useBundled = true;
                SwingUtilities.invokeLater(() -> Notifications.Bus.notify(NOTIFICATION.createNotification("Switching to a bundled PlantUML v" + version, MessageType.INFO)));
            }
            lastBundledVersion = version;
        } catch (Throwable e) {
            LOG.error(e);
        }

        applyState();
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
}
