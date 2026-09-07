plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.3.1"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.2.1")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

tasks {
    shadowJar {
        configurations = listOf(project.configurations.runtimeClasspath.get())

        relocate("org.bstats", "${project.group}.bstats")
    }

    processResources {
        val props = mapOf("version" to version, "description" to project.description)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
