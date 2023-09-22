PlantUML plugin for IntelliJ platform
=============

[![JetBrains plugins][plugin-version-svg]][plugin-repo]
[![JetBrains plugins][plugin-downloads-svg]][plugin-repo]

This [plugin][plugin-repo] provides integration with popular [PlantUML](http://plantuml.sourceforge.net/) diagramming tool

---

Author:
[Eugene Steinberg](https://github.com/esteinberg)

Contributors:

* [Ivan Mamontov](https://github.com/IvanMamontov)
* [Henady Zakalusky](https://github.com/hza)
* [Max Gorbunov](https://github.com/6zow)
* [Vojtěch Krása](https://github.com/krasa)
* [Andrew Korolev](https://github.com/koroandr)

# Features
* PlantUML tool window renders PlantUML source code under caret in currently selected editor
* Structure view, code navigation between declaration and usages, renaming
* Supports multiple sources per file
* Supports pagination and zoom
* Can copy diagram to clipboard or export as PNG, EPS or SVG, ASCII Art
* Caching and incremental rendering

# Tips

* PlantUML code must be inside @startuml and @enduml tags to be rendered.
* To be able to generate many diagram types, you must have [Graphviz](https://www.graphviz.org/download/)
  installed on your machine (not needed for Windows, it is bundled in the PlantUML jar). 

# [AsciiMath installation](https://plantuml.com/ascii-math)

1. Download PlantUML jar - https://plantuml.com/download
2. Download JLatexMath libs - https://jar-download.com/artifacts/org.scilab.forge/jlatexmath -
   use `Download jlatexmath.jar (xxx)` button, unzip `jar_files.zip`
3. Go to setting in IntelliJ for PlantUML
4. Set PlantUML JAR to the folder containing jars you downloaded

# Developer notes

* debug logs can be enabled by adding '#org.plantuml' and 'org.plantuml' to [Help | Debug Log Settings]
* use [Jetbrains JDK](https://confluence.jetbrains.com/display/JBR/JetBrains+Runtime) if normal JDK produces UI bugs

## There are following branches:

### master

* Current production branch

### grammar

* old experimental syntax support - never finished
* Grammar classes can be generated using tools/grammar-gen.sh
* This script can run automatically when you run the plugin using "Plugin" Run/Debug configuration. Just add the script
above as an external tool and make it run before the "Make" step.



<!-- Badges -->
[plugin-repo]: https://plugins.jetbrains.com/plugin/7017-plantuml-integration
[plugin-version-svg]: https://img.shields.io/jetbrains/plugin/v/7017-plantuml-integration.svg
[plugin-downloads-svg]: https://img.shields.io/jetbrains/plugin/d/7017-plantuml-integration.svg
