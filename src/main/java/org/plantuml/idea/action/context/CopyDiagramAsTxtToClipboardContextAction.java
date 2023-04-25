package org.plantuml.idea.action.context;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.util.ui.TextTransferable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.external.PlantUmlFacade;
import org.plantuml.idea.plantuml.ImageFormat;
import org.plantuml.idea.preview.image.ImageContainer;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderResult;
import org.plantuml.idea.rendering.RenderingType;

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
        ImageContainer data = (ImageContainer) e.getData(ImageContainer.CONTEXT_COMPONENT);
        if (data != null) {
            RenderRequest renderRequest = data.getRenderRequest();
            RenderResult render = PlantUmlFacade.get().render(new RenderRequest(renderRequest, getFormat()), null);

            try {
                byte[] firstDiagramBytes = render.getFirstDiagramBytes();
                if (firstDiagramBytes != null) {
                    CopyPasteManager.getInstance().setContents(new TextTransferable(new String(firstDiagramBytes, CharsetToolkit.UTF8)));
                } else {
                    throw new IllegalStateException("Nothing rendered.");
                }
            } catch (UnsupportedEncodingException e1) {
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (project != null) {
            ImageContainer data = (ImageContainer) e.getData(ImageContainer.CONTEXT_COMPONENT);
            e.getPresentation().setEnabled(data != null && data.getImageItem().getRenderingType() != RenderingType.REMOTE);
        }
    }

    @NotNull
    protected ImageFormat getFormat() {
        return ImageFormat.ATXT;
    }

}
