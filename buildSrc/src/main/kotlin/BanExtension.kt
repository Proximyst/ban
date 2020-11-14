package com.proximyst.ban

import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType

val Project.ban: BanExtension
    get() = rootProject.extensions.findByType()
        ?: throw IllegalStateException("ban's gradle plugin is not applied to the project")

fun Project.ban(block: BanExtension.() -> Unit): BanExtension {
    return ban.also(block)
}

open class BanExtension {
    var relocations = emptySet<String>() as Collection<String>
    var javadocLinks = emptySet<String>() as Collection<String>
}