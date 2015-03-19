package org.plantuml.idea.lang.parser;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import static org.plantuml.idea.lang.psi.PlantUmlTokenTypes.*;

%%

%class _PlantUmlLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}

CRLF= \r\n | \n | \r
LINE_SPACE=[\ \t\f]
WHITE_SPACE_CHAR={LINE_SPACE} | {CRLF}
START_UML="@startuml"
END_UML="@enduml"
ONE_LINE_COMMENT="'" [^\r\n]*
MULTI_LINE_COMMENT_START="/'"
MULTI_LINE_COMMENT_END="'/"

STRING_LITERAL="\"" [^\"\n\r]* "\""

IDENTIFIER=([:letter:] | "_") ([:letter:] | [0-9_])*
DIGITS=[0-9]+


%state IN_UML
%state MULTI_LINE_COMMENT

%%


<YYINITIAL> {START_UML}                 { yybegin(IN_UML); return START_UML; }
<YYINITIAL> [^@]*                       { return MULTI_LINE_COMMENT_TEXT; }
<YYINITIAL> "@"+                        { return MULTI_LINE_COMMENT_TEXT; }

{MULTI_LINE_COMMENT_START}              { yybegin(MULTI_LINE_COMMENT); return MULTI_LINE_COMMENT_START; }
<MULTI_LINE_COMMENT> {MULTI_LINE_COMMENT_END} { yybegin(IN_UML); return MULTI_LINE_COMMENT_END; }
<MULTI_LINE_COMMENT> [^']*              { return MULTI_LINE_COMMENT_TEXT; }
{ONE_LINE_COMMENT}                      { return ONE_LINE_COMMENT; }

{STRING_LITERAL}                        { return STRING_LITERAL; }

"abstract"                              { return ABSTRACT; }
"activate"                              { return ACTIVATE; }
"actor"                                 { return ACTOR; }
"agent"                                 { return AGENT; }
"also"                                  { return ALSO; }
"alt"                                   { return ALT; }
"annotation"                            { return ANNOTATION; }
"artifact"                              { return ARTIFACT; }
"as"                                    { return AS; }
"attributes"                            { return ATTRIBUTES; }
"autoactivate"                          { return AUTOACTIVATE; }
"autonumber"                            { return AUTONUMBER; }
"bold"                                  { return BOLD; }
"bottom"                                { return BOTTOM; }
"boundary"                              { return BOUNDARY; }
"box"                                   { return BOX; }
"break"                                 { return BREAK; }
"card"                                  { return CARD; }
"center"                                { return CENTER; }
"class"                                 { return CLASS; }
"cloud"                                 { return CLOUD; }
"component"                             { return COMPONENT; }
"control"                               { return CONTROL; }
"create"                                { return CREATE; }
"critical"                              { return CRITICAL; }
"dashed"                                { return DASHED; }
"database"                              { return DATABASE; }
"deactivate"                            { return DEACTIVATE; }
"destroy"                               { return DESTROY; }
"direction"                             { return DIRECTION; }
"dotted"                                { return DOTTED; }
"down"                                  { return DOWN; }
"else"                                  { return ELSE; }
"empty"                                 { return EMPTY; }
"end"                                   { return END; }
"endfooter"                             { return END_FOOTER; }
"endheader"                             { return END_HEADER; }
"endhnote"                              { return END_HNOTE; }
"endlegend"                             { return END_LEGEND; }
"endnote"                               { return END_NOTE; }
"endref"                                { return END_REF; }
"endrnote"                              { return END_RNOTE; }
"endtitle"                              { return END_TITLE; }
"entity"                                { return ENTITY; }
"enum"                                  { return ENUM; }
"extends"                               { return EXTENDS; }
"fields"                                { return FIELDS; }
"folder"                                { return FOLDER; }
"footbox"                               { return FOOTBOX; }
"footer"                                { return FOOTER; }
"for"                                   { return FOR; }
"frame"                                 { return FRAME; }
"group"                                 { return GROUP; }
"header"                                { return HEADER; }
"height"                                { return HEIGHT; }
"hide"                                  { return HIDE; }
"hidden"                                { return HIDDEN; }
"hnote"                                 { return HNOTE; }
"ignore"                                { return IGNORE; }
"ignorenewpage"                         { return IGNORE_NEWPAGE; }
"interface"                             { return INTERFACE; }
"implements"                            { return IMPLEMENTS; }
"import"                                { return IMPORT; }
"is"                                    { return IS; }
"left"                                  { return LEFT; }
"legend"                                { return LEGEND; }
"link"                                  { return LINK; }
"loop"                                  { return LOOP; }
"members"                               { return MEMBERS; }
"methods"                               { return METHODS; }
"minwidth"                              { return MINWIDTH; }
"mouseover"                             { return MOUSEOVER; }
"namespace"                             { return NAMESPACE; }
"namespaceseparator"                    { return NAMESPACESEPARATOR; }
"newpage"                               { return NEWPAGE; }
"node"                                  { return NODE; }
"note"                                  { return NOTE; }
"of"                                    { return OF; }
"off"                                   { return OFF; }
"on"                                    { return ON; }
"opt"                                   { return OPT; }
"over"                                  { return OVER; }
"package"                               { return PACKAGE; }
"page"                                  { return PAGE; }
"par"                                   { return PAR; }
"par2"                                  { return PAR2; }
"participant"                           { return PARTICIPANT; }
"private"                               { return PRIVATE; }
"protected"                             { return PROTECTED; }
"public"                                { return PUBLIC; }
"rectangle"                             { return RECTANGLE; }
"ref"                                   { return REF; }
"right"                                 { return RIGHT; }
"rnote"                                 { return RNOTE; }
"rotate"                                { return ROTATE; }
"scale"                                 { return SCALE; }
"set"                                   { return SET;}
"show"                                  { return SHOW; }
"skin"                                  { return SKIN; }
"skinparam"                             { return SKINPARAM; }
"skinparamlocked"                       { return SKINPARAMLOCKED; }
"sprite"                                { return SPRITE; }
"stereotypes"                           { return STEREOTYPES; }
"storage"                               { return STORAGE; }
"title"                                 { return TITLE; }
"to"                                    { return TO;}
"top"                                   { return TOP; }
"unlinked"                              { return UNLINKED; }
"up"                                    { return UP; }
"url"                                   { return URL; }
"usecase"                               { return USECASE; }
"width"                                 { return WIDTH; }

"{"                                     { return OPEN_CURLY_BRACE; }
"}"                                     { return CLOSE_CURLY_BRACE; }
"("                                     { return LPAREN; }
")"                                     { return RPAREN; }
"["                                     { return LBRACKET; }
"]"                                     { return RBRACKET; }
"<"                                     { return LT; }
">"                                     { return GT; }
","                                     { return COMMA; }
"."                                     { return DOT; }
":"                                     { return COLON; }
"/"                                     { return SLASH; }
"\\"                                    { return BACKSLASH; }
"$"                                     { return DOLLAR_SIGN; }
"#"                                     { return HASH_SIGN; }
"@"                                     { return AT_SIGN; }
"-"                                     { return MINUS_SIGN; }
"+"                                     { return PLUS_SIGN; }
"*"                                     { return STAR_SIGN; }
"!"                                     { return EXCLAMATION_SIGN; }
"="                                     { return EQUALS_SIGN; }
"|"                                     { return PIPE_SIGN; }
"^"                                     { return HAT_SIGN; }
"~"                                     { return TILDE_SIGN; }
"\""                                    { return DOUBLE_QUOTE_SIGN; }

"!pragma" [^\n\r]* / {CRLF}             { return PRAGMA; }

<IN_UML> {END_UML}                      { yybegin(YYINITIAL); return END_UML; }

{IDENTIFIER}                            { return IDENTIFIER; }

{DIGITS}                                { return DIGITS; }

{LINE_SPACE}+                           { return WHITE_SPACE; }
{CRLF} (" "* "* ")?                     { return CRLF; }
.                                       { return BAD_CHARACTER; }
