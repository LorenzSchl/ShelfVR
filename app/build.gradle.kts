plugins {
    alias(libs.plugins.shelfvr.android.application)
    alias(libs.plugins.shelfvr.android.compose)
    alias(libs.plugins.shelfvr.android.roborazzi)
    alias(libs.plugins.shelfvr.hilt)
}

android {
    namespace = "de.lorenzschl.shelfvr"

    defaultConfig {
        applicationId = "de.lorenzschl.shelfvr"
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:network"))
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":playback"))
    implementation(project(":feature:miniplayer"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}
