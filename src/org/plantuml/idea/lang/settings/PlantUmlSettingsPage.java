package org.plantuml.idea.lang.settings;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Max Gorbunov
 */
public class PlantUmlSettingsPage implements Configurable {
    private JPanel panel;
    private JTextField textFieldDotExecutable;
    private JCheckBox plantUMLErrorAnnotationExperimentalCheckBox;
    private JButton browse;
    private JTextField renderDelay;
    private JTextField cacheSize;

    public PlantUmlSettingsPage() {
        browse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseForFile(textFieldDotExecutable);
            }
        });
    }

    private void browseForFile(@NotNull final JTextField target) {
        final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor();
        descriptor.setTitle("Select path to Graphviz/DOT executable (dot.exe)");
        String text = target.getText();
        final VirtualFile toSelect = text == null || text.isEmpty() ? null
                : LocalFileSystem.getInstance().findFileByPath(text);

        VirtualFile virtualFile = FileChooser.chooseFile(descriptor, null, toSelect);
        if (virtualFile != null) {
            target.setText(virtualFile.getPath());
        }
    }

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
        return isModified(PlantUmlSettings.getInstance());
    }

    @Override
    public void apply() throws ConfigurationException {
        PlantUmlSettings instance = PlantUmlSettings.getInstance();
        getData(instance);
        instance.applyState();
    }

    @Override
    public void reset() {
        setData(PlantUmlSettings.getInstance());
    }

    @Override
    public void disposeUIResources() {
    }

    public void setData(PlantUmlSettings data) {
        textFieldDotExecutable.setText(data.getDotExecutable());
        plantUMLErrorAnnotationExperimentalCheckBox.setSelected(data.isErrorAnnotationEnabled());
        renderDelay.setText(data.getRenderDelay());
        cacheSize.setText(data.getCacheSize());
    }

    public void getData(PlantUmlSettings data) {
        data.setDotExecutable(textFieldDotExecutable.getText());
        data.setErrorAnnotationEnabled(plantUMLErrorAnnotationExperimentalCheckBox.isSelected());
        data.setRenderDelay(renderDelay.getText());
        data.setCacheSize(cacheSize.getText());
    }

    public boolean isModified(PlantUmlSettings data) {
        if (textFieldDotExecutable.getText() != null ? !textFieldDotExecutable.getText().equals(data.getDotExecutable()) : data.getDotExecutable() != null)
            return true;
        if (plantUMLErrorAnnotationExperimentalCheckBox.isSelected() != data.isErrorAnnotationEnabled()) return true;
        if (renderDelay.getText() != null ? !renderDelay.getText().equals(data.getRenderDelay()) : data.getRenderDelay() != null)
            return true;
        if (cacheSize.getText() != null ? !cacheSize.getText().equals(data.getCacheSize()) : data.getCacheSize() != null)
            return true;
        return false;
    }
}
