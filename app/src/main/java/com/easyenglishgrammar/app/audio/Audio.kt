package com.easyenglishgrammar.app.audio

import androidx.compose.runtime.staticCompositionLocalOf

class Audio(val music: MusicPlayer, val voices: VoiceActor)

val LocalAudio = staticCompositionLocalOf<Audio> { error("Audio not provided") }
