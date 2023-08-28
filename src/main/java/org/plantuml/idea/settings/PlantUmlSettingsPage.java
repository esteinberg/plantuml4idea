package org.plantuml.idea.settings;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredSideBorder;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.external.Classloaders;
import org.plantuml.idea.plantuml.ImageFormat;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Max Gorbunov
 */
public class PlantUmlSettingsPage implements Configurable {
    private static final Logger LOG = Logger.getInstance(PlantUmlSettingsPage.class);
    public static final Icon COINS = IconLoader.getIcon("/images/coins_in_hand.png", PlantUmlSettingsPage.class);

    public static final ColoredSideBorder ERROR_BORDER = new ColoredSideBorder(
            JBColor.RED, JBColor.RED, JBColor.RED, JBColor.RED, 1);

    public static final ColoredSideBorder VALID_BORDER = new ColoredSideBorder(
            JBColor.GREEN, JBColor.GREEN, JBColor.GREEN, JBColor.GREEN, 1);

    private final Border defaultBorder;

    private JPanel panel;
    private JTextField textFieldDotExecutable;
    private JCheckBox syntaxCheck;
    private JButton textFieldDotExecutableBrowse;
    private JTextField renderDelay;
    private JTextField cacheSize;
    private JCheckBox renderLinksPng;
    private JCheckBox usePreferentiallyGRAPHIZ_DOT;
    private JTextField encoding;
    private JTextArea config;
    private JTextArea configExample;
    private JCheckBox showUrlLinksBorder;
    private JTextField PLANTUML_LIMIT_SIZE;
    private JTextArea includePaths;
    private JTextField customPlantumlJar;
    private JButton browseCustomPlantumlJar;
    private JRadioButton bundledPlantUMLRadioButton;
    private JCheckBox switchToBundledAfterUpdate;
    private JRadioButton customPlantUMLRadioButton;
    private JLabel version;
    private JButton web;
    private JCheckBox usePageTitles;
    private JCheckBox grammarSupport;
    private JCheckBox keywordHighlighting;
    private JCheckBox insertPair;
    private JCheckBox linkOpensSearchBar;
    private JCheckBox displaySvg;
    private JCheckBox highlightInImages;
    private JTextField maxSvgSize;
    private JCheckBox svgPreviewScaling;
    private JButton donate;
    private JLabel svgPreviewLimitLabel;
    private JComboBox<String> defaultFileExtension;
    private JTextField serverUrl;
    private JCheckBox remoteRendering;
    private JCheckBox useProxy;
    private JButton reset;
    private JTextField clipboardLinkType;
    private JLabel includePathsL1;
    private JLabel includePathsL2;
    private JLabel includePathsL3;
    private JLabel configL1;
    private JLabel configL2;
    private JLabel configL3;
    private JLabel plantumlLimitSizeLabel;
    private JCheckBox rememberLastExportDir;
    private JLabel textFieldDotExecutableL;
    private JCheckBox scaleExport;
    private JCheckBox generateMetadata;
    private JCheckBox remoteRenderingSinglePage;

    public PlantUmlSettingsPage() {
        ArrayList<String> list = new ArrayList<String>();
        ImageFormat[] values = ImageFormat.values();
        for (ImageFormat value : values) {
            list.add(value.name());
        }
        defaultFileExtension.setModel(new ListComboBoxModel<>(list));

        donate.setIcon(COINS);
        donate.addActionListener(e -> {
            BrowserUtil.browse("https://www.paypal.com/donate/?business=75YN7U7H7D7XU&item_name=PlantUML+integration+-+IntelliJ+plugin&currency_code=EUR");
        });
        reset.addActionListener(e -> {
            serverUrl.setText(PlantUmlSettings.DEFAULT_SERVER);
        });
        textFieldDotExecutableBrowse.addActionListener(e -> browseForFile(textFieldDotExecutable));
        browseCustomPlantumlJar.addActionListener(e -> {
            browseForjarOrFolder(customPlantumlJar);
        });
        customPlantumlJar.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent documentEvent) {
                customPlantUMLRadioButton.setSelected(true);
            }
        });
        web.addActionListener(e -> {
            BrowserUtil.browse("https://plantuml.com/news");
        });

        try {
            version.setText("v" + Classloaders.getBundledVersion());
        } catch (Throwable throwable) {
            LOG.error(throwable);
        }

        addListeners();
        defaultBorder = textFieldDotExecutable.getBorder();
    }

    private void addListeners() {
        for (Field field : PlantUmlSettingsPage.class.getDeclaredFields()) {
            try {
                Object o = field.get(this);
                if (o instanceof JToggleButton) {
                    JToggleButton button = (JToggleButton) o;
                    button.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            updateComponents();
                        }
                    });
                }
                if (o instanceof JTextField) {
                    JTextField jTextField = (JTextField) o;
                    jTextField.getDocument().addDocumentListener(new DocumentAdapter() {
                        @Override
                        protected void textChanged(DocumentEvent e) {
                            updateComponents();
                        }
                    });
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void updateComponents() {
        DialogUtils.disableByAny(new JComponent[]{renderLinksPng}, displaySvg);
        DialogUtils.disableByAny(new JComponent[]{syntaxCheck, textFieldDotExecutableL, textFieldDotExecutableBrowse, plantumlLimitSizeLabel, configL1, configL2, configL3, configExample, includePathsL1, includePathsL2, includePathsL3, includePaths, config, PLANTUML_LIMIT_SIZE, usePreferentiallyGRAPHIZ_DOT, textFieldDotExecutable,}, remoteRendering);
        DialogUtils.enabledByAny(new JComponent[]{useProxy}, remoteRendering);
        DialogUtils.enabledByAny(new JComponent[]{svgPreviewScaling, svgPreviewLimitLabel, maxSvgSize}, displaySvg);
        DialogUtils.enabledByAny(new JComponent[]{highlightInImages, linkOpensSearchBar, showUrlLinksBorder}, displaySvg, renderLinksPng);
        validatePath(customPlantumlJar);
        validatePath(textFieldDotExecutable);
    }

    private void browseForjarOrFolder(@NotNull final JTextField target) {
        final FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, true, true, false, false);

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
        if (!Objects.equals(settings.getDefaultExportFileFormat(), defaultFileExtension.getSelectedItem())) {
            return true;
        }

        return isModified(settings);
    }

    @Override
    public void apply() throws ConfigurationException {
        PlantUmlSettings settings = PlantUmlSettings.getInstance();
        getData(settings);

        settings.setUseBundled(bundledPlantUMLRadioButton.isSelected());
        settings.setDefaultExportFileFormat((String) defaultFileExtension.getSelectedItem());
        ApplicationManager.getApplication().getMessageBus().syncPublisher(PlantUmlSettings.SettingsChangedListener.TOPIC).onSettingsChange(settings);
    }

    @Override
    public void reset() {
        PlantUmlSettings settings = PlantUmlSettings.getInstance();

        setData(settings);
        defaultFileExtension.setSelectedItem(settings.getDefaultExportFileFormat());
        bundledPlantUMLRadioButton.setSelected(settings.isUseBundled());
        customPlantUMLRadioButton.setSelected(!settings.isUseBundled());

        updateComponents();
    }

    @Override
    public void disposeUIResources() {
    }

    private void createUIComponents() {
        configExample = new ConfigExample();
    }

    public void setData(PlantUmlSettings data) {
        textFieldDotExecutable.setText(data.getDotExecutable());
        usePreferentiallyGRAPHIZ_DOT.setSelected(data.isUsePreferentiallyGRAPHIZ_DOT());
        config.setText(data.getConfig());
        includePaths.setText(data.getIncludedPaths());
        switchToBundledAfterUpdate.setSelected(data.isSwitchToBundledAfterUpdate());
        customPlantumlJar.setText(data.getCustomPlantumlJarPath());
        renderDelay.setText(data.getRenderDelay());
        cacheSize.setText(data.getCacheSize());
        PLANTUML_LIMIT_SIZE.setText(data.getPLANTUML_LIMIT_SIZE());
        maxSvgSize.setText(data.getMaxSvgSize());
        encoding.setText(data.getEncoding());
        generateMetadata.setSelected(data.isGenerateMetadata());
        usePageTitles.setSelected(data.isUsePageTitles());
        grammarSupport.setSelected(data.isUseGrammar());
        keywordHighlighting.setSelected(data.isKeywordHighlighting());
        displaySvg.setSelected(data.isDisplaySvg());
        syntaxCheck.setSelected(data.isErrorAnnotationEnabled());
        svgPreviewScaling.setSelected(data.isSvgPreviewScaling());
        renderLinksPng.setSelected(data.isRenderLinks());
        linkOpensSearchBar.setSelected(data.isLinkOpensSearchBar());
        highlightInImages.setSelected(data.isHighlightInImages());
        showUrlLinksBorder.setSelected(data.isShowUrlLinksBorder());
        insertPair.setSelected(data.isInsertPair());
        scaleExport.setSelected(data.isScaleExport());
        rememberLastExportDir.setSelected(data.isRememberLastExportDir());
        serverUrl.setText(data.getServerPrefix());
        remoteRendering.setSelected(data.isRemoteRendering());
        useProxy.setSelected(data.isUseProxy());
        clipboardLinkType.setText(data.getServerClipboardLinkType());
        remoteRenderingSinglePage.setSelected(data.isRemoteRenderingSinglePage());
    }

    public void getData(PlantUmlSettings data) {
        data.setDotExecutable(textFieldDotExecutable.getText());
        data.setUsePreferentiallyGRAPHIZ_DOT(usePreferentiallyGRAPHIZ_DOT.isSelected());
        data.setConfig(config.getText());
        data.setIncludedPaths(includePaths.getText());
        data.setSwitchToBundledAfterUpdate(switchToBundledAfterUpdate.isSelected());
        data.setCustomPlantumlJarPath(customPlantumlJar.getText());
        data.setRenderDelay(renderDelay.getText());
        data.setCacheSize(cacheSize.getText());
        data.setPLANTUML_LIMIT_SIZE(PLANTUML_LIMIT_SIZE.getText());
        data.setMaxSvgSize(maxSvgSize.getText());
        data.setEncoding(encoding.getText());
        data.setGenerateMetadata(generateMetadata.isSelected());
        data.setUsePageTitles(usePageTitles.isSelected());
        data.setUseGrammar(grammarSupport.isSelected());
        data.setKeywordHighlighting(keywordHighlighting.isSelected());
        data.setDisplaySvg(displaySvg.isSelected());
        data.setErrorAnnotationEnabled(syntaxCheck.isSelected());
        data.setSvgPreviewScaling(svgPreviewScaling.isSelected());
        data.setRenderLinks(renderLinksPng.isSelected());
        data.setLinkOpensSearchBar(linkOpensSearchBar.isSelected());
        data.setHighlightInImages(highlightInImages.isSelected());
        data.setShowUrlLinksBorder(showUrlLinksBorder.isSelected());
        data.setInsertPair(insertPair.isSelected());
        data.setScaleExport(scaleExport.isSelected());
        data.setRememberLastExportDir(rememberLastExportDir.isSelected());
        data.setServerPrefix(serverUrl.getText());
        data.setRemoteRendering(remoteRendering.isSelected());
        data.setUseProxy(useProxy.isSelected());
        data.setServerClipboardLinkType(clipboardLinkType.getText());
        data.setRemoteRenderingSinglePage(remoteRenderingSinglePage.isSelected());
    }

    public boolean isModified(PlantUmlSettings data) {
        if (textFieldDotExecutable.getText() != null ? !textFieldDotExecutable.getText().equals(data.getDotExecutable()) : data.getDotExecutable() != null)
            return true;
        if (usePreferentiallyGRAPHIZ_DOT.isSelected() != data.isUsePreferentiallyGRAPHIZ_DOT()) return true;
        if (config.getText() != null ? !config.getText().equals(data.getConfig()) : data.getConfig() != null)
            return true;
        if (includePaths.getText() != null ? !includePaths.getText().equals(data.getIncludedPaths()) : data.getIncludedPaths() != null)
            return true;
        if (switchToBundledAfterUpdate.isSelected() != data.isSwitchToBundledAfterUpdate()) return true;
        if (customPlantumlJar.getText() != null ? !customPlantumlJar.getText().equals(data.getCustomPlantumlJarPath()) : data.getCustomPlantumlJarPath() != null)
            return true;
        if (renderDelay.getText() != null ? !renderDelay.getText().equals(data.getRenderDelay()) : data.getRenderDelay() != null)
            return true;
        if (cacheSize.getText() != null ? !cacheSize.getText().equals(data.getCacheSize()) : data.getCacheSize() != null)
            return true;
        if (PLANTUML_LIMIT_SIZE.getText() != null ? !PLANTUML_LIMIT_SIZE.getText().equals(data.getPLANTUML_LIMIT_SIZE()) : data.getPLANTUML_LIMIT_SIZE() != null)
            return true;
        if (maxSvgSize.getText() != null ? !maxSvgSize.getText().equals(data.getMaxSvgSize()) : data.getMaxSvgSize() != null)
            return true;
        if (encoding.getText() != null ? !encoding.getText().equals(data.getEncoding()) : data.getEncoding() != null)
            return true;
        if (generateMetadata.isSelected() != data.isGenerateMetadata()) return true;
        if (usePageTitles.isSelected() != data.isUsePageTitles()) return true;
        if (grammarSupport.isSelected() != data.isUseGrammar()) return true;
        if (keywordHighlighting.isSelected() != data.isKeywordHighlighting()) return true;
        if (displaySvg.isSelected() != data.isDisplaySvg()) return true;
        if (syntaxCheck.isSelected() != data.isErrorAnnotationEnabled()) return true;
        if (svgPreviewScaling.isSelected() != data.isSvgPreviewScaling()) return true;
        if (renderLinksPng.isSelected() != data.isRenderLinks()) return true;
        if (linkOpensSearchBar.isSelected() != data.isLinkOpensSearchBar()) return true;
        if (highlightInImages.isSelected() != data.isHighlightInImages()) return true;
        if (showUrlLinksBorder.isSelected() != data.isShowUrlLinksBorder()) return true;
        if (insertPair.isSelected() != data.isInsertPair()) return true;
        if (scaleExport.isSelected() != data.isScaleExport()) return true;
        if (rememberLastExportDir.isSelected() != data.isRememberLastExportDir()) return true;
        if (serverUrl.getText() != null ? !serverUrl.getText().equals(data.getServerPrefix()) : data.getServerPrefix() != null)
            return true;
        if (remoteRendering.isSelected() != data.isRemoteRendering()) return true;
        if (useProxy.isSelected() != data.isUseProxy()) return true;
        if (clipboardLinkType.getText() != null ? !clipboardLinkType.getText().equals(data.getServerClipboardLinkType()) : data.getServerClipboardLinkType() != null)
            return true;
        if (remoteRenderingSinglePage.isSelected() != data.isRemoteRenderingSinglePage()) return true;
        return false;
    }


    private void validatePath(JTextField field) {
        String text = field.getText();
        if (StringUtils.isEmpty(text)) {
            field.setBorder(defaultBorder);
            field.setToolTipText(null);
        } else if (new File(text).exists()) {
            field.setBorder(VALID_BORDER);
            field.setToolTipText("Valid path");
        } else {
            field.setBorder(ERROR_BORDER);
            field.setToolTipText("Invalid path");
        }
    }

}

