package org.plantuml.idea.action;

import com.intellij.icons.AllIcons;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.plantuml.PlantUml;

/**
 * @author Eugene Steinberg
 */
public class CopyDiagramAsUnicodeTxtToClipboardContextAction extends CopyDiagramAsTxtToClipboardContextAction {

    public CopyDiagramAsUnicodeTxtToClipboardContextAction() {
        super("Copy diagram(s) to clipboard as Unicode ASII", "Copy diagram(s) to clipboard as Unicode ASII", AllIcons.FileTypes.Text);
    }

    @Override
    @NotNull
    protected PlantUml.ImageFormat getFormat() {
        return PlantUml.ImageFormat.UTXT;
    }

}
