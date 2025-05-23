plugins {
    // Apply the common convention plugin for shared build configuration between library and application projects.
    id("buildlogic.kotlin-common-conventions")

    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

tasks.withType<JavaExec> {
    systemProperty("rootProjectDir", rootProject.projectDir.absolutePath)
}
