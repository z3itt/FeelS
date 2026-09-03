package com.feels.core.data.backup

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val FORMAT_VERSION = 1
private const val APP_ID = "com.z3itt.feels"

@Serializable
data class FeelSBackupFile(
    val formatVersion: Int = FORMAT_VERSION,
    val appId: String = APP_ID,
    val exportedAtMillis: Long,
    val checkIns: List<FeelSBackupCheckIn> = emptyList(),
)

@Serializable
data class FeelSBackupCheckIn(
    val emotionId: String,
    val intensity: Int,
    val note: String? = null,
    val timestampMillis: Long,
)

object FeelSBackupJson {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    fun encode(file: FeelSBackupFile): String = json.encodeToString(FeelSBackupFile.serializer(), file)

    fun decode(raw: String): FeelSBackupFile = json.decodeFromString(FeelSBackupFile.serializer(), raw)
}
