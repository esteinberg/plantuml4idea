package org.plantuml.idea.lang.settings;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTextArea;
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
    private JCheckBox renderUrlLinks;
    private JCheckBox usePreferentiallyGRAPHIZ_DOT;
    private JTextField encoding;
    private JBTextArea config;
    private JTextArea configExample;

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

    private void createUIComponents() {
        configExample = new ConfigExample();
    }

    public void setData(PlantUmlSettings data) {
        textFieldDotExecutable.setText(data.getDotExecutable());
        renderDelay.setText(data.getRenderDelay());
        cacheSize.setText(data.getCacheSize());
        usePreferentiallyGRAPHIZ_DOT.setSelected(data.isUsePreferentiallyGRAPHIZ_DOT());
        renderUrlLinks.setSelected(data.isRenderUrlLinks());
        plantUMLErrorAnnotationExperimentalCheckBox.setSelected(data.isErrorAnnotationEnabled());
        encoding.setText(data.getEncoding());
        config.setText(data.getConfig());
    }

    public void getData(PlantUmlSettings data) {
        data.setDotExecutable(textFieldDotExecutable.getText());
        data.setRenderDelay(renderDelay.getText());
        data.setCacheSize(cacheSize.getText());
        data.setUsePreferentiallyGRAPHIZ_DOT(usePreferentiallyGRAPHIZ_DOT.isSelected());
        data.setRenderUrlLinks(renderUrlLinks.isSelected());
        data.setErrorAnnotationEnabled(plantUMLErrorAnnotationExperimentalCheckBox.isSelected());
        data.setEncoding(encoding.getText());
        data.setConfig(config.getText());
    }

    public boolean isModified(PlantUmlSettings data) {
        if (textFieldDotExecutable.getText() != null ? !textFieldDotExecutable.getText().equals(data.getDotExecutable()) : data.getDotExecutable() != null)
            return true;
        if (renderDelay.getText() != null ? !renderDelay.getText().equals(data.getRenderDelay()) : data.getRenderDelay() != null)
            return true;
        if (cacheSize.getText() != null ? !cacheSize.getText().equals(data.getCacheSize()) : data.getCacheSize() != null)
            return true;
        if (usePreferentiallyGRAPHIZ_DOT.isSelected() != data.isUsePreferentiallyGRAPHIZ_DOT()) return true;
        if (renderUrlLinks.isSelected() != data.isRenderUrlLinks()) return true;
        if (plantUMLErrorAnnotationExperimentalCheckBox.isSelected() != data.isErrorAnnotationEnabled()) return true;
        if (encoding.getText() != null ? !encoding.getText().equals(data.getEncoding()) : data.getEncoding() != null)
            return true;
        if (config.getText() != null ? !config.getText().equals(data.getConfig()) : data.getConfig() != null)
            return true;
        return false;
    }
}
