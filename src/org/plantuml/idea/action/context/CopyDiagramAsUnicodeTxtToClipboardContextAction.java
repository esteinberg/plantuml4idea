package org.plantuml.idea.action.context;

import com.intellij.icons.AllIcons;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.plantuml.ImageFormat;

public class CopyDiagramAsUnicodeTxtToClipboardContextAction extends CopyDiagramAsTxtToClipboardContextAction {

    public CopyDiagramAsUnicodeTxtToClipboardContextAction() {
        super("Copy diagram(s) to clipboard as Unicode ASCII", "Copy diagram(s) to clipboard as Unicode ASCII", AllIcons.FileTypes.Text);
    }

    @Override
    @NotNull
    protected ImageFormat getFormat() {
        return ImageFormat.UTXT;
    }

}
