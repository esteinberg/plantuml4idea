package org.plantuml.idea.lang.settings;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
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
    private Project project;

    public PlantUmlSettingsPage(Project project) {
        this.project = project;
        browse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                browseForFile(textFieldDotExecutable);
            }
        });
    }

    private void browseForFile(@NotNull final JTextField target) {
        final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor();
        descriptor.setTitle("Select path to Graphviz/DOT executable (dot.exe)");
        String text = target.getText();
        final VirtualFile toSelect = text == null || text.isEmpty() ? project.getBaseDir()
                : LocalFileSystem.getInstance().findFileByPath(text);

        VirtualFile virtualFile = FileChooser.chooseFile(descriptor, project, toSelect);
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
        return !textFieldDotExecutable.getText().equals(PlantUmlSettings.getInstance().getDotExecutable())
                || plantUMLErrorAnnotationExperimentalCheckBox.isSelected() == PlantUmlSettings.getInstance().isErrorAnnotationEnabled();
    }

    @Override
    public void apply() throws ConfigurationException {

        PlantUmlSettings.getInstance().setDotExecutable(textFieldDotExecutable.getText());
        PlantUmlSettings.getInstance().setErrorAnnotationEnabled(plantUMLErrorAnnotationExperimentalCheckBox.isSelected());

    }

    @Override
    public void reset() {
        textFieldDotExecutable.setText(PlantUmlSettings.getInstance().getDotExecutable());
        plantUMLErrorAnnotationExperimentalCheckBox.setSelected(PlantUmlSettings.getInstance().isErrorAnnotationEnabled());
    }

    @Override
    public void disposeUIResources() {
    }
}
