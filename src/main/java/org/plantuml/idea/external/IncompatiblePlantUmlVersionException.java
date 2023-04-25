package org.plantuml.idea.external;

public class IncompatiblePlantUmlVersionException extends RuntimeException {
    public IncompatiblePlantUmlVersionException() {
    }

    public IncompatiblePlantUmlVersionException(String message) {
        super(message);
    }

    public IncompatiblePlantUmlVersionException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncompatiblePlantUmlVersionException(Throwable cause) {
        super(cause);
    }

    public IncompatiblePlantUmlVersionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
