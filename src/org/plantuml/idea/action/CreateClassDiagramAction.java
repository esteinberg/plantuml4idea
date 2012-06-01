package org.plantuml.idea.action;

/**
 * @author mamontov
 */
public class CreateClassDiagramAction extends AbstractCreateDiagramAction {

    @Override
    protected String getDiagramName() {
            return "Class";
    }
}
