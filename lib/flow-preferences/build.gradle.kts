plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "ru.radiationx.flowpreferences"
    compileSdk = libs.versions.app.compile.sdk.version.get().toInt()
    defaultConfig {
        minSdk = libs.versions.app.min.sdk.version.get().toInt()
    }
}
kotlin {
    jvmToolchain(17)
}
dependencies {
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.androidx.preference.ktx)
    testImplementation(libs.junit)
}