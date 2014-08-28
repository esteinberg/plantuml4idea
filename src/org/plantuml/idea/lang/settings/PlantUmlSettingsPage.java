package org.plantuml.idea.lang.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Max Gorbunov
 */
public class PlantUmlSettingsPage implements Configurable {
    private JPanel panel;
    private JTextField textFieldDotExecutable;

    @Nls
    @Override
    public String getDisplayName() {
        return "PlantUML";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return panel;
    }

    @Override
    public boolean isModified() {
        return !textFieldDotExecutable.getText().equals(PlantUmlSettings.getInstance().getDotExecutable());
    }

    @Override
    public void apply() throws ConfigurationException {
        PlantUmlSettings.getInstance().setDotExecutable(textFieldDotExecutable.getText());
    }

    @Override
    public void reset() {
        textFieldDotExecutable.setText(PlantUmlSettings.getInstance().getDotExecutable());
    }

    @Override
    public void disposeUIResources() {
    }
}
