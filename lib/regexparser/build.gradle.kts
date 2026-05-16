plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "ru.radiationx.regexparser"
    compileSdk = libs.versions.app.compile.sdk.version.get().toInt()
    defaultConfig {
        minSdk = libs.versions.app.min.sdk.version.get().toInt()
    }
}
kotlin {
    jvmToolchain(17)
}
dependencies {
    testImplementation(libs.junit)
}