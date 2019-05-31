package moe.nikky.plugin

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.kotlin.dsl.get
import java.io.File

object LoomUtil {
    lateinit var project: Project
//    lateinit var multimcConfiguration: Configuration

    val yarn: String
        get() = project.configurations["mappings"]
                .resolvedConfiguration
                .firstLevelModuleDependencies
                .first()
                .moduleVersion

    val minecraftVersion : String
    get() = yarn.substringBefore("+")

    val loaderVersion: String
        get() = project.configurations["modCompile"]
        .resolvedConfiguration
        .firstLevelModuleDependencies
        .first {
            it.moduleGroup == "net.fabricmc" && it.moduleName == "fabric-loader"
        }
        .moduleVersion
}