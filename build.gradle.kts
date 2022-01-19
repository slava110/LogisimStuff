plugins {
    java
}

group = "com.slava_110.logisimstuff"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    implementation("org.jetbrains:annotations:16.0.2")
    compileOnly(files("run/logisim.jar"))

    implementation("io.netty:netty-transport:4.1.72.Final")
    implementation("io.netty:netty-handler:4.1.72.Final")

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

val fatJar = task("fatJar", type = Jar::class) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Library-Class"] = "com.slava_110.logisimstuff.LogisimStuff"
    }
    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory)
            it
        else
            zipTree(it)
    })
    with(tasks["jar"] as CopySpec)
}