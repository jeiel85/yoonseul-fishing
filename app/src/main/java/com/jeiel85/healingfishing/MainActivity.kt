package com.jeiel85.healingfishing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.jeiel85.healingfishing.ui.AudioSynthesizer
import com.jeiel85.healingfishing.ui.FishingViewModel
import com.jeiel85.healingfishing.ui.FishingViewModelFactory
import com.jeiel85.healingfishing.ui.HealingFishingGame
import com.jeiel85.healingfishing.ui.theme.HealingFishingTheme

class MainActivity : ComponentActivity() {
    private var audioSynthesizer: AudioSynthesizer? = null
    private lateinit var viewModel: FishingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize custom procedural healing chimes/engine
        audioSynthesizer = AudioSynthesizer(applicationContext)

        // 2. Load the state manager with proper database binding
        val factory = FishingViewModelFactory(application)
        viewModel = ViewModelProvider(this, factory)[FishingViewModel::class.java]

        // 3. Connect audio context
        audioSynthesizer?.let {
            viewModel.setSynthesizer(it)
        }

        setContent {
            HealingFishingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    HealingFishingGame(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Resumes the calming waveline & chimes when game enters foreground
        audioSynthesizer?.start()
    }

    override fun onPause() {
        super.onPause()
        // Sanitize audio context when backgrounded to prevent memory leak/unwanted playback
        audioSynthesizer?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioSynthesizer?.stop()
        audioSynthesizer = null
    }
}
