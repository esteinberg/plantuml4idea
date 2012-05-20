package org.plantuml.idea.plantuml;

import java.awt.image.BufferedImage;

/**
 * User: eugene
 * Date: 5/13/12
 * Time: 9:22 PM
 */
public class PlantUmlResult {
    private BufferedImage diagram;
    private String description;
    private String error;

    public PlantUmlResult(BufferedImage diagram, String description, String error) {
        this.diagram = diagram;
        this.description = description;
        this.error = error;
    }

    public BufferedImage getDiagram() {
        return diagram;
    }

    public void setDiagram(BufferedImage diagram) {
        this.diagram = diagram;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isError() {
        return (description == null || description.isEmpty());
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "PlantUmlResult{" +
                "diagram=" + diagram +
                ", description='" + description + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
