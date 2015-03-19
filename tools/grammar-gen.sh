#!/bin/bash

cd "$( dirname "${BASH_SOURCE[0]}" )"

if [ ! -f JFlex.jar ]; then
  curl -fsSL "https://github.com/JetBrains/intellij-community/raw/master/tools/lexer/jflex-1.4/lib/JFlex.jar" -o JFlex.jar
fi
if [ ! -f idea-flex.skeleton ]; then
  curl -fsSL "https://raw.github.com/JetBrains/intellij-community/master/tools/lexer/idea-flex.skeleton" -o idea-flex.skeleton
fi

if [ ! -f GrammarKit.zip ]; then
  curl -fsSL "https://github.com/JetBrains/Grammar-Kit/releases/download/1.2.0/GrammarKit.zip" -o GrammarKit.zip
  unzip GrammarKit.zip
fi
if [ ! -f GrammarKit/lib/light-psi-all.jar ]; then
  curl -fsSL "https://github.com/JetBrains/Grammar-Kit/releases/download/1.2.0.1/light-psi-all.jar" -o GrammarKit/lib/light-psi-all.jar
fi


cd ..
java -jar tools/JFlex.jar --sliceandcharat --skel tools/idea-flex.skeleton -d gen/org/plantuml/idea/lang/parser src/org/plantuml/idea/lang/parser/PlantUml.flex
java -jar tools/GrammarKit/lib/grammar-kit.jar gen src/org/plantuml/idea/lang/parser/PlantUml.bnf
