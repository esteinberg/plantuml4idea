package org.plantuml.idea.action.context;

import com.intellij.icons.AllIcons;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.plantuml.ImageFormat;

public class CopyDiagramAsTikzCodeToClipboardContextAction extends CopyDiagramAsTxtToClipboardContextAction {

    public CopyDiagramAsTikzCodeToClipboardContextAction() {
        super("Copy diagram(s) to clipboard as TikZ", "Copy diagram(s) to clipboard as TikZ", AllIcons.FileTypes.Json);
    }

    @Override
    @NotNull
    protected ImageFormat getFormat() {
        return ImageFormat.TIKZ;
    }

}
