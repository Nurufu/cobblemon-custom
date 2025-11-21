/*
 *
 *  * Copyright (C) 2023 Cobblemon Contributors
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import utilities.ACCESS_WIDENER

plugins {
    id("java")
    id("java-library")
    id("net.kyori.indra")
    id("net.kyori.indra.git")

    id("org.cadixdev.licenser")
    id("dev.architectury.loom")
    id("architectury-plugin")
    kotlin("jvm")
}

group = rootProject.group
version = rootProject.version
description = rootProject.description

indra {
    javaVersions {
        minimumToolchain(17)
        target(17)
    }
}

repositories {
    mavenCentral()
    //JEI
    maven("https://maven.blamejared.com/")
    maven("https://maven.tterrag.com/")
}

license {
    header(rootProject.file("HEADER"))
}

architectury {
    minecraft = project.property("mc_version").toString()
}

loom {
    silentMojangMappingsLicense()
    accessWidenerPath.set(project(":common").file(ACCESS_WIDENER))

    mixin {
        defaultRefmapName.set("cobblemon-${project.name}-refmap.json")
    }
}

dependencies {
    minecraft("net.minecraft:minecraft:${rootProject.property("mc_version")}")
    mappings("net.fabricmc:yarn:${rootProject.property("yarn_version")}:v2")
}

tasks {
    withType<JavaCompile> {
        options.compilerArgs.add("-Xlint:-processing,-classfile,-serial")
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    withType<Jar> {
        from(rootProject.file("LICENSE"))
    }
}
