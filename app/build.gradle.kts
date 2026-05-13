import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kapt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

fun getDateTime(): String {
    val df: DateFormat = SimpleDateFormat("dd MMMMM yyyy")
    return df.format(Date()) + " г."
}

val keystoreProperties = Properties().apply {
    load(rootProject.file("keystore.properties").inputStream())
}

val versionPropsFile = file("version.properties")
val versionProps: Properties = Properties().apply {
    load(versionPropsFile.inputStream())
}
val versionBuild = versionProps.getProperty("VERSION_BUILD", "-1").toInt() + 1
val versionDate = getDateTime()
versionProps.setProperty("VERSION_BUILD", versionBuild.toString())
versionProps.setProperty("DATE_BUILD", versionDate)
versionProps.store(versionPropsFile.writer(), null)

val versionNumber = 223
val fileVersionName = "1.0.1"
val baseVersionName = "$fileVersionName ($versionBuild)"

base {
    archivesName = "ForPDA-${fileVersionName}"
}

android {
    namespace = "forpdateam.ru.forpda"

    compileSdk = 37

    defaultConfig {
        applicationId = "ru.forpdateam.forpda"
        versionCode = versionNumber
        versionName = baseVersionName
        minSdk = 23
        targetSdk = 37
        vectorDrawables.useSupportLibrary = true
        buildConfigField("String", "BUILD_DATE", "\"${versionDate}\"")
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

room {
    schemaDirectory("$projectDir/schemas")
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

    implementation(libs.permissionsdispatcher)
    kapt(libs.permissionsdispatcher.processor)

    implementation(libs.appmetrica)

    implementation(libs.roundedimageview)
    implementation(libs.viewbindingpropertydelegate)
}