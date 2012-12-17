package org.plantuml.idea.action;

import com.intellij.lang.CompositeLanguage;

public class PlantUmlLanguage extends CompositeLanguage {

    public static final PlantUmlLanguage INSTANCE = new PlantUmlLanguage();

    private PlantUmlLanguage() {
        super("PlantUML", "text/uml");
    }
}