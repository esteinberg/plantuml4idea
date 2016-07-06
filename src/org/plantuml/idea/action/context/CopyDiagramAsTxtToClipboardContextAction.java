package org.plantuml.idea.action.context;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.util.ui.TextTransferable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.rendering.PlantUmlRenderer;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderResult;
import org.plantuml.idea.toolwindow.PlantUmlImageLabel;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.io.UnsupportedEncodingException;

public class CopyDiagramAsTxtToClipboardContextAction extends DumbAwareAction {

    private static final DataFlavor FLAVOR = DataFlavor.stringFlavor;

    public CopyDiagramAsTxtToClipboardContextAction() {
        super("Copy diagram(s) to clipboard as ASCII", "Copy diagram(s) to clipboard as ASCII", AllIcons.FileTypes.Text);
    }

    public CopyDiagramAsTxtToClipboardContextAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    @Override
    public void actionPerformed(final AnActionEvent e) {
        PlantUmlImageLabel data = (PlantUmlImageLabel) e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
        if (data != null) {
            RenderRequest renderRequest = data.getRenderRequest();
            RenderResult render = PlantUmlRenderer.render(new RenderRequest(renderRequest, getFormat()), null);

            try {
                byte[] firstDiagramBytes = render.getFirstDiagramBytes();
                if (firstDiagramBytes != null) {
                    CopyPasteManager.getInstance().setContents(new TextTransferable(new String(firstDiagramBytes, CharsetToolkit.UTF8)));
                }
            } catch (UnsupportedEncodingException e1) {
            }
        }
    }

    @NotNull
    protected PlantUml.ImageFormat getFormat() {
        return PlantUml.ImageFormat.ATXT;
    }

}
