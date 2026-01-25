import g.buildsrc.CountTask
import java.util.jar.Attributes

plugins {
    kotlin("jvm")
    application
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
    id("org.jetbrains.kotlinx.kover")
}

val runCountTask by tasks.register<CountTask.RunCountTask>("runCount")
val packCountTask by tasks.register<CountTask.PackCountTask>("packCount")

val dokkaHtmlJar by tasks.register<Jar>("dokkaHtmlJar") {
    group = "documentation"
    from(tasks.dokkaGeneratePublicationHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("htmldoc")
}

val dokkaJavadocJar by tasks.register<Jar>("dokkaJavadocJar") {
    group = "documentation"
    from(tasks.dokkaGeneratePublicationJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(runCountTask)
}

tasks.named<Jar>("jar") {
    dependsOn(packCountTask)
    dependsOn(tasks.getByName("kotlinSourcesJar"))
    dependsOn(tasks.dokkaGenerate)
    manifest {
        attributes(
            Attributes.Name.MANIFEST_VERSION.toString() to "1.0",
            Attributes.Name.MAIN_CLASS.toString() to application.mainClass,
            Attributes.Name.IMPLEMENTATION_VENDOR.toString() to "Geno1024"
        )
    }
    finalizedBy(dokkaJavadocJar, dokkaHtmlJar)
}

group = "g.sw.star"
version = "0.0.${packCountTask.count}.${runCountTask.count}"

application {
    mainClass = "g.sw.Star"
}
