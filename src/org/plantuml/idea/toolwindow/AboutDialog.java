package org.plantuml.idea.toolwindow;

import com.intellij.openapi.diagnostic.Logger;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.rendering.PlantUmlRenderer;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderResult;
import org.plantuml.idea.util.ImageWithUrlData;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;

public class AboutDialog extends JDialog {
    Logger logger = Logger.getInstance(AboutDialog.class);
    private JPanel contentPane;
    private JButton buttonOK;
    private JEditorPane aboutEditorPane;
    private PlantUmlLabel testDot;

    public AboutDialog() {
        setContentPane(contentPane);
        setModal(true);

        getRootPane().setDefaultButton(buttonOK);

        ok();

        about();

        testDot();
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
        aboutEditorPane.setText("<html><body> <p> PlantUML for Idea plugin</p><p>(c) Eugene Steinberg, 2012</p><p><a href=\"https://github.com/esteinberg/plantuml4idea\">PlantUML4idea on GitHub</a></p></body></html>");
        aboutEditorPane.addHyperlinkListener(
                new BrowseHyperlinkListener()
        );
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
        RenderResult result = PlantUmlRenderer.render(new RenderRequest(null, PlantUml.TESTDOT, PlantUml.ImageFormat.PNG, 0, 100, null), null);
        try {
            final ImageWithUrlData imageWithUrlData = new ImageWithUrlData(null, result.getFirstDiagramBytes(), null, null);
            if (imageWithUrlData.getImage() != null) {
                testDot.setup(imageWithUrlData, 100, result.getRenderRequest());
            }
        } catch (IOException e) {
            logger.warn("Exception occurred rendering source = " + PlantUml.TESTDOT + ": " + e);
        }
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
