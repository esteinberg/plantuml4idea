package org.plantuml.idea.toolwindow;

import com.intellij.openapi.diagnostic.Logger;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.rendering.*;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class AboutDialog extends JDialog {
    Logger logger = Logger.getInstance(AboutDialog.class);
    private JPanel contentPane;
    private JButton buttonOK;
    private JEditorPane aboutEditorPane;
    private PlantUmlImageLabel testDot;
    private Usage usage;

    public AboutDialog() {
        setTitle("PlantUML integration");
        setContentPane(contentPane);
        setModal(true);

        getRootPane().setDefaultButton(buttonOK);

        ok();

        about();

        testDot();
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

    public static void main(String[] args) {
        AboutDialog dialog = new AboutDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private void testDot() {
        RenderRequest renderRequest = new RenderRequest(new File(""), PlantUml.TESTDOT, PlantUml.ImageFormat.PNG, 0, 100, null, false, RenderCommand.Reason.REFRESH);
        renderRequest.setUseSettings(false);
        RenderResult result = PlantUmlRenderer.render(renderRequest, null);
        try {
            final ImageItem imageItem = result.getImageItem(0);
            if (imageItem != null) {
                testDot.setup(imageItem, 100, renderRequest);
                testDot.setOpaque(false);
            }
        } catch (Exception e) {
            logger.warn("Exception occurred rendering source = " + PlantUml.TESTDOT + ": " + e);
        }
    }

    private void createUIComponents() {
        usage = new Usage();
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
