pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ShelfVR"

// Modulschnitt nach PROJECT_SPEC.md §5.1. Module für Stufe 2 (:feature:shelfwindow,
// :feature:playerwindow) kommen erst mit M6/M7 dazu.
include(":core:model")
include(":core:network")
include(":core:database")
include(":core:datastore")
include(":domain")
include(":data")
