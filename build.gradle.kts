plugins {
    kotlin("jvm") version "2.3.0"
    id("org.jetbrains.dokka") version "2.1.0"
    id("org.jetbrains.dokka-javadoc") version "2.1.0"
    id("org.jetbrains.kotlinx.kover") version "0.9.4"
    id("test-report-aggregation")
}

dependencies {
    listOf(
        project(":swdb:simpledb"),
        project(":swdb:star")
    ).forEach {
        dokka(it)
        kover(it)
        testReportAggregation(it)
    }
}

tasks.check {
    dependsOn(tasks.named<TestReport>("testAggregateTestReport"))
}
