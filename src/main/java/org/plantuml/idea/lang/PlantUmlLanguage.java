package org.plantuml.idea.lang;

import com.intellij.lang.Language;

public class PlantUmlLanguage extends Language {

    public static final PlantUmlLanguage INSTANCE = new PlantUmlLanguage();

    private PlantUmlLanguage() {
        super("PUML");
    }


}