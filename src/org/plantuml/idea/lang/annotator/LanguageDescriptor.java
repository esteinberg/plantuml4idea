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

    public static final String IDEA_PARTIAL_RENDER = "idea.partialRender";
    public static final String IDEA_DISABLE_SYNTAX_CHECK = "idea.disableSyntaxCheck";
    public final List<String> types = Collections.unmodifiableList(Arrays.asList(
            "abstract",
            "actor",
            "agent",
            "artifact",
            "boundary",
            "card",
            "class",
            "cloud",
            "component",
            "control",
            "database",
            "entity",
            "enum",
            "folder",
            "frame",
            "interface",
            "node",
            "object",
            "participant",
            "rect",
            "state",
            "storage",
            "usecase"
    ));

    public final List<String> keywords = Collections.unmodifiableList(Arrays.asList(
            "as",
            "also",
            "autonumber",
            "caption",
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
            "rnote",
            "hnote",
            "legend",
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

    public final List<String> pluginSettingsPattern = Collections.unmodifiableList(Arrays.asList(
            IDEA_PARTIAL_RENDER,
            IDEA_DISABLE_SYNTAX_CHECK
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
