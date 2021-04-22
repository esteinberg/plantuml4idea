package org.plantuml.idea;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.plantuml.idea.external.PlantUmlFacade;
import org.plantuml.idea.plantuml.ImageFormat;
import org.plantuml.idea.plantuml.SourceExtractor;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;
import org.plantuml.idea.preview.Zoom;
import org.plantuml.idea.preview.image.ImageContainerPng;
import org.plantuml.idea.rendering.ImageItem;
import org.plantuml.idea.rendering.RenderCommand;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderResult;
import org.plantuml.idea.settings.PlantUmlSettings;
import org.plantuml.idea.util.UIUtils;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URISyntaxException;

public class AboutDialog extends JDialog {
    private final Project project;
    Logger logger = Logger.getInstance(AboutDialog.class);
    private JPanel contentPane;
    private JButton buttonOK;
    private JEditorPane aboutEditorPane;
    private ImageContainerPng testDot;
    private Usage usage;
    private JTextArea debugPane;

    public AboutDialog(AnActionEvent e, Project project) {
        this.project = project;
        setTitle("PlantUML integration");
        setContentPane(contentPane);
        setModal(true);

        getRootPane().setDefaultButton(buttonOK);

        ok();

        about();

        testDot(e);

        try {
            StringBuilder debug = new StringBuilder("Debug Info:");
            debugPane.setOpaque(false);
            debugPane.setEditable(false);
            TransformerFactory xformFactory = TransformerFactory.newInstance();
            debug.append("\nTransformerFactory=").append(xformFactory.getClass());
            debug.append("\nTransformerFactoryClassLoader=").append(xformFactory.getClass().getClassLoader());
            Transformer transformer = xformFactory.newTransformer();
            debug.append("\nTransformer=").append(transformer.getClass());
            debug.append("\nTransformerClassLoader=").append(transformer.getClass().getClassLoader());
            debug.append("\nTransformerOutputProperties=").append(transformer.getOutputProperties());
            debug.append("\njavax.xml.transform.TransformerFactory=").append(System.getProperty("javax.xml.transform.TransformerFactory"));
            debugPane.setText(debug.toString());
        } catch (Throwable t) {
            logger.warn(t);
            debugPane.setText(t.toString());
        }

        usage.setText(Usage.TEXT);


        getRootPane().registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AboutDialog.this.dispose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void ok() {
        buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
    }

    private void about() {
        aboutEditorPane.setText("<html><body>PlantUML for Idea plugin<br/>(c) Eugene Steinberg, 2012<br/><a href=\"https://github.com/esteinberg/plantuml4idea\">PlantUML4idea on GitHub</a><br/></body></html>");
        aboutEditorPane.addHyperlinkListener(
                new BrowseHyperlinkListener()
        );
        aboutEditorPane.setOpaque(false);
        StyledDocument doc = (StyledDocument) aboutEditorPane.getDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
    }

    private void onOK() {
// add your code here
        dispose();
    }

    private void testDot(AnActionEvent e) {
        PlantUmlPreviewPanel previewPanel = UIUtils.getEditorOrToolWindowPreview(e);
        Zoom zoom = new Zoom(previewPanel, 100, PlantUmlSettings.getInstance());
        RenderRequest renderRequest = new RenderRequest("", SourceExtractor.TESTDOT, ImageFormat.PNG, 0, zoom, null, false, RenderCommand.Reason.REFRESH);
        renderRequest.setUseSettings(false);
        RenderResult result = PlantUmlFacade.get().render(renderRequest, null);
        try {
            final ImageItem imageItem = result.getImageItem(0);
            if (imageItem != null) {
                testDot.init(previewPanel, e.getProject(), contentPane, imageItem, 0, renderRequest, result);
                testDot.setOpaque(false);
            }
        } catch (Throwable ex) {
            logger.error("Exception occurred rendering source = " + SourceExtractor.TESTDOT, ex);
        }
    }

    private void createUIComponents() {
        usage = new Usage();
        testDot = new ImageContainerPng();
    }

    private class BrowseHyperlinkListener implements HyperlinkListener {
        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (IOException e1) {
                        logger.warn("Exception browsing to " + e.getURL().toExternalForm() + " : " + e1);
                    } catch (URISyntaxException e1) {
                        logger.warn("Incorrect URI syntax " + e.getURL().toExternalForm() + " : " + e1);
                    }
                }
            }

        }
    }
}
