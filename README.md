plantuml4idea
=============

Intellij [IDEA plugin for PlantUML](http://plugins.intellij.net/plugin/?idea&id=7017)

This plugin provides integration with popular [PlantUML](http://plantuml.sourceforge.net/) diagramming tool

Author: [Eugene Steinberg](https://github.com/esteinberg)

Contributors:

 * [Ivan Mamontov](https://github.com/IvanMamontov)
 * [Henady Zakalusky](https://github.com/hza)
 * [Max Gorbunov](https://github.com/6zow)
 * [Vojtěch Krása](https://github.com/krasa)
 * [Andrew Korolev](https://github.com/koroandr)

# Features

* PlantUML tool window renders PlantUML source code under caret in currently selected editor
* Supports multiple sources per file
* Supports pagination and zoom
* Can copy diagram to clipboard or export as PNG, EPS or SVG, ASCII Art
* Caching and incremental rendering 

# Tips

* PlantUML code must be inside @startuml and @enduml tags to be rendered.
* To be able to generate many diagram types, you must have [Graphviz](http://plantuml.sourceforge.net/graphvizdot.html)
 installed on your machine. About screen tests your installation.

# Developer notes
* Project setup: [gif](https://user-images.githubusercontent.com/1160875/55478653-7dbb2300-561c-11e9-8a58-66f5a66b5dc1.gif) [mp4](https://mega.nz/#!66oTUIgA!ckkAdLZNHtXjIwyoSlN6BwA-vEWh_034vTRqtWZr9AM)
* debug logs can be enabled by adding '#org.plantuml' to [Help | Debug Log Settings] 

## There are following branches:

### master
* Current production branch

### 1.x
* deprecated branch, to be deleted

### grammar
* Contains new experimental syntax support
* Grammar classes can be generated using tools/grammar-gen.sh
* This script can run automatically when you run the plugin using "Plugin" Run/Debug configuration. Just add the script
above as an external tool and make it run before the "Make" step.
