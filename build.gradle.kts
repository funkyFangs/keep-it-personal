import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/*================*\
 ‖  Dependencies  ‖
\*================*/

/*-----------*\
 |  Plugins  |
\*-----------*/

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.fabric.loom)
    id("maven-publish")
}

/*----------------*\
 |  Repositories  |
\*----------------*/

repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
}

/*----------------*\
 |  Dependencies  |
\*----------------*/

dependencies {
    // Fabric
    modImplementation(libs.fabric.api)
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.permissions.api)

    // Jackson
    implementation(libs.jackson.dataformat.toml)

    // Jakarta
    compileOnly(libs.jakarta.annotation.api)

    // Minecraft
    minecraft(libs.minecraft)

    // Yarn
    mappings("${libs.yarn.get()}:v2")

    // Included Dependencies
    for (dependency in libs.bundles.included.get()) {
        include(dependency)
    }
}

/*===============*\
 ‖  Compilation  ‖
\*===============*/

/*-------------*\
 |  Resources  |
\*-------------*/

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", libs.versions.minecraft.get())
    inputs.property("loader_version", libs.versions.fabric.loader.get())
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to libs.versions.project.get(),
            "minecraft_version" to libs.versions.minecraft.get(),
            "loader_version" to libs.versions.fabric.loader.get(),
            "fabric_permissions_api_version" to libs.versions.fabric.permissions.api.get()
        )
    }
}

/*--------*\
 |  Java  |
\*--------*/

val targetJavaVersion = Integer.parseInt(libs.versions.java.get())
java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    // ensure that the encoding is set to UTF-8, no matter what the system default is
    // this fixes some edge cases with special characters not displaying correctly
    // see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
    // If Javadoc is generated, this must be specified in that task too.
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

/*----------*\
 |  Kotlin  |
\*----------*/

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(targetJavaVersion.toString()))
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName}" }
    }
}

/*------------*\
 |  Artifact  |
\*------------*/

version = libs.versions.project.get()
val projectName: String by project

base {
    archivesName.set(projectName)
}

loom {
    serverOnlyMinecraftJar()
    splitEnvironmentSourceSets()

    mods {
        register(projectName) {
            sourceSet("main")
        }
    }
}

/*==============*\
 ‖  Publishing  ‖
\*==============*/

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = projectName
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}
