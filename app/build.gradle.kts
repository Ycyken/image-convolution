plugins {
    id("buildlogic.kotlin-application-conventions")
    id("me.champeau.jmh") version "0.7.3"
}

dependencies {
    implementation(project(":convolution"))
    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.37")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.6")
}

application {
    mainClass = "app.AppKt"
}

jmh {
    jvmArgs = listOf("-DrootProjectDir=${rootProject.projectDir.absolutePath}")
}
