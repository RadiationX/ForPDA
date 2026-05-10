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
        maven("https://github.com/wada811/Android-Material-Design-Colors/raw/master/repository/")
    }
}

include(":app")
