package com.example.player

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader

data class SubtitleEntry(
    val index: Int,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val text: String
)

object SubtitleParser {

    fun parseFromUri(context: Context, uri: Uri): List<SubtitleEntry> {
        val entries = mutableListOf<SubtitleEntry>()
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                var line: String?
                var currentIndex = 0
                var currentStartMs = 0L
                var currentEndMs = 0L
                val textBuilder = StringBuilder()

                while (reader.readLine().also { line = it } != null) {
                    val trimmed = line?.trim() ?: ""
                    if (trimmed.isEmpty()) {
                        if (textBuilder.isNotEmpty()) {
                            entries.add(
                                SubtitleEntry(
                                    index = currentIndex,
                                    startTimeMs = currentStartMs,
                                    endTimeMs = currentEndMs,
                                    text = textBuilder.toString().trim()
                                )
                            )
                            textBuilder.clear()
                        }
                    } else if (trimmed.contains("-->")) {
                        // Timing line: 00:00:20,000 --> 00:00:24,400 or 00:00:20.000 --> 00:00:24.400
                        val parts = trimmed.split("-->")
                        if (parts.size >= 2) {
                            currentStartMs = parseTimestampToMs(parts[0].trim())
                            val endPart = parts[1].trim().split(" ")[0] // remove extra vtt tags
                            currentEndMs = parseTimestampToMs(endPart)
                        }
                    } else if (trimmed.all { it.isDigit() } && textBuilder.isEmpty()) {
                        currentIndex = trimmed.toIntOrNull() ?: 0
                    } else {
                        // Subtitle text line
                        if (textBuilder.isNotEmpty()) textBuilder.append("\n")
                        textBuilder.append(trimmed.replace(Regex("<[^>]*>"), "")) // Strip HTML tags
                    }
                }

                if (textBuilder.isNotEmpty()) {
                    entries.add(
                        SubtitleEntry(
                            index = currentIndex,
                            startTimeMs = currentStartMs,
                            endTimeMs = currentEndMs,
                            text = textBuilder.toString().trim()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return entries
    }

    private fun parseTimestampToMs(timestamp: String): Long {
        try {
            val normalized = timestamp.replace(',', '.')
            val parts = normalized.split(":")
            return when (parts.size) {
                3 -> {
                    val hours = parts[0].toLongOrNull() ?: 0L
                    val minutes = parts[1].toLongOrNull() ?: 0L
                    val secondsParts = parts[2].split(".")
                    val seconds = secondsParts[0].toLongOrNull() ?: 0L
                    val millis = if (secondsParts.size > 1) {
                        secondsParts[1].padEnd(3, '0').take(3).toLongOrNull() ?: 0L
                    } else 0L
                    (hours * 3600 + minutes * 60 + seconds) * 1000 + millis
                }
                2 -> {
                    val minutes = parts[0].toLongOrNull() ?: 0L
                    val secondsParts = parts[1].split(".")
                    val seconds = secondsParts[0].toLongOrNull() ?: 0L
                    val millis = if (secondsParts.size > 1) {
                        secondsParts[1].padEnd(3, '0').take(3).toLongOrNull() ?: 0L
                    } else 0L
                    (minutes * 60 + seconds) * 1000 + millis
                }
                else -> 0L
            }
        } catch (e: Exception) {
            return 0L
        }
    }

    fun getCurrentSubtitle(entries: List<SubtitleEntry>, currentPositionMs: Long): String? {
        if (entries.isEmpty()) return null
        return entries.find { currentPositionMs in it.startTimeMs..it.endTimeMs }?.text
    }
}
