package org.plantuml.idea.action.context;

import com.intellij.icons.AllIcons;
import org.plantuml.idea.plantuml.PlantUml;

/**
 * @author Henady Zakalusky
 */
public class ExternalOpenDiagramAsPNGAction extends ExternalOpenDiagramAction {

    public ExternalOpenDiagramAsPNGAction() {
        super("Open in external editor as PNG image", AllIcons.Actions.Export, PlantUml.ImageFormat.PNG);
    }

}
