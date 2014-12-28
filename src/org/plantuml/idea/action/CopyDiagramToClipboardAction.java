package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.plantuml.PlantUmlResult;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.plantuml.idea.util.UIUtils;

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
                PlantUmlResult result = PlantUml.render(UIUtils.getSelectedSourceWithCaret(project),
                        UIUtils.getSelectedDir(project), umlToolWindow.getPage(), umlToolWindow.getZoom());
                return UIUtils.getBufferedImage(result.getFirstDiagramBytes());
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
                e.getPresentation().setEnabled(toolWindow.getNumPages() == 1 || toolWindow.getPage() != -1);
            }
        }
    }
}
