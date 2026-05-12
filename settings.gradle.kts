pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://dl.bintray.com/patrickfav/maven/")
        maven("https://jitpack.io")
    }
}

include(":app")
