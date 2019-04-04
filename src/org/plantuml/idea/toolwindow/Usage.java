package org.plantuml.idea.toolwindow;

import javax.swing.*;

public class Usage extends JTextArea {
    public static final String TEXT =
            "- Open file, put the cursor inside of the PlantUML code to render it\n"
                    + "- PlantUML code must be inside @startuml and @enduml tags to be rendered\n"
                    + "- To render other than sequence diagram types, install Graphviz and set path to it\n\n"
                    + "- A new file can be created from template (File | New | PlantUML File)\n"
                    + "- PlantUML code can be placed anywhere, '*' are ignored for usage in Java comments\n"
                    + "- About screen tests Graphviz installation\n"
                    + "\n- Intentions (Alt+Enter):\n"
                    + " - reverse arrow\n"
                    + " - with a caret on top of @startuml:\n"
                    + "  - disable syntax check\n"
                    + "  - enable partial rendering - renders each page on it's own, useful for big sequence diagram files\n"
                    + "\n- Performance tips:\n"
                    + " - disable automatic rendering and use Update (Alt+D) or Reload (Alt+F) buttons\n"
                    + " - do not put @newpage into included files (it prohibits incremental rendering)\n"
                    + " - try to enable partial rendering\n"
                    + " - disable syntax checking\n"
                    + " - tune cache size in settings, make sure you have enough heap memory (enable Memory Indicator)\n"
                    + "\n- Other supported PlantUML features:\n"
                    + " - @startuml <filename> - changes default filename when saving/exporting\n"
                    + " - Settings | PlantUML config - useful for global 'skinparam' command\n"
                    + "";

    public Usage() {
        this("");
    }

    public Usage(String prefix) {
        setOpaque(false);
        setText(prefix + TEXT);
        setEditable(false);
    }
}
