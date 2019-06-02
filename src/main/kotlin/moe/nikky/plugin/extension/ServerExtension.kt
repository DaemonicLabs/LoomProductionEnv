package moe.nikky.plugin.extension

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.BasePluginConvention
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getPluginByName
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

open class ServerExtension(
    private val project: Project,
    private val multimcConfiguration: Configuration
) {
    private val base: BasePluginConvention
        get() = project.convention.getPluginByName<org.gradle.api.plugins.BasePluginConvention>("base")

    /**
     * working directory of the server
     */
    var workingDirectory = project.file("runServer")

    /**
     * max memory
     */
    var Xmx: String = "${1024 * 4}M"

    var gui: Boolean = true

    var jvmArgs: MutableList<String> = mutableListOf()
    var arguments: MutableList<String> = mutableListOf()

    // TODO: grab latest installer version from:
    //  https://maven.modmuss50.me/net/fabricmc/fabric-installer/maven-metadata.xml
    private var installerVersionProperty: String? = null
    var installerVersion: String
        set(value: String) {
            installerVersionProperty = value
        }
        get() = installerVersionProperty ?: run {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse("https://maven.modmuss50.me/net/fabricmc/fabric-installer/maven-metadata.xml")
            val root = doc.documentElement
            val versioning = root.getElementsByTagName("versioning").item(0) as Element
            val released = versioning.getElementsByTagName("release").item(0) as Element
            released.textContent
        }

    /**
     * configurations that will copy dependencies into the multimc instance
     */
    var configurations: MutableList<Configuration> = listOfNotNull(
        multimcConfiguration,
        project.configurations["modCompile"]
    ).toMutableList()
}