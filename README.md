plantuml4idea
=============

Intellij [IDEA plugin for PlantUML](http://plugins.intellij.net/plugin/?idea&id=7017)

This plugin provides integration with popular [PlantUML](http://plantuml.sourceforge.net/) diagramming tool

Author: [Eugene Steinberg](https://github.com/esteinberg)

Contributors:

 * [Ivan Mamontov](https://github.com/IvanMamontov)
 * [Henady Zakalusky](https://github.com/hza)
 * [Max Gorbunov](https://github.com/6zow)
 * [Vojtěch Krása] (https://github.com/krasa)
 * [Andrew Korolev] (https://github.com/koroandr)

# Features

* PlantUML tool window renders PlantUML source code under caret in currently selected editor
* Supports multiple sources per file
* Supports pagination and zoom
* Can copy diagram to clipboard or export as PNG, EPS or SVG, ASCII Art

# Tips

* Don't forget that @startuml tag is required.
* To be able to generate many diagram types, you must have [Graphviz](http://plantuml.sourceforge.net/graphvizdot.html)
 installed on your machine. About screen tests your installation.

# Developer notes

There are following branches:

* debug logs can be enabled by adding '#org.plantuml' to [Help | Debug Log Settings] 

### master
* Current production branch
* No setup needed, checkout and run.

### 1.x
* deprecated branch, to be deleted

### grammar
* Contains new experimental syntax support
* Grammar classes can be generated using tools/grammar-gen.sh
* This script can run automatically when you run the plugin using "Plugin" Run/Debug confuguration. Just add the script
above as an external tool and make it run before the "Make" step.
