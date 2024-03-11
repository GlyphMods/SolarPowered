@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.format.DateTimeFormatter

println("Java: ${System.getProperty("java.version")}, JVM: ${System.getProperty("java.vm.version")} (${System.getProperty("java.vendor")}), Arch: ${System.getProperty("os.arch")}")

object Properties {
    const val MC_VERSION = "1.20.1"
    const val MC_VERSION_RANGE = "[1.20.1]"
    const val FORGE_VERSION = "47.1.47"
    const val FORGE_VERSION_RANGE = "[47,)"
    const val KFF_VERSION = "4.7.0"

    const val MAPPING_CHANNEL = "parchment"
    const val MAPPING_VERSION = "1.20.1:2023.09.03"

    const val MOD_ID = "solarpowered"
    const val MOD_NAME = "Create: Solar Powered"
    const val MOD_VERSION = "0.1.0"
    const val MOD_AUTHORS = "Glyph Mods"
    const val MOD_DESCRIPTION = "Harnessing the power of the sun!"
    const val MOD_LICENSE = "Mozilla Public License 2.0"
    const val MOD_GROUP_ID = "io.github.glyphmods.solarpowered"

    const val CREATE_MC_VERSION = "1.20.1"
    const val FLYWHEEL_MC_VERSION = "1.20.1"
    const val CREATE_VERSION = "0.5.1.f-26"
    const val CREATE_VERSION_RANGE = "[0.5.1.e,)"
    const val FLYWHEEL_VERSION = "0.6.10-7"
    const val REGISTRATE_VERSION = "MC1.20-1.3.3"
}

plugins {
    id("idea")
    id("maven-publish")
    id("dev.architectury.loom") version "1.4-SNAPSHOT"
    id("org.jetbrains.kotlin.jvm") version "1.9.20"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.20"
}

base {
    archivesName.set(Properties.MOD_ID)
}
version = Properties.MOD_VERSION
group = Properties.MOD_GROUP_ID

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

loom {
    runs {
        register("data") {
            data()
            programArgs("--all", "--mod", Properties.MOD_ID,
                "--output", file("src/generated/resources/").absolutePath,
                "--existing", file("src/main/resources/").absolutePath)
        }
        all {
            vmArg("-XX:+AllowEnhancedClassRedefinition")
            property("fabric.log.level", "debug")
        }
    }
}

sourceSets {
    main {
        resources {
            srcDir("src/generated/resources/")
            exclude("*.cache/")
        }
    }
}

repositories {
    // Parchment
    maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }

    // Kotlin For Forge
    maven("https://thedarkcolour.github.io/KotlinForForge/") {
        name = "Kotlin for Forge"
        mavenContent {
            includeGroup("thedarkcolour")
        }
    }

    // Create
    maven("https://maven.tterrag.com/") { name = "tterrag maven" }
}

dependencies {
    minecraft("com.mojang:minecraft:${Properties.MC_VERSION}")

    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${Properties.MAPPING_VERSION}@zip")
    })

    forge("net.minecraftforge:forge:${Properties.MC_VERSION}-${Properties.FORGE_VERSION}")

    implementation("thedarkcolour:kotlinforforge:${Properties.KFF_VERSION}")

    modImplementation("com.simibubi.create:create-${Properties.CREATE_MC_VERSION}:${Properties.CREATE_VERSION}:slim") { isTransitive = false }
    modImplementation("com.jozufozu.flywheel:flywheel-forge-${Properties.FLYWHEEL_MC_VERSION}:${Properties.FLYWHEEL_VERSION}")
    modImplementation("com.tterrag.registrate:Registrate:${Properties.REGISTRATE_VERSION}")
}

val replaceProperties = mapOf(
        "minecraft_version" to Properties.MC_VERSION,
        "minecraft_version_range" to Properties.MC_VERSION_RANGE,
        "forge_version" to Properties.FORGE_VERSION,
        "forge_version_range" to Properties.FORGE_VERSION_RANGE,
        "mod_id" to Properties.MOD_ID,
        "mod_name" to Properties.MOD_NAME,
        "mod_license" to Properties.MOD_LICENSE,
        "mod_version" to Properties.MOD_VERSION,
        "mod_authors" to Properties.MOD_AUTHORS,
        "mod_description" to Properties.MOD_DESCRIPTION
)

tasks.processResources {
    inputs.properties(replaceProperties)

    filesMatching("META-INF/mods.toml") {
        expand(replaceProperties)
    }

    filesMatching("pack.mcmeta") {
        expand(replaceProperties)
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}

tasks.jar {
    manifest {
        attributes(mapOf(
                "Specification-Title"      to Properties.MOD_ID,
                "Specification-Vendor"     to Properties.MOD_AUTHORS,
                "Specification-Version"    to "1",
                "Implementation-Title"     to Properties.MOD_NAME,
                "Implementation-Version"   to Properties.MOD_VERSION,
                "Implementation-Vendor"    to Properties.MOD_AUTHORS,
                "Implementation-Timestamp" to DateTimeFormatter.ISO_DATE_TIME
        ))
    }
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
        }
    }
    repositories {
        maven("file://${project.projectDir}/mcmodsrepo")
    }
}
