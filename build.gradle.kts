import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import plugin.GenerateConstantsTask

// First, apply the publishing plugin
plugins {
    id("com.gradle.plugin-publish") version "0.10.0"
    id("moe.nikky.persistentCounter") version "0.0.8-SNAPSHOT"
    id("com.github.johnrengelman.shadow") version "4.0.4"
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
    id("kotlinx-serialization") version "1.3.31"
    constantsGenerator
}

val ver = "0.0.1"

val branch = System.getenv("GIT_BRANCH")
    ?.takeUnless { it == "master" }
    ?.let { "-$it" }
    ?: ""

val buildNumber = counter.variable(id = "buildNumber", key = ver + branch)

val isCI = System.getenv("BUILD_NUMBER") != null

group = "moe.nikky"
version = ver + if (isCI) "-SNAPSHOT" else "-dev"
val versionSuffix = if (isCI) buildNumber else "dev"

constants {
    constantsObject(pkg = "moe.nikky.plugin", className = "Const") {
        field("VERSION") value "$ver-$versionSuffix"
    }
}

//val cleanGenerateConstants = tasks.getByName("")
val generateConstants by tasks.getting(GenerateConstantsTask::class) {
    kotlin.sourceSets["main"].kotlin.srcDir(outputFolder)
    outputs.upToDateWhen { false }
}

// TODO depend on kotlin tasks in the plugin ?
tasks.withType<KotlinCompile> {
    dependsOn(generateConstants)
}

// If your plugin has any external java dependencies, Gradle will attempt to
// download them from JCenter for anyone using the plugins DSL
// so you should probably use JCenter for dependency resolution in your own
// project.
repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    shadow(
        group = "org.jetbrains.kotlinx",
        name = "kotlinx-serialization-runtime",
        version = "0.11.0"
    )
    shadow(
        group = "org.jetbrains.kotlinx",
        name = "kotlinx-coroutines-core",
        version = "1.2.1"
    )
}

val shadowJar = tasks.getByName<ShadowJar>("shadowJar") {
    mustRunAfter("jar")
    archiveClassifier.set("")
//    classifier = ""
    configurations = listOf(
        project.configurations.shadow.get()
    )
//    exclude("META-INF")
}

artifacts {
    add("archives", shadowJar)
}

val pluginId = "moe.nikky.loom-production-env"
// Use java-gradle-plugin to generate plugin descriptors and specify plugin ids
gradlePlugin {
    plugins {
        create("loonProductionEnv") {
            id = pluginId
            implementationClass = "moe.nikky.plugin.LoomProdPlugin"
        }
    }
}

pluginBundle {
    // These settings are set for the whole plugin bundle
    website = "https://github.com/DaemonicLabs/LoomProductionEnv"
    vcsUrl = "https://github.com/DaemonicLabs/LoomProductionEnv"

    // tags and description can be set for the whole bundle here, but can also
    // be set / overridden in the config for specific plugins
//    description = "Greetings from here!"

    // The plugins block can contain multiple plugin entries.
    //
    // The name for each plugin block below (greetingsPlugin, goodbyePlugin)
    // does not affect the plugin configuration, but they need to be unique
    // for each plugin.

    // Plugin config blocks can set the id, displayName, version, description
    // and tags for each plugin.

    // id and displayName are mandatory.
    // If no version is set, the project version will be used.
    // If no tags or description are set, the tags or description from the
    // pluginBundle block will be used, but they must be set in one of the
    // two places.

    (plugins) {

        // first plugin
        "loonProductionEnv" {
            // id is captured from java-gradle-plugin configuration
            displayName = "Loom Production Environment"
            description = "Test mods using a production environment"
            tags = listOf("fabric", "minecraft", "loom")
        }
    }

    // Optional overrides for Maven coordinates.
    // If you have an existing plugin deployed to Bintray and would like to keep
    // your existing group ID and artifact ID for continuity, you can specify
    // them here.
    //
    // As publishing to a custom group requires manual approval by the Gradle
    // team for security reasons, we recommend not overriding the group ID unless
    // you have an existing group ID that you wish to keep. If not overridden,
    // plugins will be published automatically without a manual approval process.
    //
    // You can also override the version of the deployed artifact here, though it
    // defaults to the project version, which would normally be sufficient.

//    mavenCoordinates {
//        groupId = project.group
//        artifactId = "persistentCounter"
//        version = project.version
//    }
}

val sourcesJar = tasks.create<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

val javadoc = tasks.getByName<Javadoc>("javadoc") {}
val javadocJar = tasks.create<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(javadoc)
}

publishing {
    publications {
        create("main", MavenPublication::class.java) {
            artifact(shadowJar)
            artifact(sourcesJar)
            artifact(javadocJar)
        }
    }
    repositories {
        maven(url = "http://mavenupload.modmuss50.me/") {
            val mavenPass: String? = project.properties["mavenPass"] as String?
            mavenPass?.let {
                credentials {
                    username = "buildslave"
                    password = mavenPass
                }
            }
        }
    }
}
