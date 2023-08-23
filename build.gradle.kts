@file:Suppress("HardCodedStringLiteral")
import org.jetbrains.changelog.Changelog


fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)
fun Jar.patchManifest() = manifest { attributes("Version" to project.version) }

plugins {
    id("java") // Java support
    alias(libs.plugins.kotlin)
    alias(libs.plugins.gradleIntelliJPlugin) // Gradle IntelliJ Plugin
    alias(libs.plugins.changelog) // Gradle Changelog Plugin
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

// Configure project's dependencies
repositories {
    mavenCentral()
    maven {
        url = uri("https://cache-redirector.jetbrains.com/intellij-dependencies")
    }
}

java.sourceSets["main"].java {
    srcDir("gen")
}

// Dependencies are managed with Gradle version catalog - read more: https://docs.gradle.org/current/userguide/platforms.html#sub:version-catalog
dependencies {
// https://mvnrepository.com/artifact/net.sourceforge.plantuml/plantuml
    implementation("net.sourceforge.plantuml:plantuml:1.2023.9")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("org.jetbrains.intellij.deps.batik:batik-transcoder:1.16.0-32") {
        exclude(group = "it.unimi.dsi", module = "fastutil")
//        exclude(group = "xml-apis", module = "xml-apis")
//        exclude(group = "xml-apis", module = "xml-apis-ext")
        exclude(group = "commons-io", module = "commons-io")
        exclude(group = "commons-logging", module = "commons-logging")
    }

//    implementation("io.sf.carte:echosvg-all:0.3")

//    2.7.3 breaks org.plantuml.idea.rendering.ImageItem.parseLinks
    implementation("xalan:xalan:2.7.0")


// https://mvnrepository.com/artifact/xerces/xercesImpl
    implementation("xerces:xercesImpl:2.12.2")
// https://mvnrepository.com/artifact/net.sf.saxon/Saxon-HE
    implementation("net.sf.saxon:Saxon-HE:12.2")

}

repositories {
    maven {
        url = uri("https://css4j.github.io/maven/")
        content {
            // this repository *only* contains artifacts with group "my.company"
            includeGroup("com.github.css4j")
            includeGroup("io.sf.carte")
            includeGroup("io.sf.jclf")
        }
    }
}

//// Set the JVM language level used to build the project. Use Java 11 for 2020.3+, and Java 17 for 2022.2+.
kotlin {
    jvmToolchain(17)
}


// Configure Gradle IntelliJ Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    pluginName = properties("pluginName")
    version = properties("platformVersion")
    type = properties("platformType")

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins = properties("platformPlugins").map { it.split(',').map(String::trim).filter(String::isNotEmpty) }
}


// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl = properties("pluginRepositoryUrl")
}



tasks {
    buildSearchableOptions {
        enabled = false
    }
    compileJava {
        options.encoding = "UTF-8"
    }
    compileTestJava {
        options.encoding = "UTF-8"
    }

    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }

    patchPluginXml {
        version = properties("pluginVersion")
        sinceBuild = properties("pluginSinceBuild")
        untilBuild = properties("pluginUntilBuild")

//        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
//        pluginDescription = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
//            val start = "<!-- Plugin description -->"
//            val end = "<!-- Plugin description end -->"
//
//            with (it.lines()) {
//                if (!containsAll(listOf(start, end))) {
//                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
//                }
//                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
//            }
//        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = properties("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }
    }

    // Configure UI tests plugin
    // Read more: https://github.com/JetBrains/intellij-ui-test-robot
    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
        systemProperty("jb.consents.confirmation.enabled", "false")
    }

    signPlugin {
        certificateChain = environment("CERTIFICATE_CHAIN")
        privateKey = environment("PRIVATE_KEY")
        password = environment("PRIVATE_KEY_PASSWORD")
    }

    publishPlugin {
//        dependsOn("patchChangelog")
        token = environment("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
//        channels = properties("pluginVersion").map { listOf(it.split('-').getOrElse(1) { "default" }.split('.').first()) }
    }
}
