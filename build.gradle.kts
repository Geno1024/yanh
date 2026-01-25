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

tasks.register("generateDownloadPagesForGitHubPages") {
    doLast {
        File("$rootDir/pages/download.html").writeText("""
            <!doctype html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>Yanh :: Downloads</title>
            </head>
            <body>
            <table style="width: 100%">
                <thead>
                    <tr><th>Name</th><th>Size</th><th>Version</th></tr>
                </thead>
                <tbody>
                    ${File("$rootDir/pages/jars").listFiles().joinToString(separator = "\n") {
                        "<tr><td>${it.name}</td><td>${it.length()}</td><td></td></tr>"
                    }}
                </tbody>
            </table>
            </body>
            </html>

        """.trimMargin())
    }
}
