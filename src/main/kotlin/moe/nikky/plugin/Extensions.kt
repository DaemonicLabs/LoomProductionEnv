package moe.nikky.plugin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.gradle.api.logging.Logger
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

fun CoroutineScope.logStdout(process: Process, logger: Logger) {
    launch(Dispatchers.IO) {
        process.inputStream.bufferedReader().lines().forEach {
            logger.lifecycle(it)
        }
    }
}
fun CoroutineScope.logErr(process: Process, logger: Logger) {
    launch(Dispatchers.IO) {
        process.errorStream.bufferedReader().lines().forEach {
            logger.error(it)
        }
    }
}

fun String.runCommand(workingDir: File = File(".")): String {
    try {
        val parts = this.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()

        proc.waitFor(60, TimeUnit.MINUTES)
        return proc.inputStream.bufferedReader().readText().trim()
    } catch (e: IOException) {
        e.printStackTrace()
        throw Exception("cannot execute '$this'")
    }
}

fun File.contains(path: String): Boolean {
    return ZipInputStream(
        this.inputStream()
    ).use { zipStream ->
        var entry: ZipEntry? = zipStream.nextEntry

        while(entry != null) {
            if(entry.name == path) return@use true

            entry = zipStream.nextEntry
        }
        return@use false
    }
}