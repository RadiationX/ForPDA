plugins {
    alias(libs.plugins.android.library)
    id("kotlin-parcelize")
}

android {
    namespace = "ru.radiationx.coretypes"
    compileSdk = libs.versions.app.compile.sdk.version.get().toInt()
    defaultConfig {
        minSdk = libs.versions.app.min.sdk.version.get().toInt()
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
}
