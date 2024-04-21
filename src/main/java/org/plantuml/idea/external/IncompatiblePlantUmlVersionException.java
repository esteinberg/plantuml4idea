package org.plantuml.idea.external;

public class IncompatiblePlantUmlVersionException extends ClassNotFoundException {
    public IncompatiblePlantUmlVersionException() {
    }

    public IncompatiblePlantUmlVersionException(String message) {
        super(message);
    }

    public IncompatiblePlantUmlVersionException(String message, Throwable cause) {
        super(message, cause);
    }

}
