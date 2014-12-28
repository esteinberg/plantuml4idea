package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.plantuml.idea.util.UIUtils;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * @author Eugene Steinberg
 */
public class CopyDiagramToClipboardAction extends DumbAwareAction {
    

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();

        CopyPasteManager.getInstance().setContents(new Transferable() {
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{
                        DataFlavor.imageFlavor
                };
            }

            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return flavor.equals(DataFlavor.imageFlavor);
            }

            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                if (!flavor.equals(DataFlavor.imageFlavor)) {
                    throw new UnsupportedFlavorException(flavor);
                }
                PlantUmlToolWindow umlToolWindow = UIUtils.getPlantUmlToolWindow(project);
                JPanel imagesPanel = umlToolWindow.getImagesPanel();
                JLabel component = (JLabel) imagesPanel.getComponent(0);
                ImageIcon icon = (ImageIcon) component.getIcon();
                return icon.getImage();
            }
        });
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        final Project project = e.getProject();
        if (project != null) {
            PlantUmlToolWindow toolWindow = UIUtils.getPlantUmlToolWindow(project);
            if (toolWindow != null) {
                e.getPresentation().setEnabled(toolWindow.getImagesPanel().getComponentCount() == 1);
            }
        }
    }
}
