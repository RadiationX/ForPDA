import java.io.FileInputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kapt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.appmetrica)
    alias(libs.plugins.kotlin.serialization)
}

fun getDateTime(): String {
    val df: DateFormat = SimpleDateFormat("dd MMMMM yyyy")
    return df.format(Date()) + " г."
}

fun getKeystoreProperties(buildType: String): Properties {
    var file = rootProject.file("signing/${buildType}.properties")
    if (!file.exists()) {
        logger.error("Signing properties for build type '$buildType' not exists. Fallback to debug properties.")
        file = rootProject.file("signing/debug.properties")
    }
    return Properties().apply { load(file.inputStream()) }
}

base {
    archivesName = "ForPDA-${libs.versions.app.version.name.get()}"
}

android {
    namespace = "forpdateam.ru.forpda"

    compileSdk = libs.versions.app.compile.sdk.version.get().toInt()

    defaultConfig {
        applicationId = "ru.forpdateam.forpda"
        minSdk = libs.versions.app.min.sdk.version.get().toInt()
        targetSdk = libs.versions.app.target.sdk.version.get().toInt()
        versionCode = libs.versions.app.version.code.get().toInt()
        versionName = libs.versions.app.version.name.get()
        vectorDrawables.useSupportLibrary = true
        buildConfigField("String", "BUILD_DATE", "\"${getDateTime()}\"")
    }

    signingConfigs {
        getByName("debug") {
            val keystoreProperties = getKeystoreProperties(name)
            storeFile = rootProject.file(keystoreProperties.getProperty("STORE_FILE"))
            storePassword = keystoreProperties.getProperty("STORE_PASSWORD")
            keyAlias = keystoreProperties.getProperty("KEY_ALIAS")
            keyPassword = keystoreProperties.getProperty("KEY_PASSWORD")
        }
        create("release") {
            val keystoreProperties = getKeystoreProperties(name)
            storeFile = rootProject.file(keystoreProperties.getProperty("STORE_FILE"))
            storePassword = keystoreProperties.getProperty("STORE_PASSWORD")
            keyAlias = keystoreProperties.getProperty("KEY_ALIAS")
            keyPassword = keystoreProperties.getProperty("KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
        }
    }

    flavorDimensions += listOf("type")
    productFlavors {
        create("stable") {
            dimension = "type"
        }
        create("beta") {
            dimension = "type"
            applicationIdSuffix = ".beta"
        }
    }

    packaging {
        resources {
            excludes += listOf(
                "META-INF/DEPENDENCIES.txt",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE.txt",
                "META-INF/NOTICE",
                "META-INF/LICENSE",
                "META-INF/DEPENDENCIES",
                "META-INF/notice.txt",
                "META-INF/license.txt",
                "META-INF/dependencies.txt"
            )
        }
        jniLibs.useLegacyPackaging = true
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    lint {
        disable += "UseCompatLoadingForDrawables"
    }
}

kotlin {
    jvmToolchain(17)
}

room {
    schemaDirectory("$projectDir/schemas")
}

appmetrica {
    val localProperties = Properties().apply {
        load(FileInputStream(rootProject.file("local.properties")))
    }
    val propApiKey = localProperties.getProperty("appmetrica_post_api_key", "")
    postApiKey.set(propApiKey)
}

dependencies {
    implementation(project(":lib:regexparser"))

    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.preference.ktx)

    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.okhttp.coroutines)
    implementation(libs.okhttp.urlconnection)
    implementation(libs.okhttp.logging.interceptor)
    //implementation "com.annimon:stream:1.1.4"
    implementation(libs.universal.image.loader)
    implementation(libs.circularprogressview)
    implementation(libs.circleimageview)
    implementation(libs.tagsoup)
    implementation(libs.minitemplator.repackaged)

    implementation(libs.photoview)
    implementation(libs.pagerbullet)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.moxy)
    implementation(libs.moxy.androidx)
    kapt(libs.moxy.compiler)

    implementation(libs.cicerone)
    implementation(libs.adapterdelegates4)
    implementation(libs.easinginterpolator)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.mintpermissions)
    //implementation(libs.mintpermissions.flows)

    implementation(libs.appmetrica)

    implementation(libs.roundedimageview)
    implementation(libs.viewbindingpropertydelegate)
}