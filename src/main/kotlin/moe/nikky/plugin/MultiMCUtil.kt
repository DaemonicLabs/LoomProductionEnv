package moe.nikky.plugin

import moe.nikky.plugin.extension.ProdExtension
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
//import org.gradle.api.file.
import org.gradle.api.artifacts.Configuration
import org.gradle.api.logging.Logger
import java.io.File
import java.util.SortedMap
import kotlin.system.exitProcess

object MultiMCUtil {
    lateinit var project: Project
    val logger: Logger get() = project.logger
    lateinit var multimcConfiguration: Configuration
    lateinit var extension: ProdExtension

    fun readCfg(cfgFile: File): SortedMap<String, String> =
        cfgFile.bufferedReader().useLines { lines ->
            lines.map { Pair(it.substringBefore('='), it.substringAfter('=')) }.toMap().toSortedMap()
        }

    fun writeCfg(cfgFile: File, properties: Map<String, String>) {
        cfgFile.createNewFile()
        cfgFile.writeText(
            properties.map { (key, value) -> "$key=$value" }
                .joinToString("\n")
        )
    }

    val binary: String
        get() = when {
            Os.isFamily(Os.FAMILY_WINDOWS) -> {
                logger.debug("executing 'where multimc'")
                val location = "where ${"multimc"}".runCommand().replace("\n", "")
                logger.debug("output: $location")
                val multimcBinary = File(location)
                if (!multimcBinary.exists()) {
                    logger.error("multimcBinary: '$multimcBinary'")
                    logger.error("Cannot find MultiMC on PATH")
                    logger.error("make sure to add the multimc install location to the PATH")
                    logger.error(
                        "go to `Control Panel\\All Control Panel Items\\System`" +
                                " >> Advanced system settings" +
                                " >> Environment Variables"
                    )
                    logger.info("once added restart the shell and try to execute `multimc`")
                    exitProcess(1)
                }
                multimcBinary.absolutePath
            }
            Os.isFamily(Os.FAMILY_UNIX) -> {
                val location = "which multimc".runCommand().replace("\n", "")
                logger.lifecycle("location: '$location'")
                if (File(location).exists()) {
                    "multimc"
                } else {
                    File(System.getProperty("user.home"))
                        .resolve(".local")
                        .resolve("share")
                        .resolve("multimc")
                        .resolve("MultiMC")
                        .absolutePath
                }
            }
            else -> {
                logger.warn("unsupported platform, on OSX please contact NikkyAi to implement this OR make a PR")
                throw Exception("unsupported platform, on OSX please contact NikkyAi to implement this OR make a PR")
                //File(System.getProperty("user.home")).resolve(mmcConfig.path)
            }
        }

    /**
     * Finds the MultiMC data loccation
     */
    fun findDir(): File {
        logger.info("os.name: ${System.getProperty("os.name")}")
        return when {
            Os.isFamily(Os.FAMILY_WINDOWS) -> {
                File(binary).parentFile
            }
            Os.isFamily(Os.FAMILY_UNIX) -> {
                File(System.getProperty("user.home"))
                    .resolve(".local")
                    .resolve("share")
                    .resolve("multimc")
            }
            else -> {
                logger.warn("unsupported platform, on OSX please contact NikkyAi to implement this OR make a PR")
                throw Exception("unsupported platform, on OSX please contact NikkyAi to implement this OR make a PR")
                //File(System.getProperty("user.home")).resolve(mmcConfig.path)
            }
        }
    }

    val files: List<File>
        get() = extension.multiMCExtension.configurations.flatMap { configuration ->
            configuration.resolvedConfiguration
                .getFiles {
                    !(it.group == "net.fabricmc" && it.name == "fabric-loader")
                }.filter {
                    it.contains("fabric.mod.json")
                }
        }
}