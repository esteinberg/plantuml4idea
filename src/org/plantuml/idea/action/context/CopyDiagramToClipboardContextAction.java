package org.plantuml.idea.action.context;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbAwareAction;
import org.plantuml.idea.toolwindow.image.ImageContainerPng;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * @author Eugene Steinberg
 */
public class CopyDiagramToClipboardContextAction extends DumbAwareAction {

    public CopyDiagramToClipboardContextAction() {
        super("Copy current diagram to clipboard", "Copy current diagram to clipboard", AllIcons.Actions.Copy);
    }

    @Override
    public void actionPerformed(final AnActionEvent e) {
        ImageContainerPng data = (ImageContainerPng) e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
        if (data == null) {
            return;
        }
        final Image image = data.getOriginalImage();
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

}
