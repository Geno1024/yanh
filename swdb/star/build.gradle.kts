import g.buildsrc.CountTask
import java.util.jar.Attributes

plugins {
    kotlin("jvm")
    application
}

val runCountTask by tasks.register<CountTask.RunCountTask>("runCount")
val packCountTask by tasks.register<CountTask.PackCountTask>("packCount")

tasks.withType<JavaCompile> {
    dependsOn(runCountTask)
}

tasks.withType<Jar> {
    dependsOn(packCountTask)
    manifest {
        attributes(
            Attributes.Name.MANIFEST_VERSION.toString() to "1.0",
            Attributes.Name.MAIN_CLASS.toString() to application.mainClass,
            Attributes.Name.IMPLEMENTATION_VENDOR.toString() to "Geno1024"
        )
    }
}

group = "g.sw.star"
version = "0.0.${packCountTask.count}.${runCountTask.count}"

application {
    mainClass = "g.sw.Star"
}
