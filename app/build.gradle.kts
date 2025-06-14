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

tasks.register<JavaExec>("jmhConv") {
    group = "benchmark"
    description = "Run JMH convolution benchmark"
    classpath = files(tasks.named("jmhJar"))
    mainClass.set("bench.convolution.BenchConvolutionKt")
}

tasks.register<JavaExec>("jmhPipe") {
    group = "benchmark"
    description = "Run JMH pipeline benchmark"
    classpath = files(tasks.named("jmhJar"))
    mainClass.set("bench.pipeline.BenchPipelineKt")
}
