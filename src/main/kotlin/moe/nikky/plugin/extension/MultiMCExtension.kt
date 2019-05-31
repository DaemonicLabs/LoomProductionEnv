package moe.nikky.plugin.extension

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.BasePluginConvention
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getPluginByName
import java.io.File

open class MultiMCExtension(
    private val project: Project,
    private val multimcConfiguration: Configuration
) {
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

    var instanceId = project.name
    var instanceName = "${project.displayName} Test Instance"

    /**
     * configurations that will copy dependencies into the multimc instance
     */
    var configurations: MutableList<Configuration> = listOfNotNull(
        multimcConfiguration,
        project.configurations["modCompile"]
    ).toMutableList()
}