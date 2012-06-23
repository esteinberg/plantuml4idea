package org.plantuml.idea.plantuml;

import java.util.Arrays;

/**
 *
 * @author Eugene Steinberg
 */
public class PlantUmlResult {
    private byte[] diagramBytes;
    private String description;
    private String error;

    public PlantUmlResult(byte[] diagramBytes, String description, String error) {
        this.diagramBytes = diagramBytes;
        this.description = description;
        this.error = error;
    }

    public byte[] getDiagramBytes() {
        return diagramBytes;
    }

    public void setDiagramBytes(byte[] diagramBytes) {
        this.diagramBytes = diagramBytes;
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
                "diagramBytes=" + Arrays.toString(diagramBytes) +
                ", description='" + description + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
