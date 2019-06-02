plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    idea
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    compile(group = "com.squareup", name = "kotlinpoet", version = "1.3.0")
}

configure<GradlePluginDevelopmentExtension> {
    plugins {
        create("constGenerator") {
            id = "constantsGenerator"
            implementationClass = "plugin.GeneratorPlugin"
        }
    }
}