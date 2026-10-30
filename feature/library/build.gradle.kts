// Platzhalter nach PROJECT_SPEC.md §5.1; Inhalt folgt ab M1.
plugins {
    alias(libs.plugins.shelfvr.android.library)
    alias(libs.plugins.shelfvr.android.compose)
}

dependencies {
    implementation(project(":core:designsystem"))
}
