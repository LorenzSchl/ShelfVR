package de.lorenzschl.shelfvr.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")

        extensions.configure<LibraryExtension> {
            // Namespace aus dem Modulpfad ableiten: :core:network -> de.lorenzschl.shelfvr.core.network
            namespace = "de.lorenzschl.shelfvr" + path.replace(':', '.')
            compileSdk = libs.int("compileSdk")
            defaultConfig {
                minSdk = libs.int("minSdk")
            }
            compileOptions {
                sourceCompatibility = JAVA_VERSION
                targetCompatibility = JAVA_VERSION
            }
            testOptions {
                targetSdk = libs.int("targetSdk")
                unitTests.isIncludeAndroidResources = true
            }
            lint {
                targetSdk = libs.int("targetSdk")
                abortOnError = true
                disable += LINT_DISABLED
            }
        }
        configureKotlinJvmTarget()

        dependencies {
            add("testImplementation", libs.lib("junit"))
            add("testImplementation", libs.lib("kotlin-test"))
        }
    }
}
