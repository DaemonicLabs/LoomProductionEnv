package moe.nikky.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

open class LoomProdPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        LoomUtil.project = project
        MultiMCUtil.project = project

        val multimcConfiguration = project.configurations.create("multimc")
//        LoomUtil.multimcConfiguration = multimcConfiguration
        MultiMCUtil.multimcConfiguration = multimcConfiguration

        project.pluginManager.apply("fabric-loom")
        project.pluginManager.apply("java")

        val multiMCExtension = project.extensions.create<MultiMCExtension>(
            "multimc",
            project,
            multimcConfiguration
        )
        MultiMCUtil.multiMCExtension = multiMCExtension


        val multiMCTask = project.tasks.create<MultiMCTask>("multimc")


        project.afterEvaluate {

        }
    }
}