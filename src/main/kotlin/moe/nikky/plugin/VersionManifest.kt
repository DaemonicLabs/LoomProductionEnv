package moe.nikky.plugin

import kotlinx.serialization.Serializable

@Serializable
data class VersionManifest(
    val latest: Latest,
    val versions: List<Version>
)

@Serializable
data class Latest(
    val release: String,
    val snapshot: String
)

@Serializable
data class Version(
    val id: String,
    val type: String,
    val url: String,
    val time: String,
    val releaseTime: String
)

@Serializable
data class PackageVersion(
    val downloads: Map<String, Download>
)

@Serializable
data class Download(
    val sha1: String,
    val size: Long,
    val url: String
)