package com.feels.core.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class NoteKeywordAnalyzerTest {

    @Test
    fun ignoresStopWordsAndShortTokens() {
        val keywords = NoteKeywordAnalyzer.topKeywords(
            listOf("I feel so very tired today tired tired"),
        )
        assertEquals(listOf("Tired"), keywords)
    }

    @Test
    fun ranksRepeatedWordsFirst() {
        val keywords = NoteKeywordAnalyzer.topKeywords(
            listOf("anxious meeting", "anxious work", "calm walk"),
            limit = 3,
        )
        assertEquals("Anxious", keywords.first())
        assertEquals(3, keywords.size)
    }

    @Test
    fun emptyNotesReturnEmpty() {
        assertEquals(emptyList<String>(), NoteKeywordAnalyzer.topKeywords(emptyList()))
    }
}
