package moe.nikky.plugin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.gradle.api.logging.Logger

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