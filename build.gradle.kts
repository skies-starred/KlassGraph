plugins {
    kotlin("jvm") version "2.4.10"
    `maven-publish`
}

val projectName = "klassgraph"
val projectGroup = "foo.starred"
val projectVersion = "2026.10.1"

group = projectGroup
version = projectVersion

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://repo.spongepowered.org/maven/")
}

dependencies {
    compileOnly("org.ow2.asm:asm:9.10.1")
    compileOnly("org.ow2.asm:asm-tree:9.10.1")
    compileOnly("org.spongepowered:mixin:0.8.7")
    compileOnly("net.fabricmc:fabric-loader:0.19.3")
}

publishing {
    repositories {
        val a = if (Regex("-b[0-9]*$") in projectVersion) "snapshots" else "releases"
        maven("https://maven.starred.foo/$a") {
            name = "starred"
            credentials {
                username = (project.findProperty("MAVEN_USER") as? String) ?: System.getenv("MAVEN_USER") ?: ""
                password = (project.findProperty("MAVEN_PASS") as? String) ?: System.getenv("MAVEN_PASS") ?: ""
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = projectGroup
            artifactId = projectName
            version = projectVersion
            from(components["java"])
        }
    }
}

kotlin {
    jvmToolchain(21)
}

java {
    withSourcesJar()
}
