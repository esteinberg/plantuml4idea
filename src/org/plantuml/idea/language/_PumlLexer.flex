package org.plantuml.idea.language;
                                  
import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import org.plantuml.idea.language.psi.PumlTypes;
import com.intellij.psi.TokenType;

%%

%{
  public PumlLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class PumlLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode
            
NEW_LINE_INDENT=[\ \t\f]*\R+
WHITE_SPACE=[\ \t\f]
LINE_COMMENT=\s*("'")[^\r\n]*
WORD_CHARACTER=[^\s/\[\(]        
BLOCK_COMMENT="/'"([^'/])*("'/")
BRACKET_IDENTIFIER="["([^\]\R])*("]")
BRACKET_IDENTIFIER2="("([^\)\R])*(")")

%xstate LINE_START_STATE,IN_COMMENT
   
%%  


<LINE_START_STATE>{LINE_COMMENT}                                 { yybegin(YYINITIAL); return PumlTypes.COMMENT; }
<YYINITIAL, LINE_START_STATE>{BLOCK_COMMENT}                         { yybegin(YYINITIAL); return PumlTypes.COMMENT; }
<YYINITIAL, LINE_START_STATE>{BRACKET_IDENTIFIER}                  { yybegin(YYINITIAL); return PumlTypes.IDENTIFIER; }
<YYINITIAL, LINE_START_STATE>{BRACKET_IDENTIFIER2}                  { yybegin(YYINITIAL); return PumlTypes.IDENTIFIER; }
<YYINITIAL, LINE_START_STATE>{WORD_CHARACTER}+                      { yybegin(YYINITIAL); return PumlTypes.IDENTIFIER; }
<YYINITIAL, LINE_START_STATE>"/"                                    { yybegin(YYINITIAL); return PumlTypes.IDENTIFIER; }
<YYINITIAL, LINE_START_STATE>"["                                    { yybegin(YYINITIAL); return PumlTypes.IDENTIFIER; }
<YYINITIAL, LINE_START_STATE>"("                                    { yybegin(YYINITIAL); return PumlTypes.IDENTIFIER; }
<YYINITIAL, LINE_START_STATE>{NEW_LINE_INDENT}                      { yybegin(LINE_START_STATE); return PumlTypes.NEW_LINE_INDENT; }
<YYINITIAL, LINE_START_STATE>{WHITE_SPACE}+                         { yybegin(YYINITIAL); return PumlTypes.WHITE_SPACE; }

[^] { return TokenType.BAD_CHARACTER; }
