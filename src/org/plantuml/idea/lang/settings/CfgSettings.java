package org.plantuml.idea.lang.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.Nullable;

@Deprecated
@State(name = "PlantUmlSettings", storages = {@Storage("plantuml.cfg")})
public class CfgSettings implements PersistentStateComponent<PlantUmlSettings> {

    PlantUmlSettings settings = new PlantUmlSettings();

    public static CfgSettings getInstance() {
        return ServiceManager.getService(CfgSettings.class);
    }

    @Nullable
    @Override
    public PlantUmlSettings getState() {
        return settings;
    }

    @Override
    public void loadState(PlantUmlSettings state) {
        settings = state;
    }


}