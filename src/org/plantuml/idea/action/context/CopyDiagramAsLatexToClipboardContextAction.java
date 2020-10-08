package org.plantuml.idea.action.context;

import com.intellij.icons.AllIcons;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.plantuml.PlantUml;

public class CopyDiagramAsLatexToClipboardContextAction extends CopyDiagramAsTxtToClipboardContextAction {

    public CopyDiagramAsLatexToClipboardContextAction() {
        super("Copy diagram(s) to clipboard as LaTeX", "Copy diagram(s) to clipboard as LaTeX", AllIcons.FileTypes.Json);
    }

    @Override
    @NotNull
    protected PlantUml.ImageFormat getFormat() {
        return PlantUml.ImageFormat.TEX;
    }

}
