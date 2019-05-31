package moe.nikky.plugin

import moe.nikky.plugin.extension.ProdExtension
import moe.nikky.plugin.task.MultiMCTask
import moe.nikky.plugin.task.ServerTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

open class LoomProdPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        LoomUtil.project = project
        MultiMCUtil.project = project

        val multimcConfiguration = project.configurations.create("multimc")
        val serverConfiguration = project.configurations.create("server")
//        LoomUtil.multimcConfiguration = multimcConfiguration
        MultiMCUtil.multimcConfiguration = multimcConfiguration

        project.pluginManager.apply("fabric-loom")
        project.pluginManager.apply("java")

        val extension = project.extensions.create<ProdExtension>(
            "production",
            project,
            multimcConfiguration,
            serverConfiguration
        )
        MultiMCUtil.extension = extension

        project.afterEvaluate {

            val multiMCTask = project.tasks.create<MultiMCTask>("multimc")
            val serverTask = project.tasks.create<ServerTask>("server")

        }
    }
}