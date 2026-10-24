package com.easyenglishgrammar.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.runtime.mutableStateOf
import com.easyenglishgrammar.app.R

/**
 * Loops the calm background theme. Remembers whether the music is muted,
 * and gets quieter ("ducks") while a character is speaking.
 */
class MusicPlayer(context: Context) {

    private val prefs = context.getSharedPreferences("audio", Context.MODE_PRIVATE)
    private val appContext = context.applicationContext
    private var player: MediaPlayer? = null
    private var inForeground = false
    private var ducked = false

    val muted = mutableStateOf(prefs.getBoolean(KEY_MUTED, false))

    fun toggleMute() {
        muted.value = !muted.value
        prefs.edit().putBoolean(KEY_MUTED, muted.value).apply()
        if (muted.value) player?.pause() else play()
    }

    fun onForeground() {
        inForeground = true
        if (!muted.value) play()
    }

    fun onBackground() {
        inForeground = false
        player?.pause()
    }

    fun duck(on: Boolean) {
        ducked = on
        applyVolume()
    }

    fun release() {
        player?.release()
        player = null
    }

    private fun play() {
        if (!inForeground) return
        val p = player ?: MediaPlayer.create(appContext, R.raw.bgm)?.apply {
            isLooping = true
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
        }?.also { player = it } ?: return
        applyVolume()
        if (!p.isPlaying) p.start()
    }

    private fun applyVolume() {
        val v = if (ducked) DUCKED_VOLUME else VOLUME
        player?.setVolume(v, v)
    }

    private companion object {
        const val KEY_MUTED = "music_muted"
        const val VOLUME = 0.45f
        const val DUCKED_VOLUME = 0.12f
    }
}
