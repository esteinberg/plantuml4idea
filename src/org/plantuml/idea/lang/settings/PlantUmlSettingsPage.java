package org.plantuml.idea.lang.settings;

import com.intellij.openapi.diagnostic.Logger;
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
import org.plantuml.idea.external.PlantUmlFacade;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

/**
 * @author Max Gorbunov
 */
public class PlantUmlSettingsPage implements Configurable {
    private static final Logger LOG = Logger.getInstance(PlantUmlSettingsPage.class);

    private JPanel panel;
    private JTextField textFieldDotExecutable;
    private JCheckBox plantUMLErrorAnnotationExperimentalCheckBox;
    private JButton browse;
    private JTextField renderDelay;
    private JTextField cacheSize;
    private JCheckBox renderUrlLinks;
    private JCheckBox usePreferentiallyGRAPHIZ_DOT;
    private JTextField encoding;
    private JTextArea config;
    private JTextArea configExample;
    private JCheckBox showUrlLinksBorder;
    private JTextField PLANTUML_LIMIT_SIZE;
    private JTextArea includePaths;
    private JTextField customPlantumlJar;
    private JButton browseCustomPlantumlJar;
    private JLabel plantumlJarLabel;
    private JRadioButton bundledPlantUMLRadioButton;
    private JCheckBox switchToBundledAfterUpdate;
    private JRadioButton customPlantUMLRadioButton;
    private JLabel version;
    private JButton web;
    private JCheckBox usePageTitles;

    public PlantUmlSettingsPage() {
        browse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseForFile(textFieldDotExecutable);
            }
        });
        browseCustomPlantumlJar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseForjar(customPlantumlJar);
            }
        });
        web.setVisible(Desktop.isDesktopSupported());
        web.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI("https://plantuml.com/news"));
            } catch (Exception ee) {
                throw new RuntimeException(ee);
            }
        });

        try {
            version.setText("v" + PlantUmlFacade.getBundled().version());
        } catch (Throwable throwable) {
            LOG.error(throwable);
        }
    }

    private void browseForjar(@NotNull final JTextField target) {
        final FileChooserDescriptor descriptor = new FileChooserDescriptor(false, false, true, true, false, false);

        descriptor.setTitle("Select path to plantuml.jar");
        String text = target.getText();
        final VirtualFile toSelect = text == null || text.isEmpty() ? null
                : LocalFileSystem.getInstance().findFileByPath(text);

        VirtualFile virtualFile = FileChooser.chooseFile(descriptor, null, toSelect);
        if (virtualFile != null) {
            target.setText(virtualFile.getPath());
        }
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
        PlantUmlSettings settings = PlantUmlSettings.getInstance();

        if (bundledPlantUMLRadioButton.isSelected() != settings.isUseBundled()) {
            return true;
        }

        return isModified(settings);
    }

    @Override
    public void apply() throws ConfigurationException {
        PlantUmlSettings settings = PlantUmlSettings.getInstance();
        getData(settings);

        settings.setUseBundled(bundledPlantUMLRadioButton.isSelected());

        settings.applyState();
    }

    @Override
    public void reset() {
        PlantUmlSettings settings = PlantUmlSettings.getInstance();

        setData(settings);

        bundledPlantUMLRadioButton.setSelected(settings.isUseBundled());
        customPlantUMLRadioButton.setSelected(!settings.isUseBundled());
    }

    @Override
    public void disposeUIResources() {
    }

    private void createUIComponents() {
        configExample = new ConfigExample();
    }

    public void setData(PlantUmlSettings data) {
        renderDelay.setText(data.getRenderDelay());
        cacheSize.setText(data.getCacheSize());
        encoding.setText(data.getEncoding());
        renderUrlLinks.setSelected(data.isRenderUrlLinks());
        plantUMLErrorAnnotationExperimentalCheckBox.setSelected(data.isErrorAnnotationEnabled());
        textFieldDotExecutable.setText(data.getDotExecutable());
        usePreferentiallyGRAPHIZ_DOT.setSelected(data.isUsePreferentiallyGRAPHIZ_DOT());
        config.setText(data.getConfig());
        showUrlLinksBorder.setSelected(data.isShowUrlLinksBorder());
        PLANTUML_LIMIT_SIZE.setText(data.getPLANTUML_LIMIT_SIZE());
        includePaths.setText(data.getIncludedPaths());
        switchToBundledAfterUpdate.setSelected(data.isSwitchToBundledAfterUpdate());
        customPlantumlJar.setText(data.getCustomPlantumlJarPath());
        usePageTitles.setSelected(data.isUsePageTitles());
    }

    public void getData(PlantUmlSettings data) {
        data.setRenderDelay(renderDelay.getText());
        data.setCacheSize(cacheSize.getText());
        data.setEncoding(encoding.getText());
        data.setRenderUrlLinks(renderUrlLinks.isSelected());
        data.setErrorAnnotationEnabled(plantUMLErrorAnnotationExperimentalCheckBox.isSelected());
        data.setDotExecutable(textFieldDotExecutable.getText());
        data.setUsePreferentiallyGRAPHIZ_DOT(usePreferentiallyGRAPHIZ_DOT.isSelected());
        data.setConfig(config.getText());
        data.setShowUrlLinksBorder(showUrlLinksBorder.isSelected());
        data.setPLANTUML_LIMIT_SIZE(PLANTUML_LIMIT_SIZE.getText());
        data.setIncludedPaths(includePaths.getText());
        data.setSwitchToBundledAfterUpdate(switchToBundledAfterUpdate.isSelected());
        data.setCustomPlantumlJarPath(customPlantumlJar.getText());
        data.setUsePageTitles(usePageTitles.isSelected());
    }

    public boolean isModified(PlantUmlSettings data) {
        if (renderDelay.getText() != null ? !renderDelay.getText().equals(data.getRenderDelay()) : data.getRenderDelay() != null)
            return true;
        if (cacheSize.getText() != null ? !cacheSize.getText().equals(data.getCacheSize()) : data.getCacheSize() != null)
            return true;
        if (encoding.getText() != null ? !encoding.getText().equals(data.getEncoding()) : data.getEncoding() != null)
            return true;
        if (renderUrlLinks.isSelected() != data.isRenderUrlLinks()) return true;
        if (plantUMLErrorAnnotationExperimentalCheckBox.isSelected() != data.isErrorAnnotationEnabled()) return true;
        if (textFieldDotExecutable.getText() != null ? !textFieldDotExecutable.getText().equals(data.getDotExecutable()) : data.getDotExecutable() != null)
            return true;
        if (usePreferentiallyGRAPHIZ_DOT.isSelected() != data.isUsePreferentiallyGRAPHIZ_DOT()) return true;
        if (config.getText() != null ? !config.getText().equals(data.getConfig()) : data.getConfig() != null)
            return true;
        if (showUrlLinksBorder.isSelected() != data.isShowUrlLinksBorder()) return true;
        if (PLANTUML_LIMIT_SIZE.getText() != null ? !PLANTUML_LIMIT_SIZE.getText().equals(data.getPLANTUML_LIMIT_SIZE()) : data.getPLANTUML_LIMIT_SIZE() != null)
            return true;
        if (includePaths.getText() != null ? !includePaths.getText().equals(data.getIncludedPaths()) : data.getIncludedPaths() != null)
            return true;
        if (switchToBundledAfterUpdate.isSelected() != data.isSwitchToBundledAfterUpdate()) return true;
        if (customPlantumlJar.getText() != null ? !customPlantumlJar.getText().equals(data.getCustomPlantumlJarPath()) : data.getCustomPlantumlJarPath() != null)
            return true;
        if (usePageTitles.isSelected() != data.isUsePageTitles()) return true;
        return false;
    }
}
