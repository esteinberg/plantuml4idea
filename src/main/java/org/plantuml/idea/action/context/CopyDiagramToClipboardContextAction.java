package org.plantuml.idea.action.context;

import com.intellij.icons.AllIcons;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.action.MyDumbAwareAction;
import org.plantuml.idea.preview.image.ImageContainer;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import static org.plantuml.idea.util.UIUtils.notification;

/**
 * @author Eugene Steinberg
 */
public class CopyDiagramToClipboardContextAction extends MyDumbAwareAction {

    public CopyDiagramToClipboardContextAction() {
        super("Copy Diagram to Clipboard", null, AllIcons.Actions.Copy);
    }

    @Override
    public void actionPerformed(final AnActionEvent e) {
        Component data1 = e.getData(ImageContainer.CONTEXT_COMPONENT);
        ImageContainer data = (ImageContainer) data1;
        if (data == null) {
            return;
        }
        final Image image = data.getPngImage(e);
        if (image == null) {
            Notifications.Bus.notify(notification().createNotification("Failed to copy image", MessageType.WARNING));
            return;
        }
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
        Project project = e.getProject();
        if (project != null) {
            WindowManager.getInstance().getStatusBar(project).setInfo("Image copied to clipboard");
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (project != null) {
            ImageContainer data = (ImageContainer) e.getData(ImageContainer.CONTEXT_COMPONENT);
            boolean enabled = data != null && data.isPngAvailable();
            e.getPresentation().setEnabled(enabled);
        }
    }

}
