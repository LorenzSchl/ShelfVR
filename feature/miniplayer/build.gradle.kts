plugins {
    alias(libs.plugins.shelfvr.android.library)
    alias(libs.plugins.shelfvr.android.compose)
    alias(libs.plugins.shelfvr.android.roborazzi)
}

dependencies {
    api(project(":core:model"))
    implementation(project(":core:designsystem"))
}
