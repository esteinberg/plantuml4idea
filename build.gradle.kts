@file:Suppress("HardCodedStringLiteral")

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)
fun Jar.patchManifest() = manifest { attributes("Version" to project.version) }

plugins {
    id("java") // Java support
    alias(libs.plugins.kotlin)
    alias(libs.plugins.gradleIntelliJPlugin) // Gradle IntelliJ Plugin
//    alias(libs.plugins.changelog) // Gradle Changelog Plugin
}

group = properties("pluginGroup").get()

version = "6.2.2-IJ2022.2"

tasks {
    patchPluginXml {
        // https://plugins.jetbrains.com/docs/intellij/build-number-ranges.html
        sinceBuild.set("222.4167.9")
        untilBuild.set("")
        changeNotes.set(
            """
            - IntelliJ 2023.2 compatibility
            - support for @startdef
            - bugfix
            """.trimIndent()
        )
    }
}

// Configure Gradle IntelliJ Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    pluginName = properties("pluginName")
    //https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html#configuration-intellij-extension
    version = "LATEST-EAP-SNAPSHOT"
    type = "IC"

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins = properties("platformPlugins").map { it.split(',').map(String::trim).filter(String::isNotEmpty) }
}


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

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(
        fileTree(
            "lib/plantuml/",
        )
    )
    implementation(
        fileTree(
            "lib/",
        )
    )
}

//// Set the JVM language level used to build the project. Use Java 11 for 2020.3+, and Java 17 for 2022.2+.
kotlin {
    jvmToolchain(17)
}


// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
//changelog {
//    groups.empty()
////    repositoryUrl.set("https://github.com/esteinberg/plantuml4idea")
//}


tasks {
    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }

    buildSearchableOptions {
        enabled = false
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
        dependsOn("patchChangelog")
        token = environment("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
//        channels = properties("pluginVersion").map { listOf(it.split('-').getOrElse(1) { "default" }.split('.').first()) }
    }
}
