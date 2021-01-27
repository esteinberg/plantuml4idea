package org.plantuml.idea.action.context;

import com.intellij.icons.AllIcons;
import org.plantuml.idea.plantuml.ImageFormat;

/**
 * @author Henady Zakalusky
 */
public class ExternalOpenDiagramAsSVGAction extends ExternalOpenDiagramAction {

    public ExternalOpenDiagramAsSVGAction() {
        super("Open in external editor as SVG document", AllIcons.ToolbarDecorator.Export, ImageFormat.SVG);
    }

}
