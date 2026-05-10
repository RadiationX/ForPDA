import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kapt)
    alias(libs.plugins.realm)
}

fun getDateTime(): String {
    val df: DateFormat = SimpleDateFormat("dd MMMMM yyyy")
    return df.format(Date()) + " г."
}

val keystoreProperties = Properties().apply {
    load(rootProject.file("keystore.properties").inputStream())
}

android {
    val versionPropsFile = file("version.properties")
    val versionProps: Properties = Properties().apply {
        load(versionPropsFile.inputStream())
    }
    val versionBuild = versionProps.getProperty("VERSION_BUILD", "-1").toInt() + 1
    versionProps.setProperty("VERSION_BUILD", versionBuild.toString())
    versionProps.setProperty("DATE_BUILD", getDateTime())
    versionProps.store(versionPropsFile.writer(), null)

    val versionNumber = 223
    val fileVersionName = "1.0.1"
    val baseVersionName = "$fileVersionName ($versionBuild)"

    namespace = "forpdateam.ru.forpda"

    compileSdk = 36

    defaultConfig {
        applicationId = "ru.forpdateam.forpda"
        versionCode = versionNumber
        versionName = baseVersionName
        minSdk = 23
        targetSdk = 36
        vectorDrawables.useSupportLibrary = true
        buildConfigField("String", "BUILD_DATE", "\"${getDateTime()}\"")
    }

    signingConfigs {
        getByName("debug") {
            storeFile = rootProject.file(keystoreProperties.getProperty("DEBUG_STORE_FILE"))
            storePassword = keystoreProperties.getProperty("DEBUG_STORE_PASSWORD")
            keyAlias = keystoreProperties.getProperty("DEBUG_KEY_ALIAS")
            keyPassword = keystoreProperties.getProperty("DEBUG_KEY_PASSWORD")
        }
        create("release") {
            storeFile = rootProject.file(keystoreProperties.getProperty("DEBUG_STORE_FILE"))
            storePassword = keystoreProperties.getProperty("DEBUG_STORE_PASSWORD")
            keyAlias = keystoreProperties.getProperty("DEBUG_KEY_ALIAS")
            keyPassword = keystoreProperties.getProperty("DEBUG_KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
            ndk {
                abiFilters += listOf("x86_64", "arm64-v8a")
            }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
            ndk {
                abiFilters += listOf("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
            }
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
            versionCode = versionNumber
            versionName = "$baseVersionName beta"
        }

        create("dev") {
            dimension = "type"
            applicationIdSuffix = ".debug"
            versionCode = versionNumber
            versionName = "$baseVersionName dev"
        }
    }

    applicationVariants.configureEach {
        outputs.configureEach {
            this as BaseVariantOutputImpl
            outputFileName = "ForPDA-${fileVersionName}.apk"
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
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.kotlin.coroutines.core)

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
    //implementation "com.lapism:searchview:4.0"
    implementation(libs.atv)
    implementation(libs.sectioned.recyclerview)

    implementation(libs.photoview)
    implementation(libs.android.material.design.colors)
    implementation(libs.spectrum)
    implementation(libs.android.simple.tooltip)
    implementation(libs.pagerbullet)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.moxy)
    implementation(libs.moxy.androidx)
    kapt(libs.moxy.compiler)

    implementation(libs.kotlin.stdlib)

    implementation(libs.cicerone)
    implementation(libs.adapterdelegates3)
    implementation(libs.easinginterpolator)

    implementation(libs.realm.base)

    implementation(libs.permissionsdispatcher)
    kapt(libs.permissionsdispatcher.processor)

    implementation(libs.appmetrica)

    implementation(libs.roundedimageview)
    implementation(libs.viewbindingpropertydelegate)
}