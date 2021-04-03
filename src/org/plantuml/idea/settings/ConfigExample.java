package org.plantuml.idea.settings;

import javax.swing.*;

public class ConfigExample extends JTextArea {
    public static final String TEXT =
            "- example:\n" +
                    "!include config.skinparam\nskinparam monochrome true";

    public ConfigExample() {
        this("");
    }

    public ConfigExample(String prefix) {
        setOpaque(false);
        setText(prefix + TEXT);
        setEditable(false);
    }
}
