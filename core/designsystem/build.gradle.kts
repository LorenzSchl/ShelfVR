plugins {
    alias(libs.plugins.shelfvr.android.library)
    alias(libs.plugins.shelfvr.android.compose)
    alias(libs.plugins.shelfvr.android.roborazzi)
}

dependencies {
    api(libs.compose.material3)
    api(libs.compose.material.icons.extended)
}
