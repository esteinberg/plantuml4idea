package org.plantuml.idea.action;

/**
 * @author mamontov
 */
public class CreateSequenceDiagramAction extends AbstractCreateDiagramAction {

    public CreateSequenceDiagramAction() {
    }

    @Override
    protected String getDiagramName() {
        return "Sequence";
    }
}
