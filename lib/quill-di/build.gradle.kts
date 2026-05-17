plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "ru.radiationx.quill"
    compileSdk = libs.versions.app.compile.sdk.version.get().toInt()
    defaultConfig {
        minSdk = libs.versions.app.min.sdk.version.get().toInt()
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.recyclerview)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.toothpick.runtime)
    ksp(libs.toothpick.compiler)

    api(libs.javax.inject)
}
