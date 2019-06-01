package moe.nikky.plugin.task

import kotlinx.coroutines.runBlocking
import moe.nikky.plugin.LoomUtil
import moe.nikky.plugin.MultiMCUtil
import moe.nikky.plugin.extension.ProdExtension
import moe.nikky.plugin.logErr
import moe.nikky.plugin.logStdout
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByName
import java.io.File
import java.net.URL

open class MultiMCTask : DefaultTask() {
    init {
        group = "prodenv"
        description = "MultiMC production environment"
    }

    @TaskAction
    fun exec() {
        val extension: ProdExtension = project.extensions.getByName<ProdExtension>("production")
        val multiMCExtension = extension.multiMCExtension

        // get yarn and loader version
        val yarn = LoomUtil.yarn
        logger.lifecycle("yarn: $yarn")
        val loaderVersion = LoomUtil.loaderVersion
        logger.lifecycle("loader: $loaderVersion")

        val minecraftVersion = LoomUtil.minecraftVersion
        logger.lifecycle("minecraftVersion: $minecraftVersion")

        // TODO: get multimc instance directory
        val multimcRoot = project.properties["multimcRoot"]?.let { File(it as String) }
            ?: MultiMCUtil.findDir()

        logger.lifecycle("multimcRoot: $multimcRoot")

        val cfgFile = multimcRoot.resolve("multimc.cfg")
        val cfgMap = MultiMCUtil.readCfg(cfgFile)

        val instancesDir = multimcRoot.resolve(cfgMap["InstanceDir"] ?: "instances")
        logger.lifecycle("instancesDir: $instancesDir")

        // TODO: download // https://fabricmc.net/download/multimc/?yarn=${yarn}&loader=${loader}&format=patchJson
        val fabricPatchUrl =
            "http://fabricmc.net/download/multimc/?yarn=${yarn}&loader=${loaderVersion}&format=patchJson".replace("+", "%2B")
        logger.lifecycle("downloading: $fabricPatchUrl")
        val jsonPatch = URL(fabricPatchUrl).readText()
        logger.info("patch: $jsonPatch")

        // TODO: create multimc instance or update
        val instanceDir = instancesDir.resolve(multiMCExtension.instanceId)
        instancesDir.mkdirs()
        val minecraftDir = instanceDir.resolve(".minecraft")

        val patchFile = instanceDir.resolve("patches").also {
            it.mkdirs()
        }.resolve("net.fabricmc.json")

        patchFile.writeText(jsonPatch)

        val mmcPackFile = instanceDir.resolve("mmc-pack.json")
        mmcPackFile.writeText(
            """
            |{
            |    "formatVersion": 1,
            |    "components": [
            |        {
            |            "important": true,
            |            "uid": "net.minecraft",
            |            "version": "$minecraftVersion"
            |        },
            |        {
            |            "uid": "net.fabricmc"
            |        }
            |    ]
            |}
        """.trimMargin()
        )

        val instanceCfgFile = instanceDir.resolve("instance.cfg")
        val instanceCfg = if (instanceCfgFile.exists())
            MultiMCUtil.readCfg(instanceCfgFile)
        else
            sortedMapOf<String, String>()

        instanceCfg["InstanceType"] = "OneSix"
        instanceCfg["name"] = multiMCExtension.instanceName
        instanceCfg["LogPrePostOutput"] = "true"
        instanceCfg["MCLaunchMethod"] = "LauncherPart"
        instanceCfg["OverrideConsole"] = "true"
        MultiMCUtil.writeCfg(instanceCfgFile, instanceCfg)

        // TODO: copy all dependnecies into mods folder
        val modsDir = minecraftDir.resolve("mods")
        modsDir.deleteRecursively()
        modsDir.mkdirs()

        val mods = MultiMCUtil.files

        logger.lifecycle("mods:")
        mods.forEach {
            logger.lifecycle(" - $it")
            val targetFile = modsDir.resolve(it.name)
            it.copyTo(targetFile, overwrite = true)
        }
        val mainJar = multiMCExtension.mainJar
        // remapJar.output
        logger.lifecycle("main mod: ${mainJar}")
        val targetFile = modsDir.resolve(mainJar.name)
        mainJar.copyTo(targetFile, overwrite = true)

        val process = ProcessBuilder(MultiMCUtil.binary, "--launch", multiMCExtension.instanceId).also {
            logger.lifecycle("executing: ${it.command().joinToString(" ")}")
        }
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()

        logger.lifecycle("started multimc instance ${multiMCExtension.instanceName} $process")

        val status = runBlocking {
            logStdout(process, logger)
            logErr(process, logger)
            process.waitFor()
        }
        logger.lifecycle("multimc instance exited with code $status")
    }
}