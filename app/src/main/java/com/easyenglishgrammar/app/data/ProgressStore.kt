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

    fun reset() {
        read.value = emptySet()
        mastered.value = emptySet()
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_READ = "read"
        const val KEY_MASTERED = "mastered"
    }
}
