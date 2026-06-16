package com.example.utils

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

object SoundEngine {
    private var toneGenerator: ToneGenerator? = null
    
    // Audio options
    var isSoundEnabled: Boolean = true
    var volumePercent: Int = 85 // 0 to 100
    
    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, volumePercent)
        } catch (e: Exception) {
            Log.e("SoundEngine", "Failed to initialize ToneGenerator", e)
        }
    }
    
    fun updateVolume(newVolume: Int) {
        volumePercent = newVolume.coerceIn(0, 100)
        try {
            toneGenerator?.release()
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, volumePercent)
        } catch (e: Exception) {
            Log.e("SoundEngine", "Failed to update ToneGenerator volume preset", e)
        }
    }
    
    fun playClick() {
        if (!isSoundEnabled) return
        try {
            // Short, tactile navigation feedback tone
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 60)
        } catch (e: Exception) {
            Log.e("SoundEngine", "Error playing click tone", e)
        }
    }
    
    fun playWhistle() {
        if (!isSoundEnabled) return
        Thread {
            try {
                // Dual high-intensity blasts: Ref whistle!
                toneGenerator?.startTone(ToneGenerator.TONE_SUP_PIP, 120)
                Thread.sleep(160)
                toneGenerator?.startTone(ToneGenerator.TONE_SUP_PIP, 120)
                Thread.sleep(160)
                toneGenerator?.startTone(ToneGenerator.TONE_SUP_RADIO_ACK, 400)
            } catch (e: Exception) {
                Log.e("SoundEngine", "Error playing whistle sound", e)
            }
        }.start()
    }

    fun playGoalCheer() {
        if (!isSoundEnabled) return
        Thread {
            try {
                // Tri-tone major chord rise representing the goal alarm/cheers!
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
                Thread.sleep(100)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 120)
                Thread.sleep(100)
                toneGenerator?.startTone(ToneGenerator.TONE_SUP_DIAL, 450)
            } catch (e: Exception) {
                Log.e("SoundEngine", "Error playing goal sound", e)
            }
        }.start()
    }

    fun playNotification() {
        if (!isSoundEnabled) return
        try {
            // Friendly double chime for incoming manager notifications or message alerts
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 140)
        } catch (e: Exception) {
            Log.e("SoundEngine", "Error playing notification sound", e)
        }
    }

    fun playUpgradeSuccess() {
        if (!isSoundEnabled) return
        Thread {
            try {
                // High-ascending pitch celebratory sound
                toneGenerator?.startTone(ToneGenerator.TONE_SUP_PIP, 150)
                Thread.sleep(120)
                toneGenerator?.startTone(ToneGenerator.TONE_SUP_CONFIRM, 300)
            } catch (e: Exception) {
                Log.e("SoundEngine", "Error playing upgrade success sound", e)
            }
        }.start()
    }
}
