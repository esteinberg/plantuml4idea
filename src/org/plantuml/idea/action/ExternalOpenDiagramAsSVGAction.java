package org.plantuml.idea.action;

import com.intellij.icons.AllIcons;
import org.plantuml.idea.plantuml.PlantUml;

/**
 * @author Henady Zakalusky
 */
public class ExternalOpenDiagramAsSVGAction extends ExternalOpenDiagramAction {

    public ExternalOpenDiagramAsSVGAction() {
        super("Open in external editor as SVG document", AllIcons.Actions.Export, PlantUml.ImageFormat.SVG);
    }

}
