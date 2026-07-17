pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "Yanh"

include(":swdb:simpledb")
include(":swdb:star")
include(":swdb:star-android")
