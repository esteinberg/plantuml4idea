package org.plantuml.idea.language;
                                  
import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import org.plantuml.idea.language.psi.PumlTypes;
import com.intellij.psi.TokenType;

%%

%{
  public _PumlLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _PumlLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode
            
NEW_LINE_INDENT=[\ \t\f]*\R+
WHITE_SPACE=[\ \t\f]
END_OF_LINE_COMMENT=\s*("'")[^\r\n]*
WORD_CHARACTER=[^\s/]        
BLOCK_COMMENT="/'"([^'/])*("'/")

%xstate LINE_START_STATE,IN_COMMENT
   
%%  

//<YYINITIAL,LINE_START_STATE>"/'"         {yybegin(IN_COMMENT);return PumlTypes.COMMENT;}
//<IN_COMMENT>"'/"     { yybegin(YYINITIAL);return PumlTypes.COMMENT;}
//<IN_COMMENT>[^'\n]+   {return PumlTypes.COMMENT;}
//<IN_COMMENT>"'"       {return PumlTypes.COMMENT;}
//<IN_COMMENT>\n       {return PumlTypes.COMMENT; }


<LINE_START_STATE>{END_OF_LINE_COMMENT}                          { yybegin(YYINITIAL); return PumlTypes.COMMENT; }
<YYINITIAL, LINE_START_STATE>{BLOCK_COMMENT}                         { yybegin(YYINITIAL); return PumlTypes.COMMENT; }
<YYINITIAL, LINE_START_STATE>{WORD_CHARACTER}+                      { yybegin(YYINITIAL); return PumlTypes.IDENTIFIER; }
<YYINITIAL, LINE_START_STATE>"/"                                    { yybegin(YYINITIAL); return PumlTypes.IDENTIFIER; }
<YYINITIAL, LINE_START_STATE>{NEW_LINE_INDENT}                      { yybegin(LINE_START_STATE); return PumlTypes.NEW_LINE_INDENT; }
<YYINITIAL, LINE_START_STATE>{WHITE_SPACE}+                         { yybegin(YYINITIAL); return PumlTypes.WHITE_SPACE; }

[^] { return TokenType.BAD_CHARACTER; }
