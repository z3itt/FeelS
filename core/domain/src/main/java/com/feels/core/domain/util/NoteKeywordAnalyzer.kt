package com.feels.core.domain.util

object NoteKeywordAnalyzer {

    private val STOP_WORDS = setOf(
        "a", "an", "the", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with",
        "is", "it", "this", "that", "was", "were", "be", "been", "am", "are", "my", "me",
        "i", "im", "i'm", "so", "very", "just", "feel", "feeling", "felt", "today",
    )

    fun topKeywords(notes: List<String>, limit: Int = 3): List<String> {
        if (notes.isEmpty()) return emptyList()

        val counts = mutableMapOf<String, Int>()
        notes.forEach { note ->
            tokenize(note).forEach { token ->
                counts[token] = (counts[token] ?: 0) + 1
            }
        }

        return counts.entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .take(limit)
            .map { it.key.replaceFirstChar { c -> c.uppercaseChar() } }
    }

    private fun tokenize(note: String): List<String> =
        note.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length >= 3 && it !in STOP_WORDS }
}
