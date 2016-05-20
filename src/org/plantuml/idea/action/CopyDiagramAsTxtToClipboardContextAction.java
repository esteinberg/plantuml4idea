package org.plantuml.idea.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.vfs.CharsetToolkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.plantuml.PlantUmlResult;
import org.plantuml.idea.plantuml.RenderRequest;
import org.plantuml.idea.toolwindow.PlantUmlLabel;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * @author Eugene Steinberg
 */
public class CopyDiagramAsTxtToClipboardContextAction extends DumbAwareAction {

    private static final DataFlavor FLAVOR = DataFlavor.stringFlavor;

    public CopyDiagramAsTxtToClipboardContextAction() {
        super("Copy diagram(s) to clipboard as ASII", "Copy diagram(s) to clipboard as ASII", AllIcons.FileTypes.Text);
    }

    public CopyDiagramAsTxtToClipboardContextAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    @Override
    public void actionPerformed(final AnActionEvent e) {
        CopyPasteManager.getInstance().setContents(new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{
                        FLAVOR
                };
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return flavor.equals(FLAVOR);
            }

            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                if (!flavor.equals(FLAVOR)) {
                    throw new UnsupportedFlavorException(flavor);
                }

                PlantUmlLabel data = (PlantUmlLabel) e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
                if (data != null) {
                    RenderRequest renderRequest = data.getRenderRequest();
                    renderRequest.setFormat(getFormat());
                    PlantUmlResult render = renderRequest.render();

                    return new String(render.getFirstDiagramBytes(), CharsetToolkit.UTF8);
                }
                return null;
            }
        });
    }

    @NotNull
    protected PlantUml.ImageFormat getFormat() {
        return PlantUml.ImageFormat.ATXT;
    }

}
