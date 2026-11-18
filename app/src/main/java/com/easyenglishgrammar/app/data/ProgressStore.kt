package com.easyenglishgrammar.app.data

import android.content.Context
import androidx.compose.runtime.mutableStateOf

/** Remembers which rules have been read and which quizzes were aced. */
class ProgressStore(context: Context) {

    private val prefs = context.getSharedPreferences("progress", Context.MODE_PRIVATE)

    val read = mutableStateOf(prefs.getStringSet(KEY_READ, emptySet())!!.toSet())
    val mastered = mutableStateOf(prefs.getStringSet(KEY_MASTERED, emptySet())!!.toSet())

    fun markRead(ruleId: String) {
        if (ruleId in read.value) return
        read.value = read.value + ruleId
        prefs.edit().putStringSet(KEY_READ, read.value).apply()
    }

    fun markMastered(ruleId: String) {
        markRead(ruleId)
        if (ruleId in mastered.value) return
        mastered.value = mastered.value + ruleId
        prefs.edit().putStringSet(KEY_MASTERED, mastered.value).apply()
    }

    /** Best score (percent) in the Practice Dojo, per topic ("All" for the mixed test). */
    fun bestPractice(topic: String): Int = prefs.getInt(KEY_PRACTICE + topic, -1)

    fun recordPractice(topic: String, percent: Int) {
        if (percent > bestPractice(topic)) prefs.edit().putInt(KEY_PRACTICE + topic, percent).apply()
    }

    fun reset() {
        read.value = emptySet()
        mastered.value = emptySet()
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_READ = "read"
        const val KEY_MASTERED = "mastered"
        const val KEY_PRACTICE = "practice_best_"
    }
}
