package org.plantuml.idea.lang.settings;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.util.xmlb.XmlSerializerUtil;
import net.sourceforge.plantuml.cucadiagram.dot.GraphvizUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.plantuml.idea.util.UIUtils;
import org.plantuml.idea.util.Utils;

/**
 * @author Max Gorbunov
 * @author Eugene Steinberg
 */
@State(name = "PlantUmlSettings", storages = {@Storage(file = StoragePathMacros.APP_CONFIG + "/plantuml.cfg")})
public class PlantUmlSettings implements PersistentStateComponent<PlantUmlSettings> {
    private String dotExecutable = "";
    private boolean errorAnnotationEnabled = true;
    private boolean autoHide = true;
    private String renderDelay = "100";

    public static PlantUmlSettings getInstance() {
        return ServiceManager.getService(PlantUmlSettings.class);
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

    public String getRenderDelay() {
        return renderDelay;
    }

    public int getRenderDelayAsInt() {
        return Utils.asInt(renderDelay, 100);
    }

    public void setRenderDelay(String renderDelay) {
        int i = Utils.asInt(renderDelay, 100);
        this.renderDelay = String.valueOf(i);
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
        if (String.valueOf(dotExecutable).isEmpty()) {
            GraphvizUtils.setDotExecutable(null);
        } else {
            GraphvizUtils.setDotExecutable(dotExecutable);
        }
        
        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
            PlantUmlToolWindow toolWindow = UIUtils.getPlantUmlToolWindow(project);
            if (toolWindow != null) {
                toolWindow.applyNewSettings(this);
            }
        }
    }

}
