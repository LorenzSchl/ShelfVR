plugins {
    alias(libs.plugins.shelfvr.android.library)
    alias(libs.plugins.shelfvr.hilt)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(project(":domain"))
    api(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(libs.tink.android)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
