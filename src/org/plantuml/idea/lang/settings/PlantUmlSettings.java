package org.plantuml.idea.lang.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.util.xmlb.XmlSerializerUtil;
import net.sourceforge.plantuml.cucadiagram.dot.GraphvizUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.plantuml.idea.util.UIUtils;
import org.plantuml.idea.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Max Gorbunov
 * @author Eugene Steinberg
 */
@State(name = "PlantUmlSettings", storages = {@Storage("plantuml.cfg")})
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

    public static PlantUmlSettings getInstance() {
        return ServiceManager.getService(PlantUmlSettings.class);
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
        applyPlantumlOptions();

        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
            PlantUmlToolWindow toolWindow = UIUtils.getPlantUmlToolWindow(project);
            if (toolWindow != null) {
                toolWindow.applyNewSettings(this);
            }
        }
    }

    public void applyPlantumlOptions() {
        boolean blank = StringUtils.isBlank(System.getProperty("GRAPHVIZ_DOT"));
        boolean blank1 = StringUtils.isBlank(System.getenv("GRAPHVIZ_DOT"));
        boolean propertyNotSet = blank && blank1;
        boolean propertySet = !blank || !blank1;

        if (propertyNotSet || (propertySet && !usePreferentiallyGRAPHIZ_DOT)) {
            if (String.valueOf(dotExecutable).isEmpty()) {
                GraphvizUtils.setDotExecutable(null);
            } else {
                GraphvizUtils.setDotExecutable(dotExecutable);
            }
        }

        if (StringUtils.isNotBlank(plantuml_limit_size)) {
            try {
                Integer.parseInt(plantuml_limit_size);
                System.setProperty("PLANTUML_LIMIT_SIZE", plantuml_limit_size);
            } catch (NumberFormatException e) {
                LOG.error("invalid PLANTUML_LIMIT_SIZE", e);
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
}
