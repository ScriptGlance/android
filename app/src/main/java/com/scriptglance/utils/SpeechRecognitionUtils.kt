package com.scriptglance.utils

import android.util.Log


private val PUNCTUATION_REGEX = """[.,!?;:"'"„“‘’`—–-]""".toRegex()

fun calculateLevenshteinDistance(a: String, b: String): Int {
    if (a.isEmpty()) return b.length
    if (b.isEmpty()) return a.length

    val matrix = Array(b.length + 1) { IntArray(a.length + 1) }

    for (i in 0..b.length) {
        matrix[i][0] = i
    }

    for (j in 0..a.length) {
        matrix[0][j] = j
    }

    for (i in 1..b.length) {
        for (j in 1..a.length) {
            val cost = if (b[i - 1] == a[j - 1]) 0 else 1
            matrix[i][j] = minOf(
                matrix[i - 1][j] + 1,
                matrix[i][j - 1] + 1,
                matrix[i - 1][j - 1] + cost
            )
        }
    }

    return matrix[b.length][a.length]
}


fun getSimilarity(word1: String, word2: String): Float {
    val w1 = word1.lowercase().replace(PUNCTUATION_REGEX, "")
    val w2 = word2.lowercase().replace(PUNCTUATION_REGEX, "")

    if (w1.isEmpty() || w2.isEmpty()) return 0f
    if (w1 == w2) return 1f

    val distance = calculateLevenshteinDistance(w1, w2)
    val maxLength = maxOf(w1.length, w2.length)

    return if (maxLength == 0) 1f else (maxLength - distance).toFloat() / maxLength
}


fun findLastNonWhitespaceIdx(wordsArray: List<String>, startIdx: Int, endIdx: Int): Int {
    for (i in endIdx downTo startIdx) {
        if (wordsArray[i].trim().isNotEmpty()) {
            return i
        }
    }
    return -1
}

fun handleSpeechRecognitionResult(
    transcript: String,
    scriptWords: List<String>,
    currentWordIndex: Int,
    currentPartId: Int,
    isNearEndOfPart: (Int, Int) -> Boolean,
    calculateCharPosition: (Int, Int) -> Int,
    updateHighlightAndScroll: (Int, Int) -> Unit,
    sendReadingPosition: (Int) -> Unit,
    sendFinalPosition: (Int) -> Unit,
    timeAtCurrentPositionRef: Long,
    lastWordAdvanceTimeRef: Long
) {

    val recognizedWords = transcript
        .lowercase()
        .replace(PUNCTUATION_REGEX, "")
        .split("""\s+""".toRegex())
        .filter { it.isNotBlank() }


    if (recognizedWords.isEmpty()) {
        Log.d("SpeechRecognition", "No recognized words in transcript: ${transcript}")
        return
    }

    val lastMeaningfulWordIdx = findLastNonWhitespaceIdx(
        scriptWords,
        0,
        scriptWords.size - 1
    )

    if (lastMeaningfulWordIdx == -1) {
        Log.w("SpeechRecognition", "No meaningful words found in script.")
        return
    }

    if (currentWordIndex >= lastMeaningfulWordIdx) {
        Log.d(
            "SpeechRecognition",
            "Current word index is beyond the last meaningful word index. Sending final position."
        )
        sendFinalPosition(currentPartId)
        return
    }

    var currentPosition = currentWordIndex
    val MAX_LOOKAHEAD = 4
    val MIN_MS_PER_WORD = 100

    Log.d(
        "SpeechRecognition",
        "Recognized: \"${transcript}\" (position: ${currentWordIndex})"
    )

    for (recognizedWord in recognizedWords) {
        val searchWindowWords = mutableListOf<Pair<String, Int>>()
        var lookAheadCount = 0
        var searchIdx = currentPosition + 1

        while (lookAheadCount < MAX_LOOKAHEAD && searchIdx < scriptWords.size) {
            if (scriptWords[searchIdx].isNotBlank()) {
                val cleanWord = scriptWords[searchIdx].lowercase().replace(PUNCTUATION_REGEX, "")
                searchWindowWords.add(Pair(cleanWord, searchIdx))
                lookAheadCount++
            }
            searchIdx++
        }

        if (searchWindowWords.isEmpty()) {
            Log.d(
                "SpeechRecognition",
                "No valid words found in the next $MAX_LOOKAHEAD positions after current position $currentPosition."
            )
            if (isNearEndOfPart(currentPartId, currentPosition)) {
                sendFinalPosition(currentPartId)
            }
            break
        }

        var bestMatchIdx = -1
        var bestSimilarity = 0f

        for ((word, idx) in searchWindowWords) {
            val similarity = getSimilarity(recognizedWord, word)
            if (similarity > bestSimilarity && similarity >= 0.5f) {
                bestSimilarity = similarity
                bestMatchIdx = idx
                if (similarity >= 0.95f) break
            }
        }

        if (bestMatchIdx != -1) {
            val now = System.currentTimeMillis()
            val msSinceLastWord = now - lastWordAdvanceTimeRef

            if (msSinceLastWord < MIN_MS_PER_WORD) {
                Log.d(
                    "SpeechRecognition",
                    "Too fast ($msSinceLastWord ms since last word). Ignoring \"$recognizedWord\""
                )
                continue
            }

            currentPosition = bestMatchIdx
            val charPos = calculateCharPosition(currentPartId, currentPosition)
            updateHighlightAndScroll(currentPartId, currentPosition)
            sendReadingPosition(charPos)

            if (currentPosition >= lastMeaningfulWordIdx) {
                sendFinalPosition(currentPartId)
                break
            }
        }
    }

    if (isNearEndOfPart(currentPartId, currentPosition)) {
        val now = System.currentTimeMillis()
        val timeAtPosition = now - timeAtCurrentPositionRef
        if (timeAtPosition > 5000) {
            Log.d(
                "SpeechRecognition",
                "Near end of part, sending final position after 5 seconds of inactivity."
            )
            sendFinalPosition(currentPartId)
        }
    }
}