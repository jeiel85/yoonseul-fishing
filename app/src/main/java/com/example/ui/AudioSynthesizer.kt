package com.example.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

/**
 * Procedurally synthesizes a quiet, healing background soundscape:
 * 1. Healing major pentatonic chimes that fade in and out.
 * 2. Gentle ocean/river wave sounds (filtered white noise modulated over time).
 * Uses Android's AudioTrack for interactive real-time generation with zero file assets.
 */
class AudioSynthesizer(private val context: Context) {
    private val TAG = "AudioSynthesizer"
    private val SAMPLE_RATE = 22050
    private var audioTrack: AudioTrack? = null
    private var audioJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    @Volatile
    private var isPlaying = false

    @Volatile
    private var isMuted = false

    @Volatile
    private var activeNatureSound = NatureSound.WATER_LAP

    @Volatile
    private var activeTimeOfDay = TimeOfDay.DAY

    @Volatile
    private var activeWeather = Weather.CLEAR

    // Cross-fade target volumes for each ambient channel
    @Volatile
    private var targetWindVol = 0.15f
    @Volatile
    private var targetWaterVol = 1.0f
    @Volatile
    private var targetCricketsVol = 0.0f

    // Current interpolation volumes
    private var currentWindVol = 0.15f
    private var currentWaterVol = 1.0f
    private var currentCricketsVol = 0.0f

    fun setNatureSound(sound: NatureSound) {
        activeNatureSound = sound
        updateTargets()
    }

    fun setTimeOfDay(time: TimeOfDay) {
        activeTimeOfDay = time
        updateTargets()
    }

    fun setWeather(w: Weather) {
        activeWeather = w
        updateTargets()
    }

    private fun updateTargets() {
        var windBase = 0.1f
        var waterBase = 0.15f
        var cricketsBase = 0.0f

        when (activeNatureSound) {
            NatureSound.WIND -> {
                windBase = 1.0f
                waterBase = 0.25f
                cricketsBase = 0.05f
            }
            NatureSound.WATER_LAP -> {
                windBase = 0.15f
                waterBase = 1.0f
                cricketsBase = 0.05f
            }
            NatureSound.CRICKETS -> {
                windBase = 0.1f
                waterBase = 0.2f
                cricketsBase = 1.0f
            }
        }

        // Night time automatically boosts crickets (cosy starry night ambience)
        if (activeTimeOfDay == TimeOfDay.NIGHT) {
            cricketsBase += 0.35f
        }

        // Mist/Rain dampens crickets but increases blowing wind
        if (activeWeather == Weather.MIST) {
            windBase += 0.2f
            cricketsBase *= 0.3f
        } else if (activeWeather == Weather.RAIN) {
            windBase += 0.4f
            cricketsBase *= 0.05f
            waterBase += 0.2f
        }

        targetWindVol = windBase.coerceIn(0f, 1f)
        targetWaterVol = waterBase.coerceIn(0f, 1f)
        targetCricketsVol = cricketsBase.coerceIn(0f, 1f)
    }

    // Pentatonic scale frequencies in G Major / E minor (healing frequencies: E4, G4, A4, B4, D5, E5, G5, A5, B5, D6, E6)
    private val notes = listOf(
        329.63f, // E4
        392.00f, // G4
        440.00f, // A4
        493.88f, // B4
        587.33f, // D5
        659.25f, // E5
        783.99f, // G5
        880.00f, // A5
        987.77f, // B5
        1174.66f // D6
    )

    private class ActiveChime(
        val freq: Float,
        val startSample: Long,
        val durationSamples: Long,
        val volume: Float
    )

    private val activeChimes = CopyOnWriteArrayList<ActiveChime>()
    private var sampleCounter: Long = 0

    init {
        try {
            val minBufBytes = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = if (minBufBytes > 0) minBufBytes else 4096

            val builder = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val attributedContext = context.createAttributionContext("default")
                builder.setContext(attributedContext)
            }

            audioTrack = builder.build()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AudioTrack or AudioSynthesizer", e)
        }
    }

    /**
     * Start the sound synthesis engine.
     */
    fun start() {
        if (isPlaying) return
        val track = audioTrack ?: return
        isPlaying = true

        try {
            track.play()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start AudioTrack playback", e)
            return
        }

        audioJob = coroutineScope.launch {
            // Write synthesis buffer continuously
            val bufferSize = 1024
            val buffer = ShortArray(bufferSize)
            
            // For brownian/low filter noise
            var lastNoise = 0f
            var lastWindFilter = 0f
            var lastWaterFilter = 0f

            // Launcher loop for ambient chimes
            launch {
                while (isActive && isPlaying) {
                    // Trigger a random gentle chime note every 2-4 seconds
                    val delaySec = (1.8 + Math.random() * 2.2).toFloat()
                    delay((delaySec * 1000).toLong())

                    if (!isMuted) {
                        triggerRandomChime()
                    }
                }
            }

            while (isActive && isPlaying) {
                if (isMuted) {
                    // Output absolute silence nicely without spending CPU
                    buffer.fill(0)
                    track.write(buffer, 0, buffer.size)
                    delay(40)
                    continue
                }

                for (i in 0 until bufferSize) {
                    val t = sampleCounter / SAMPLE_RATE.toDouble()
                    
                    // Smoothly interpolate current volumes to their target mixes
                    currentWindVol += (targetWindVol - currentWindVol) * 0.00003f
                    currentWaterVol += (targetWaterVol - currentWaterVol) * 0.00003f
                    currentCricketsVol += (targetCricketsVol - currentCricketsVol) * 0.00003f

                    val rawWhite = (Math.random().toFloat() - 0.5f) * 2f
                    
                    // --- 1. SOFT WIND GENERATION (Deep resonant lowpass noise) ---
                    val windGust = 0.5f + 0.5f * sin(2.0 * Math.PI * t / 7.2).toFloat() * cos(2.0 * Math.PI * t / 3.4).toFloat()
                    val windFilterAlpha = 0.015f + 0.02f * windGust
                    lastWindFilter = (1f - windFilterAlpha) * lastWindFilter + windFilterAlpha * rawWhite
                    val windVolume = 0.045f * windGust * currentWindVol
                    val windOut = lastWindFilter * windVolume

                    // --- 2. GENTLE WATER LAPPING (Periodic rhythmic sploshes) ---
                    val waterSwell = 0.4f + 0.6f * cos(2.0 * Math.PI * t / 5.5).toFloat()
                    val lapPhase = (t % 4.0)
                    val lapSwell = if (lapPhase < 1.0) {
                        sin(Math.PI * lapPhase / 1.0).toFloat()
                    } else {
                        0f
                    }
                    val waterFilterAlpha = 0.035f + 0.04f * (waterSwell + lapSwell * 0.5f)
                    lastWaterFilter = (1f - waterFilterAlpha) * lastWaterFilter + waterFilterAlpha * rawWhite
                    val waterVolume = (0.022f * waterSwell + 0.028f * lapSwell) * currentWaterVol
                    val waterOut = lastWaterFilter * waterVolume

                    // --- 3. NIGHT CRICKETS (High-pitched textured chirps) ---
                    val cricketCycle = (t % 1.6)
                    var cricketOut = 0f
                    if (cricketCycle < 0.65) {
                        val subCycle = (cricketCycle % 0.20)
                        if (subCycle < 0.09) {
                            val carrier = sin(2.0 * Math.PI * 4000.0 * t).toFloat()
                            val buzz = sin(2.0 * Math.PI * 65.0 * t).toFloat()
                            val envelope = sin(Math.PI * (subCycle / 0.09)).toFloat()
                            cricketOut = carrier * (buzz * 0.35f + 0.65f) * envelope * 0.012f * currentCricketsVol
                        }
                    }

                    // --- 4. PROCEDURAL RAIN DROPS IN THE BACKGROUND ---
                    if (activeWeather == Weather.RAIN) {
                        if (Math.random() < 0.0006) {
                            val dropFreq = 1500f + Math.random().toFloat() * 1000f
                            val dropVolume = (0.004f + Math.random() * 0.01f).toFloat()
                            triggerChime(dropFreq, dropVolume)
                        }
                    }

                    // --- 5. TIME-OF-DAY COZY AMBIENT BACKGROUND PAD/DRONE ---
                    val dayOsc1 = sin(2.0 * Math.PI * 196.00 * t).toFloat() // G3
                    val dayOsc2 = sin(2.0 * Math.PI * 246.94 * t).toFloat() // B3
                    val dayPadSwell = 0.5f + 0.5f * sin(2.0 * Math.PI * t / 11.0).toFloat()
                    val dayPad = (dayOsc1 * 0.5f + dayOsc2 * 0.5f) * dayPadSwell * 0.0035f

                    val sunsetOsc1 = sin(2.0 * Math.PI * 220.00 * t).toFloat() // A3
                    val sunsetOsc2 = sin(2.0 * Math.PI * 261.63 * t).toFloat() // C4
                    val sunsetPadSwell = 0.5f + 0.5f * sin(2.0 * Math.PI * t / 13.0).toFloat()
                    val sunsetPad = (sunsetOsc1 * 0.5f + sunsetOsc2 * 0.5f) * sunsetPadSwell * 0.0035f

                    val nightOsc1 = sin(2.0 * Math.PI * 164.81 * t).toFloat() // E3
                    val nightOsc2 = sin(2.0 * Math.PI * 220.00 * t).toFloat() // A3
                    val nightPadSwell = 0.5f + 0.5f * sin(2.0 * Math.PI * t / 15.0).toFloat()
                    val nightPad = (nightOsc1 * 0.5f + nightOsc2 * 0.5f) * nightPadSwell * 0.0045f

                    val ambientPad = when (activeTimeOfDay) {
                        TimeOfDay.DAY -> dayPad
                        TimeOfDay.SUNSET -> sunsetPad
                        TimeOfDay.NIGHT -> nightPad
                    }

                    // Combine all ambient channels
                    var mixedVal = windOut + waterOut + cricketOut + ambientPad

                    // --- 2. ACTIVE CHIME SYNTHESIS (Pentatonic glass bells) ---
                    val chimesIterator = activeChimes.iterator()
                    while (chimesIterator.hasNext()) {
                        val chime = chimesIterator.next()
                        val elapsed = sampleCounter - chime.startSample
                        
                        if (elapsed >= chime.durationSamples) {
                            activeChimes.remove(chime)
                            continue
                        }

                        val progress = elapsed.toDouble() / SAMPLE_RATE.toDouble()
                        
                        // Exponent decay envelope (chime feel)
                        val decay = exp(-progress * 2.2) // Decay over 1.5 seconds
                        val sine = sin(2.0 * Math.PI * chime.freq * progress).toFloat()
                        
                        // Add some warm harmonics
                        val secondHarmonic = sin(4.0 * Math.PI * chime.freq * progress).toFloat() * 0.15f
                        
                        mixedVal += (sine + secondHarmonic) * chime.volume * decay.toFloat()
                    }

                    // Soft saturation clamping to avoid digital clipping
                    if (mixedVal > 1f) mixedVal = 1f
                    if (mixedVal < -1f) mixedVal = -1f

                    // Master volume scaling
                    val masterVolumeMultiplier = 0.35f
                    val sampleShort = (mixedVal * masterVolumeMultiplier * 32767).toInt().toShort()
                    buffer[i] = sampleShort
                    sampleCounter++
                }

                track.write(buffer, 0, buffer.size)
            }
        }
    }

    /**
     * Trigger a cozy tone chime.
     */
    fun triggerChime(freq: Float, volume: Float = 0.15f) {
        val durationSamples = (SAMPLE_RATE * 2.5).toLong() // 2.5 seconds max chime resonance
        activeChimes.add(
            ActiveChime(
                freq = freq,
                startSample = sampleCounter,
                durationSamples = durationSamples,
                volume = volume
            )
        )
    }

    /**
     * High pitched splash chime for fish jumps.
     */
    fun triggerSplashChime() {
        triggerChime(987.77f, 0.25f) // B5
        coroutineScope.launch {
            delay(120)
            triggerChime(1174.66f, 0.20f) // D6
            delay(80)
            triggerChime(1480.00f, 0.12f) // F#6 (extra high spark)
        }
    }

    /**
     * Double heavy reel click/sound effect.
     */
    fun triggerReelClick() {
        // High click sound
        triggerChime(110f, 0.3f)
        coroutineScope.launch {
            delay(40)
            triggerChime(220f, 0.15f)
        }
    }

    private fun triggerRandomChime() {
        // Choose specialized note palettes for each time of day to enrich the auditory experience
        val timeOfDayNotes = when (activeTimeOfDay) {
            TimeOfDay.DAY -> listOf(392.00f, 440.00f, 493.88f, 587.33f, 659.25f, 783.99f, 880.00f, 987.77f, 1174.66f) // G Major (Bright, positive, playful)
            TimeOfDay.SUNSET -> listOf(293.66f, 329.63f, 392.00f, 440.00f, 493.88f, 587.33f, 659.25f, 783.99f) // Warm Sunset (Contemplative & balanced)
            TimeOfDay.NIGHT -> listOf(164.81f, 196.00f, 220.00f, 246.94f, 329.63f, 392.00f, 440.00f, 493.88f) // Deep Night (Sleepy & low-register E minor)
        }
        val randomIdx = (Math.random() * timeOfDayNotes.size).toInt()
        val freq = timeOfDayNotes[randomIdx]
        // Random slight volume variance
        val vol = (0.04f + Math.random() * 0.08f).toFloat()
        triggerChime(freq, vol)
    }

    fun toggleMute() {
        isMuted = !isMuted
    }

    fun isMuted(): Boolean = isMuted

    fun stop() {
        isPlaying = false
        audioJob?.cancel()
        audioJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error when stopping AudioTrack", e)
        }
        audioTrack = null
    }
}
