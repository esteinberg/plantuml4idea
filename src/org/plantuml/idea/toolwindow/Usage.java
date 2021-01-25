package org.plantuml.idea.toolwindow;

import javax.swing.*;

public class Usage extends JTextArea {
    public static final String TEXT =
            "- Open a file, put the cursor inside of the PlantUML code to render it\n"
                    + "- PlantUML code must be inside of @startuml and @enduml tags to be rendered, or their equivalent\n"
                    + "- Graphviz is now included in PlantUML, install your own if needed and set a path to it\n\n"

                    + "- A new file can be created from template (File | New | PlantUML File)\n"
                    + "- PlantUML code can be placed anywhere, '*' are ignored for usage in Java comments\n"
                    + "- About screen tests Graphviz installation\n\n"

                    + "- Clicking on the rendered image's text finds it in the editor\n"
                    + "- Ctrl + Click in the editor navigates between occurrences\n"
                    + "- File Structure displays the first occurrence\n"
                    + "\nIntentions (Alt+Enter):\n"
                    + "- reverse arrow\n"
                    + "- with a caret on top of @startuml:\n"
                    + " - disable syntax check\n"
                    + " - enable partial rendering - renders each page on it's own, useful for big sequence diagram files\n"
                    + "\nPerformance tips:\n"
                    + "- disable automatic rendering and use Update (Ctrl Alt Shift F) or Reload (Ctrl Alt Shift G) buttons\n"
                    + "- do not put @newpage into included files (it prohibits incremental and partial rendering)\n"
                    + "- try to enable partial rendering - add to the first page: 'idea.partialRender \n"
                    + "- disable 'Render links for PNG'\n"
                    + "- disable syntax checking - add to the first page: 'idea.disableSyntaxCheck\n"
                    + "- reduce limits and disable 'SVG preview scaling'\n"
                    + "- tune cache size in settings, make sure you have enough heap memory (enable Memory Indicator)\n"
                    + "\nOther supported PlantUML features:\n"
                    + "- @startuml <filename> - changes default filename when saving/exporting\n"
                    + "- Settings | PlantUML config - useful for global 'skinparam' command\n"
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
