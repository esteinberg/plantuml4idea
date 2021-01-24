package org.plantuml.idea.toolwindow.image;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbAwareAction;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CopySvgToClipboard extends DumbAwareAction {
    public CopySvgToClipboard() {
        super("Copy SVG Source to Clipboard", null, null);

    }

    @Override
    public void actionPerformed(final AnActionEvent e) {
        Component data1 = e.getData(ImageContainer.CONTEXT_COMPONENT);
        ImageContainerSvg data = (ImageContainerSvg) data1;
        if (data == null) {
            return;
        }
        byte[] imageBytes = data.getImageWithData().getImageBytes();
        String s = new String(imageBytes, StandardCharsets.UTF_8);

        CopyPasteManager.getInstance().setContents(new Transferable() {

            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{
                        DataFlavor.stringFlavor
                };
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return flavor.equals(DataFlavor.stringFlavor);
            }

            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                if (!flavor.equals(DataFlavor.stringFlavor)) {
                    throw new UnsupportedFlavorException(flavor);
                }

                return s;
            }
        });
    }
}
