plugins {
    kotlin("jvm") version "2.3.0" apply false
    id("org.jetbrains.dokka") version "2.1.0" apply false
    id("org.jetbrains.dokka-javadoc") version "2.1.0" apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.4"
}

dependencies {
    kover(project(":swdb:simpledb"))
}
