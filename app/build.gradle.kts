plugins {
    id("buildlogic.kotlin-application-conventions")
}

dependencies {
    implementation(project(":convolution"))
}

application {
    mainClass = "app.AppKt"
}
