package org.plantuml.idea.action.save;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;
import org.plantuml.idea.preview.image.ImageContainer;
import org.plantuml.idea.util.UIUtils;

import javax.swing.*;
import java.awt.*;
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

        PlantUmlPreviewPanel previewPanel = UIUtils.getEditorOrToolWindowPreview(e);
        JPanel imagesPanel = previewPanel.getImagesPanel();
        ImageContainer component = (ImageContainer) imagesPanel.getComponent(0);
        final Image image = component.getPngImage(e);

        CopyPasteManager.getInstance().setContents(new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{
                        DataFlavor.imageFlavor
                };
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return flavor.equals(DataFlavor.imageFlavor);
            }

            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                if (!flavor.equals(DataFlavor.imageFlavor)) {
                    throw new UnsupportedFlavorException(flavor);
                }
                return image;
            }
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (project != null) {
            PlantUmlPreviewPanel previewPanel = UIUtils.getEditorOrToolWindowPreview(e);
            if (previewPanel != null) {
                int selectedPage = previewPanel.getSelectedPage();
                JPanel imagesPanel = previewPanel.getImagesPanel();
                int componentCount = imagesPanel.getComponentCount();
                if (componentCount > 0) {
                    Component component1 = imagesPanel.getComponent(0);
                    if (component1 instanceof ImageContainer) {
                        ImageContainer component = (ImageContainer) component1;
                        boolean pngAvailable = component.isPngAvailable();
                        boolean singlePage = previewPanel.getNumPages() == 1 || (previewPanel.getNumPages() > 1 && selectedPage != -1);
                        e.getPresentation().setEnabled(pngAvailable && singlePage);
                    }
                }
            } else {
                e.getPresentation().setEnabled(false);
            }
        }
    }
}
