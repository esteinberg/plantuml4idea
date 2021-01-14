package org.plantuml.idea.lang.annotator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * {@link net.sourceforge.plantuml.syntax.LanguageDescriptor}
 * 
 * Author: Eugene Steinberg
 * Date: 9/27/14
 */
public enum LanguageDescriptor {
    INSTANCE;

    public static final String IDEA_PARTIAL_RENDER = "idea.partialRender";
    public static final String IDEA_DISABLE_SYNTAX_CHECK = "idea.disableSyntaxCheck";

    public static final String TAGS = "uml|json|dot|jcckit|ditaa|salt|math|latex|mindmap|gantt|wbs|yaml";
    
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
            "rectangle",
            "node",
            "frame",
            "cloud",
            "database",
            "storage",
            "agent",
            "stack",
            "boundary",
            "control",
            "entity",
            "card",
            "file",
            "package",
            "queue",
            "archimate",
            "diamond" ,
            "detach"

    ));
    public final List<String> tags = Collections.unmodifiableList(Arrays.asList(
            "@startuml",
            "@startdot",
            "@startjcckit",
            "@startjson",
            "@startditaa",
            "@startsalt",
            "@startmath",
            "@startlatex",
            "@startmindmap",
            "@startgantt",
            "@startwbs",
            "@enduml",
            "@enddot",
            "@endjcckit",
            "@endditaa",
            "@endjson",
            "@endsalt",
            "@endmath",
            "@endlatex",
            "@endmindmap",
            "@endgantt",
            "@startyaml",
            "@endyaml",
            "@endwbs"

    ));

    public final List<String> keywords2 = Collections.unmodifiableList(Arrays.asList(
            "as",
            "also",
            "of",
            "on",
            "is"
    ));
    
    public final List<String> keywords = Collections.unmodifiableList(Arrays.asList(
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
            "repeat",
            "start",
            "stop",
            "while",
            "endwhile",
            "fork",
            "again",
            "kill",
            "order",
            "allow_mixing",
            "allowmixing",
            "mainframe" ,
            "across",
            "stereotype",
            "split",
            "style",
            "sprite",		
            "circle",
            "empty",
            "members",
            "description",
            "true",
            "false",
            "normal",
            "italic",
            "bold",
            "plain",
            "color",
            "dotted",
            "dashed",
            "bold",
            "map"
            
            
    ));

    public final List<String> pluginSettingsPattern = Collections.unmodifiableList(Arrays.asList(
            IDEA_PARTIAL_RENDER,
            IDEA_DISABLE_SYNTAX_CHECK
    ));



    public final List<String> preproc = Collections.unmodifiableList(Arrays.asList(
            "!exit",
            "!include",
            "!pragma",
            "!define",
            "!undef",
            "!if",
            "!ifdef",
            "!endif",
            "!ifndef",
            "!else",
            "!definelong",
            "!enddefinelong" ,
            "!function",
            "!procedure",
            "!endfunction",
            "!endprocedure",
            "!unquoted",
            "!return",
            "!startsub",
            "!endsub",
            "!assert",
            "!log",
            "!local",
            "!dump_memory",
            "!import"
    ));



}
