plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.gradle.plugin)
    compileOnly(libs.ksp.gradle.plugin)
    compileOnly(libs.hilt.gradle.plugin)
    compileOnly(libs.roborazzi.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "shelfvr.android.application"
            implementationClass = "de.lorenzschl.shelfvr.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "shelfvr.android.library"
            implementationClass = "de.lorenzschl.shelfvr.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "shelfvr.android.compose"
            implementationClass = "de.lorenzschl.shelfvr.buildlogic.AndroidComposeConventionPlugin"
        }
        register("androidRoborazzi") {
            id = "shelfvr.android.roborazzi"
            implementationClass = "de.lorenzschl.shelfvr.buildlogic.AndroidRoborazziConventionPlugin"
        }
        register("hilt") {
            id = "shelfvr.hilt"
            implementationClass = "de.lorenzschl.shelfvr.buildlogic.HiltConventionPlugin"
        }
        register("jvmLibrary") {
            id = "shelfvr.jvm.library"
            implementationClass = "de.lorenzschl.shelfvr.buildlogic.JvmLibraryConventionPlugin"
        }
    }
}
