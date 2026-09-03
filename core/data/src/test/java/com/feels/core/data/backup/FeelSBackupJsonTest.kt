package com.feels.core.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeelSBackupJsonTest {

    @Test
    fun roundTripPreservesCheckIns() {
        val original = FeelSBackupFile(
            exportedAtMillis = 1_700_000_000_000L,
            checkIns = listOf(
                FeelSBackupCheckIn(
                    emotionId = "sad",
                    intensity = 4,
                    note = "quiet day",
                    timestampMillis = 1_700_000_100_000L,
                ),
            ),
        )
        val parsed = FeelSBackupJson.decode(FeelSBackupJson.encode(original))
        assertEquals(original.formatVersion, parsed.formatVersion)
        assertEquals(original.appId, parsed.appId)
        assertEquals(1, parsed.checkIns.size)
        assertEquals("sad", parsed.checkIns[0].emotionId)
        assertEquals(4, parsed.checkIns[0].intensity)
        assertEquals("quiet day", parsed.checkIns[0].note)
    }

    @Test
    fun decodeIgnoresUnknownKeys() {
        val raw = """
            {
              "formatVersion": 1,
              "appId": "com.z3itt.feels",
              "exportedAtMillis": 1,
              "futureField": true,
              "checkIns": []
            }
        """.trimIndent()
        val parsed = FeelSBackupJson.decode(raw)
        assertTrue(parsed.checkIns.isEmpty())
        assertEquals(1, parsed.formatVersion)
    }
}
