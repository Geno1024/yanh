import g.buildsrc.CountTask

plugins {
    kotlin("jvm")
}

val runCountTask by tasks.register<CountTask.RunCountTask>("runCount")
val packCountTask by tasks.register<CountTask.PackCountTask>("packCount")

tasks.withType<JavaCompile> {
    dependsOn(runCountTask)
}

tasks.withType<Jar> {
    dependsOn(packCountTask)
}

group = "g.sw.star"
version = "0.0.${packCountTask.count}.${runCountTask.count}"
