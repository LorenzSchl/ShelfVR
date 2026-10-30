package de.lorenzschl.shelfvr.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Reines Kotlin-Modul ohne Android-Abhängigkeit. Registriert `testDebugUnitTest` und `lintDebug`
 * als Aliase, damit die Befehle aus CLAUDE.md auch diese Module erfassen.
 */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")
        pluginManager.apply("com.android.lint")

        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JAVA_VERSION
            targetCompatibility = JAVA_VERSION
        }
        configureKotlinJvmTarget()

        tasks.register("testDebugUnitTest") {
            group = "verification"
            description = "Alias für test (einheitlicher Befehl über alle Module)."
            dependsOn("test")
        }
        tasks.register("lintDebug") {
            group = "verification"
            description = "Alias für lint (einheitlicher Befehl über alle Module)."
            dependsOn("lint")
        }

        dependencies {
            add("testImplementation", libs.lib("junit"))
            add("testImplementation", libs.lib("kotlin-test"))
        }
    }
}
