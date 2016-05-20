package org.plantuml.idea.lang.annotator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Author: Eugene Steinberg
 * Date: 9/27/14
 */
public enum LanguageDescriptor {
    INSTANCE;

    public final List<String> types = Collections.unmodifiableList(Arrays.asList(
            "actor",
            "participant",
            "usecase",
            "class",
            "interface",
            "abstract",
            "enum",
            "component",
            "state",
            "object",
            "artifact",
            "folder",
            "rect",
            "node",
            "frame",
            "cloud",
            "database",
            "storage",
            "agent",
            "boundary",
            "control",
            "entity",
            "card"
    ));

    public final List<String> keywords = Collections.unmodifiableList(Arrays.asList(
            "as",
            "also",
            "autonumber",
            "title",
            "newpage",
            "box",
            "alt",
            "else",
            "opt",
            "loop",
            "par",
            "break",
            "critical",
            "note",
            "group",
            "left",
            "right",
            "of",
            "on",
            "link",
            "over",
            "end",
            "activate",
            "deactivate",
            "destroy",
            "create",
            "footbox",
            "hide",
            "show",
            "skinparam",
            "skin",
            "top",
            "bottom",
            "top to bottom direction",
            "package",
            "namespace",
            "page",
            "up",
            "down",
            "if",
            "else",
            "elseif",
            "endif",
            "partition",
            "footer",
            "header",
            "center",
            "rotate",
            "ref",
            "return",
            "is",
            "repeat",
            "start",
            "stop",
            "while",
            "endwhile",
            "fork",
            "again",
            "kill"
    ));

    public final List<String> preproc = Collections.unmodifiableList(Arrays.asList(
            "startuml",
            "startditaa",
            "startdot",
            "enduml",
            "include",
            "define",
            "undef",
            "ifdef",
            "endif",
            "ifndef"
    ));


}
