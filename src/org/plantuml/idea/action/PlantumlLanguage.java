package org.plantuml.idea.action;

import com.intellij.lang.CompositeLanguage;

public class PlantumlLanguage extends CompositeLanguage {

    PlantumlLanguage() {
        super("Plantuml", "text/story");
    }
}