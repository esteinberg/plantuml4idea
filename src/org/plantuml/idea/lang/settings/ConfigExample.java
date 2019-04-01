package org.plantuml.idea.lang.settings;

import javax.swing.*;

public class ConfigExample extends JTextArea {
    public static final String TEXT =
            "Example:\n" +
                    "!include config.skinparam\nskinparam monochrome true\nBob --> Alice: Authentication Response";

    public ConfigExample() {
        this("");
    }

    public ConfigExample(String prefix) {
        setOpaque(false);
        setText(prefix + TEXT);
        setEditable(false);
    }
}
