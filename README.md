LoomProductionEnv

requires gradle 5.+ (apparently?)

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

## tasks

- `multimc` sets up and starts a multimc instance

## configuration

### extension

the multimc instance can be configured using the `multimc` extension

```kotlin
multimc { ... }
```

the variables that can be modified in the extension include
```
var instanceId: String
var instanceName: String
var mainJar: File = remapjar.ouput // expects a `File` pointing the the remapped jar
var configurations: MutableList<Configuration>  // configurations that will copy dependencies into the multimc instance
```

### dependnecies

adding dependencies to `multimcMod` will copy them into the instances mods folder

```kotlin
dependencies {
   //...
   
   multimcMod("io.github.prospector.modmenu:ModMenu:+")
}
```