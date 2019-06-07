package moe.nikky.plugin.extension

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.BasePluginConvention
import org.gradle.kotlin.dsl.getPluginByName
import java.io.File

open class ProdExtension(
    private val project: Project,
    multimcConfiguration: Configuration,
    serverConfiguration: Configuration
) {
    internal val multiMCExtension: MultiMCExtension =
        MultiMCExtension(project, multimcConfiguration)
    fun multimc(configure: Action<MultiMCExtension>): MultiMCExtension {
        return multiMCExtension.apply {
            configure.execute(this)
        }
    }
    internal val serverExtension: ServerExtension =
        ServerExtension(project, serverConfiguration)
    fun server(configure: Action<ServerExtension>): ServerExtension {
        return serverExtension.apply {
            configure.execute(this)
        }
    }

    private val base: BasePluginConvention
        get() = project.convention.getPluginByName<org.gradle.api.plugins.BasePluginConvention>("base")

    private var mainJarProperty: File? = null
    /**
     * expects a `File` pointing the the remapped jar
     */
    var mainJar: File
        get() = mainJarProperty ?: project.file("${project.buildDir}/libs/${base.archivesBaseName}-${project.version}.jar")
        set(value) {
            mainJarProperty = value
        }

    val buildTasks: MutableList<Task> = mutableListOf()
}