package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log

object TickSoundPlayer {
    private const val SAMPLE_RATE = 44100
    private var audioTrack: AudioTrack? = null

    private val tickBuffer: ShortArray by lazy {
        val durationMs = 12
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val buffer = ShortArray(numSamples)
        
        // Synthesize a clean mechanical click:
        // A quick transient of white noise mixed with a fast decaying high-frequency sine wave.
        val frequency = 2800.0
        val random = java.util.Random()
        
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // Very sharp exponential decay
            val decay = Math.exp(-t * 450.0)
            
            // High frequency sine component
            val sineVal = Math.sin(2.0 * Math.PI * frequency * t)
            // Soft white noise transient component (strongest at the very beginning)
            val noiseVal = (random.nextFloat() * 2.0f - 1.0f) * 0.25f
            
            val mixed = (sineVal + noiseVal) * decay
            // Clamp and convert to short
            val shortVal = (mixed * 14000.0).coerceIn(-32768.0, 32767.0).toInt().toShort()
            buffer[i] = shortVal
        }
        buffer
    }

    fun playTick() {
        try {
            if (audioTrack == null) {
                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()

                val format = AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()

                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(attrs)
                    .setAudioFormat(format)
                    .setBufferSizeInBytes(tickBuffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                
                audioTrack?.write(tickBuffer, 0, tickBuffer.size)
            }
            
            audioTrack?.let { track ->
                track.stop()
                track.reloadStaticData()
                track.play()
            }
        } catch (e: Exception) {
            Log.e("TickSoundPlayer", "Error playing tick sound", e)
        }
    }

    fun release() {
        try {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            Log.e("TickSoundPlayer", "Error releasing AudioTrack", e)
        }
    }
}
