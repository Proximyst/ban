package com.proximyst.ban

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

class BanGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create<BanExtension>("ban")
    }
}