package de.lorenzschl.shelfvr.buildlogic

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.lib(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).orElseThrow { IllegalArgumentException("Unbekannte Library im Katalog: $alias") }

internal fun VersionCatalog.int(alias: String): Int =
    findVersion(alias).get().requiredVersion.toInt()

internal val JAVA_VERSION = JavaVersion.VERSION_17

/**
 * Bewusst abgeschaltete Lint-Checks:
 * - Versions-Hinweise: Updates erfolgen gezielt, neue Versionen erst ab ca. zwei Wochen Alter.
 * - OldTargetApi: targetSdk folgt der Android-Basis von Horizon OS (API 34), nicht dem neuesten SDK.
 */
internal val LINT_DISABLED = setOf("NewerVersionAvailable", "AndroidGradlePluginVersion", "GradleDependency", "OldTargetApi")

internal fun Project.configureKotlinJvmTarget() {
    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }
}
