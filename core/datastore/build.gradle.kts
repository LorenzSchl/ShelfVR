plugins {
    alias(libs.plugins.shelfvr.android.library)
    alias(libs.plugins.shelfvr.hilt)
}

dependencies {
    api(project(":core:model"))
    api(libs.datastore.preferences)
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
}
