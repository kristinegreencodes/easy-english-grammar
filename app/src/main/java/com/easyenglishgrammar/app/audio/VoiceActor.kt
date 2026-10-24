package com.easyenglishgrammar.app.audio

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Gives each cast member their own voice using the phone's text-to-speech
 * engine. Every character speaks with a different pitch and speed.
 */
class VoiceActor(context: Context, private val music: MusicPlayer) {

    private data class VoiceStyle(val pitch: Float, val rate: Float)

    private val styles = mapOf(
        "hana" to VoiceStyle(1.25f, 1.0f),
        "yumi" to VoiceStyle(1.55f, 1.08f),
        "kaito" to VoiceStyle(0.8f, 1.12f),
        "sensei" to VoiceStyle(0.65f, 0.85f),
        "chrono" to VoiceStyle(0.9f, 0.9f),
        "kage" to VoiceStyle(0.55f, 0.95f),
        "narrator" to VoiceStyle(1.0f, 0.92f),
    )

    private val prefs = context.getSharedPreferences("audio", Context.MODE_PRIVATE)
    private val main = Handler(Looper.getMainLooper())
    private val pending = mutableMapOf<String, () -> Unit>()
    private var counter = 0
    private var ready = false

    val enabled = mutableStateOf(prefs.getBoolean(KEY_VOICES, true))

    /** The utterance currently being spoken, so the UI can highlight it. */
    val speakingKey = mutableStateOf<String?>(null)

    private val tts: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        if (status == TextToSpeech.SUCCESS) {
            val result = engine.setLanguage(Locale.US)
            ready = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }
    private val engine get() = tts

    init {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {}
            override fun onDone(utteranceId: String) = finish(utteranceId)
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String) = finish(utteranceId)
            override fun onError(utteranceId: String, errorCode: Int) = finish(utteranceId)
            override fun onStop(utteranceId: String, interrupted: Boolean) = finish(utteranceId)
        })
    }

    fun toggle() {
        enabled.value = !enabled.value
        prefs.edit().putBoolean(KEY_VOICES, enabled.value).apply()
        if (!enabled.value) stop()
    }

    /** Speaks a line in the character's voice and suspends until it's finished. */
    suspend fun speakAndWait(who: String, text: String, key: String) {
        if (!enabled.value) return
        if (!ready) {
            // No speech engine available: pause for roughly the time it would take to read.
            delay(text.length * 45L)
            return
        }
        suspendCancellableCoroutine { cont ->
            val id = start(who, text, key) { if (cont.isActive) cont.resume(Unit) }
            cont.invokeOnCancellation { main.post { if (pending.containsKey(id)) stop() } }
        }
    }

    /** Fire-and-forget version for replaying a single line. */
    fun speak(who: String, text: String, key: String) {
        if (enabled.value && ready) start(who, text, key) {}
    }

    fun stop() {
        tts.stop()
        val callbacks = pending.values.toList()
        pending.clear()
        speakingKey.value = null
        music.duck(false)
        callbacks.forEach { it() }
    }

    fun shutdown() {
        stop()
        tts.shutdown()
    }

    private fun start(who: String, text: String, key: String, onDone: () -> Unit): String {
        val style = styles[who] ?: VoiceStyle(1.0f, 1.0f)
        val id = "u${counter++}"
        // A new line replaces whatever was playing; let the old caller continue.
        val previous = pending.values.toList()
        pending.clear()
        previous.forEach { it() }
        pending[id] = onDone
        speakingKey.value = key
        music.duck(true)
        tts.setPitch(style.pitch)
        tts.setSpeechRate(style.rate)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, Bundle(), id)
        return id
    }

    private fun finish(id: String) {
        main.post {
            val callback = pending.remove(id) ?: return@post
            if (pending.isEmpty()) {
                speakingKey.value = null
                music.duck(false)
            }
            callback()
        }
    }

    private companion object {
        const val KEY_VOICES = "voices_enabled"
    }
}
