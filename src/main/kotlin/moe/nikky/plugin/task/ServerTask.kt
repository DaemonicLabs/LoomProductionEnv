package moe.nikky.plugin.task

import kotlinx.coroutines.runBlocking
import moe.nikky.plugin.LoomUtil
import moe.nikky.plugin.extension.ProdExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByName
import java.net.URL
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import moe.nikky.plugin.MultiMCUtil
import moe.nikky.plugin.PackageVersion
import moe.nikky.plugin.VersionManifest
import moe.nikky.plugin.contains
import moe.nikky.plugin.logErr
import moe.nikky.plugin.logStdout
import org.gradle.api.GradleException
import java.io.File

open class ServerTask : DefaultTask() {
    init {
        group = "prodenv"
        description = "Server production environment"
    }

    private val files: List<File>
        get() = MultiMCUtil.extension.serverExtension.configurations.flatMap { configuration ->
            configuration.resolvedConfiguration
                .getFiles {
                    !(it.group == "net.fabricmc" && it.name == "fabric-loader")
                }
        }

    @TaskAction
    fun exec() {
        val extension: ProdExtension = project.extensions.getByName<ProdExtension>("production")
        val serverExtension = extension.serverExtension

        // get yarn and loader version
        val yarn = LoomUtil.yarn
        logger.lifecycle("yarn: $yarn")
        val loaderVersion = LoomUtil.loaderVersion
        logger.lifecycle("loader: $loaderVersion")

        val minecraftVersion = LoomUtil.minecraftVersion
        logger.lifecycle("minecraftVersion: $minecraftVersion")

        // TODO: get multimc instance directory
        val serverRoot = serverExtension.workingDirectory
        serverRoot.mkdirs()

        logger.lifecycle("serverRoot: $serverRoot")

        logger.lifecycle("installerVersion: ${serverExtension.installerVersion}")

        // downloading installer: https://maven.modmuss50.me/net/fabricmc/fabric-installer/0.4.2.27/fabric-installer-0.4.2.27.jar
        val installerURL = "https://maven.modmuss50.me/net/fabricmc/fabric-installer/${serverExtension.installerVersion}/fabric-installer-${serverExtension.installerVersion}.jar"
        logger.lifecycle("downloading installer: $installerURL")
        val installerFile = serverRoot.resolve("fabric-installer-${serverExtension.installerVersion}.jar")
        URL(installerURL).readBytes().let {
            installerFile.writeBytes(it)
        }

        // TODO: add kotlinx.serialization

        // TODO: download minecraft server
        //  https://launchermeta.mojang.com/mc/game/version_manifest.json
        //  https://launchermeta.mojang.com/v1/packages/8b96abed06b23d3b752e653ada36062d70bf3da1/1.14.2.json

        val versionManifestText = URL("https://launchermeta.mojang.com/mc/game/version_manifest.json").readText()
        val json: Json = Json(JsonConfiguration.Stable.copy(strictMode = false))
        val versionManifest = json.parse(VersionManifest.serializer(), versionManifestText)

        val packageUrl = versionManifest.versions
            .find { it.id == minecraftVersion }
            ?.url
            ?: throw GradleException("cannot find $minecraftVersion in ${versionManifest.versions.map { it.id }}")

        val packageText = URL(packageUrl).readText()
        val versionPackage = json.parse(PackageVersion.serializer(), packageText)
        val download = versionPackage.downloads.getValue("server")

        val vanillaServerjar = serverRoot.resolve("server.jar")
        vanillaServerjar.writeBytes(
            URL(download.url).readBytes()
        )

        // TODO: execute installer
        val installProcess = ProcessBuilder(
            "java",
            "-jar",
            installerFile.absolutePath,
            "server",
            "-dir", serverRoot.absolutePath,
            "-mappings", yarn,
            "-loader", loaderVersion
        )
            .directory(serverRoot)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE).also {
                logger.lifecycle("executing: ${it.command().joinToString(" ")}")
            }
            .start()

        val status = runBlocking {
            logStdout(installProcess, logger)
            logErr(installProcess, logger)
            installProcess.waitFor()
        }

        logger.lifecycle("server instance exited with code $status")

        val fabricServerLaunch = serverRoot.resolve("fabric-server-launch.jar")

        val modsDir = serverRoot.resolve("mods").apply {
            deleteRecursively()
            mkdirs()
        }
        val mods = serverExtension.configurations.flatMap { configuration ->
            configuration.resolvedConfiguration
                .getFiles {
                    !(it.group == "net.fabricmc" && it.name == "fabric-loader")
                }.filter {
                    it.contains("fabric.mod.json")
                }
        }
        logger.lifecycle("mods:")
        mods.forEach {
            logger.lifecycle(" - $it")
            val targetFile = modsDir.resolve(it.name)
            it.copyTo(targetFile, overwrite = true)
        }

        val eulaFile = serverRoot.resolve("eula.txt")
        eulaFile.writeText("eula=true\n")

        val serverProcess = ProcessBuilder(
            "java",
            *serverExtension.jvmArgs.toTypedArray(),
            "-Xmx${serverExtension.Xmx}",
            "-jar",
            fabricServerLaunch.absolutePath,
            *listOfNotNull(
                if(serverExtension.gui) null else "--nogui"
            ).toTypedArray(),
            *serverExtension.arguments.toTypedArray()
        // TODO: extra args
        ).also {
            logger.lifecycle("executing: ${it.command().joinToString(" ")}")
        }
            .directory(serverRoot)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        val serverStatus = runBlocking {
            logStdout(serverProcess, logger)
            logErr(serverProcess, logger)
            serverProcess.waitFor()
        }

        logger.lifecycle("server exited with status code: $serverStatus")
    }
}