import g.buildsrc.BuildCount

plugins {
    kotlin("jvm")
}

// <editor-fold desc="Build Count">
val run = BuildCount(project, "run")

val runCount = tasks.register("runCount") {
    group = "buildCount"
    doLast {
        run.inc()
    }
}

val jar = BuildCount(project, "jar")

val jarCount = tasks.register("jarCount") {
    group = "buildCount"
    doLast {
        jar.inc()
    }
}

tasks.withType<JavaCompile> {
    dependsOn(runCount)
}

tasks.withType<Jar> {
    dependsOn(jarCount)
}
// </editor-fold>
