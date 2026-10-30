plugins {
    alias(libs.plugins.shelfvr.android.library)
    alias(libs.plugins.shelfvr.hilt)
}

dependencies {
    api(project(":core:model"))
    implementation(libs.androidx.core.ktx)
    api(libs.media3.session)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.hls)
    implementation(libs.media3.datasource.okhttp)
    implementation(libs.okhttp)
    api(libs.kotlinx.coroutines.core)
}
