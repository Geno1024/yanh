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

val jarCount = BuildCount(project, "jar")

val jar = tasks.register("jarCount") {
    group = "buildCount"
    doLast {
        jarCount.inc()
    }
}

tasks.withType<JavaCompile> {
    dependsOn(run)
}

tasks.withType<Jar> {
    dependsOn(jar)
}
// </editor-fold>
