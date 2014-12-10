package org.plantuml.idea.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbAwareAction;

import javax.swing.*;
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

                JLabel data = (JLabel) e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
                ImageIcon icon = (ImageIcon) data.getIcon();
                return icon.getImage();
            }
        });
    }

}
