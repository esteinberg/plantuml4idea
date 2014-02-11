package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.plantuml.PlantUmlResult;
import org.plantuml.idea.util.UIUtils;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author Eugene Steinberg
 */
public class CopyDiagramToClipboardAction extends AnAction {
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
                PlantUmlResult result = PlantUml.render(
                        UIUtils.getSelectedSourceWithCaret(project),
                        UIUtils.getSelectedDir(project), 0);

                return UIUtils.getBufferedImage(result.getDiagramBytes(), 100);
            }
        });
    }
}
