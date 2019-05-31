LoomProductionEnv

## setup

on windows make sure that the `multimc` folder is added to `PATH` environment variable

## usage

make sure to apply it after `fabric-loom`

`build.gradle.kts`
```kotlin
plugins {
    id("moe.nikky.loom-production-env") version "0.0.1-SNAPSHOT"
}

multimc {
    mainJar = remapJar.output
}
```

`build.gradle`
```groovy
plugins {
    id 'moe.nikky.loom-production-env' version '0.0.1-SNAPSHOT'
}

multimc {
    mainJar = remapJar.output
}
```