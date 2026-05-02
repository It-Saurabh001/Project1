package com.saurabh.sudoku.presentation.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.saurabh.sudoku.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "SoundManager"
    private var soundPool: SoundPool? = null
    private var soundCorrect: Int = 0
    private var soundMistake: Int = 0
    private var soundNote: Int = 0
    private var soundComplete: Int = 0
    private var soundErase: Int = 0
    private var soundClick: Int = 0
    private var isLoaded = false
    var isSoundEnabled: Boolean = true

    init {
        try {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            soundPool = SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(attributes)
                .build()
            soundPool?.setOnLoadCompleteListener { _, _, _ -> isLoaded = true }
            loadSounds()
        } catch (e: Exception) {
            Log.w(TAG, "SoundPool init failed: ${e.message}")
        }
    }

    private fun loadSounds() {
        try {
            soundCorrect  = soundPool?.load(context, R.raw.sound_correct,  1) ?: 0
            soundMistake  = soundPool?.load(context, R.raw.sound_mistake,  1) ?: 0
            soundNote     = soundPool?.load(context, R.raw.sound_note,     1) ?: 0
            soundComplete = soundPool?.load(context, R.raw.sound_complete, 1) ?: 0
            soundErase    = soundPool?.load(context, R.raw.sound_erase,    1) ?: 0
            soundClick    = soundPool?.load(context, R.raw.sound_click,    1) ?: 0
        } catch (e: Exception) {
            Log.w(TAG, "Sound load failed: ${e.message}")
        }
    }

    private fun play(soundId: Int, volume: Float = 1.0f) {
        if (!isSoundEnabled || !isLoaded || soundId == 0) return
        try {
            soundPool?.play(soundId, volume, volume, 1, 0, 1.0f)
        } catch (e: Exception) {
            Log.w(TAG, "Sound play failed: ${e.message}")
        }
    }

    fun playCorrect()  = play(soundCorrect)
    fun playMistake()  = play(soundMistake, 0.9f)
    fun playNote()     = play(soundNote, 0.7f)
    fun playComplete() = play(soundComplete)
    fun playErase()    = play(soundErase, 0.6f)
    fun playClick()    = play(soundClick, 0.5f)

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}
