LoomProductionEnv

requires gradle 5.+ (apparently?)

## What does this do?

task `multimc` sets up a new instance in multimc, installs fabric and copies your mod and dependencies over

TODO: task `server` creates a dedicated server, installs fabric and mods and starts it

## setup

**on windows** make sure that the `multimc` folder is added to `PATH` environment variable

make sure to apply it after `fabric-loom`

`build.gradle.kts`
```kotlin
plugins {
    id("moe.nikky.loom-production-env") version "0.0.1-SNAPSHOT"
}
```

`build.gradle`
```groovy
plugins {
    id 'moe.nikky.loom-production-env' version '0.0.1-SNAPSHOT'
}
```

## configuration

### extension

the production environments can be configured using the `multimc` extension

```kotlin
production {
    mainjar = file(...)
    multimc { ... }
    server { ... }
}
```

#### prodction

```
var mainJar: File = remapjar.ouput // expects a `File` pointing the the remapped jar
```

#### multimc

```
var instanceId: String
var instanceName: String
var configurations: MutableList<Configuration>  // configurations that will copy dependencies into the multimc instance
```

#### server

```
var workingDirectory: File
var Xmx: String // example: 4048M
var gui: Boolean
var extraArguments: MutableList<String> // extra server launch arguments
var configurations: MutableList<Configuration>  // configurations that will copy dependencies into the multimc instance
```

### dependencies

adding dependencies to `multimcMod` will copy them into the instances mods folder

```kotlin
dependencies {
   //...
   
   multimc("io.github.prospector.modmenu:ModMenu:+")
   
   
   server("group:artifactID:version")
}
```