import com.android.build.api.variant.HasHostTests
import com.android.build.api.variant.HostTestBuilder

plugins {
    alias(libs.plugins.shelfvr.android.library)
    alias(libs.plugins.shelfvr.hilt)
    alias(libs.plugins.room)
}

// Schemas werden versioniert, damit Migrationen von Anfang an prüfbar sind.
room {
    schemaDirectory("$projectDir/schemas")
}

// MigrationTestHelper liest die exportierten Schemas als Assets der Unit-Tests.
androidComponents {
    onVariants { variant ->
        (variant as? HasHostTests)?.hostTests?.get(HostTestBuilder.UNIT_TEST_TYPE)
            ?.sources?.assets?.addStaticSourceDirectory("schemas")
    }
}

dependencies {
    api(project(":core:model"))
    api(libs.room.runtime)
    ksp(libs.room.compiler)

    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.room.testing)
    testImplementation(libs.kotlinx.coroutines.test)
}
