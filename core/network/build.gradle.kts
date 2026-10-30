plugins {
    alias(libs.plugins.shelfvr.jvm.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(libs.okhttp)
    api(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
}

// Integrationstests gegen den lokalen ABS-Testserver lesen .env.test selbst (TestEnv). Gradle
// reicht nur den Pfad durch, damit keine Zugangsdaten im Configuration Cache landen.
val envTest = rootProject.layout.projectDirectory.file(".env.test")

tasks.withType<Test>().configureEach {
    inputs.files(envTest).optional().withPropertyName("envTest")
    systemProperty("shelfvr.envTest", envTest.asFile.absolutePath)
}
