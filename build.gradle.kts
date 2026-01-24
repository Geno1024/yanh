plugins {
    kotlin("jvm") version "2.3.0"
    id("org.jetbrains.dokka") version "2.1.0" apply false
    id("org.jetbrains.dokka-javadoc") version "2.1.0" apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.4"
    id("test-report-aggregation")
}

dependencies {
    kover(project(":swdb:simpledb"))
    testReportAggregation(project(":swdb:simpledb"))
}

tasks.check {
    dependsOn(tasks.named<TestReport>("testAggregateTestReport"))
}
