package org.plantuml.idea.action.context;

import com.intellij.icons.AllIcons;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.plantuml.PlantUml;

public class CopyDiagramAsLatexStandaloneToClipboardContextAction extends CopyDiagramAsTxtToClipboardContextAction {

    public CopyDiagramAsLatexStandaloneToClipboardContextAction() {
        super("Copy diagram(s) to clipboard as Standalone LaTeX", "Copy diagram(s) to clipboard as Standalone LaTeX", AllIcons.FileTypes.Json);
    }

    @Override
    @NotNull
    protected PlantUml.ImageFormat getFormat() {
        return PlantUml.ImageFormat.LATEX_STANDALONE;
    }

}
