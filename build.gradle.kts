subprojects {
    afterEvaluate {
        (extensions.findByName("sourceSets") as? SourceSetContainer)
            ?.named("main") {
                resources.srcDir(rootDir.resolve("images"))
            }
    }
}