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
        maven("https://jitpack.io")
    }
}

include(":app")
include(":lib:regexparser")
include(":lib:quill-di")
include(":lib:flow-preferences")
include(":modules:analytics")
include(":modules:core-types")
include(":modules:links")
