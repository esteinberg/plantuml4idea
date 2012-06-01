package org.plantuml.idea.action;

/**
 * @author mamontov
 */
public class CreateSequenceDiagramAction extends AbstractCreateDiagramAction {

    public CreateSequenceDiagramAction() {
    }

    @Override
    public String getDiagramName() {
        return "Sequence";
    }
}
