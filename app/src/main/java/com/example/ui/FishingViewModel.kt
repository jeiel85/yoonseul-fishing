package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

enum class TimeOfDay {
    DAY, SUNSET, NIGHT
}

enum class Weather {
    CLEAR, MIST, RAIN
}

enum class NatureSound {
    WIND, WATER_LAP, CRICKETS
}

enum class FishingState {
    IDLE,       // Sitting on the boat, holding the rod
    CASTING,    // Casting the rod, bobber flying
    WAITING,    // Bobber in water, waiting for fish
    NIBBLE,     // Fish is tasting, bobber dips slightly, ripples appear
    BITE,       // Fish is hooked, bobber goes fully under! PULL NOW!
    REELING,    // Hooked, pulling fish (optional, or automated splash screen)
    SPLASHING,  // Massive splash! The fish leaps out in low-poly 3D!
    CAUGHT,     // Detail card dialog is open
    LOST        // Caught failed, fish swam away
}

enum class AppLanguage {
    KO, EN, JA
}

enum class BaitType(
    val id: String,
    val nameEn: String,
    val nameKo: String,
    val nameJa: String,
    val price: Int,
    val descriptionEn: String,
    val descriptionKo: String,
    val descriptionJa: String,
    val emoji: String
) {
    BASIC("basic", "Plain Paste", "기본 떡밥", "普通練り餌", 0, "Infinite plain bait.", "무제한으로 제공되는 기본 떡밥입니다.", "無限に使える基本的なエサです。", "🧆"),
    WORM("worm", "Lugworm", "갯지렁이 미끼", "ゴカイの餌", 10, "Bite waiting time reduced by 30%.", "물고기가 찌를 건드리는 대기 시간이 30% 단축됩니다.", "一口が30%速くなります。", "🪱"),
    SHRIMP("shrimp", "Krill Shrimps", "크릴새우 미끼", "オキアミの餌", 30, "Rare or legendary rates increased.", "희귀 등급 이상을 낚을 확률이 눈에 띄게 올라갑니다.", "レア以上の出現率が大きくアップします。", "🦐"),
    GOLDEN("golden", "Golden Mash", "황금 고농축 떡밥", "黄金の練り餌", 85, "Significantly triples Mythical / Legendary encounter rates.", "전설 및 특히 신화 등급의 고대 생명체를 만날 확률이 극대화됩니다.", "伝説・神話クラスの出現率が劇的に増加します。", "🪙")
}

class FishingViewModel(
    application: Application,
    private val repository: FishingRepository
) : AndroidViewModel(application) {

    private val _timeOfDay = MutableStateFlow(TimeOfDay.DAY)
    val timeOfDay: StateFlow<TimeOfDay> = _timeOfDay.asStateFlow()

    private val _weather = MutableStateFlow(Weather.CLEAR)
    val weather: StateFlow<Weather> = _weather.asStateFlow()

    private val _natureSound = MutableStateFlow(NatureSound.WATER_LAP)
    val natureSound: StateFlow<NatureSound> = _natureSound.asStateFlow()

    private val _fishingState = MutableStateFlow(FishingState.IDLE)
    val fishingState: StateFlow<FishingState> = _fishingState.asStateFlow()

    private val _caughtFishList = MutableStateFlow<List<CaughtFishEntity>>(emptyList())
    val caughtFishList: StateFlow<List<CaughtFishEntity>> = _caughtFishList.asStateFlow()

    private val _lastCaughtFish = MutableStateFlow<CaughtFishEntity?>(null)
    val lastCaughtFish: StateFlow<CaughtFishEntity?> = _lastCaughtFish.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isEnglish = MutableStateFlow(java.util.Locale.getDefault().language == "en")
    val isEnglish: StateFlow<Boolean> = _isEnglish.asStateFlow()

    private val _language = MutableStateFlow(AppLanguage.KO)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _hasCastBefore = MutableStateFlow(false)
    val hasCastBefore: StateFlow<Boolean> = _hasCastBefore.asStateFlow()

    // Rhythm Mini-game states
    private val _rhythmRingScale = MutableStateFlow(1.0f)
    val rhythmRingScale: StateFlow<Float> = _rhythmRingScale.asStateFlow()

    private val _rhythmHitCount = MutableStateFlow(0)
    val rhythmHitCount: StateFlow<Int> = _rhythmHitCount.asStateFlow()

    private val _rhythmMissCount = MutableStateFlow(0)
    val rhythmMissCount: StateFlow<Int> = _rhythmMissCount.asStateFlow()

    private val _rhythmFeedbackText = MutableStateFlow<String?>(null)
    val rhythmFeedbackText: StateFlow<String?> = _rhythmFeedbackText.asStateFlow()

    private val _rhythmBeatActive = MutableStateFlow(false)
    val rhythmBeatActive: StateFlow<Boolean> = _rhythmBeatActive.asStateFlow()

    // Level System States
    private val prefs = application.getSharedPreferences("healing_fishing_prefs", android.content.Context.MODE_PRIVATE)

    private val _fishingLevel = MutableStateFlow(1)
    val fishingLevel: StateFlow<Int> = _fishingLevel.asStateFlow()

    private val _fishingXp = MutableStateFlow(0)
    val fishingXp: StateFlow<Int> = _fishingXp.asStateFlow()

    private val _coins = MutableStateFlow(0)
    val coins: StateFlow<Int> = _coins.asStateFlow()

    private val _rodLevel = MutableStateFlow(1)
    val rodLevel: StateFlow<Int> = _rodLevel.asStateFlow()

    private val _showLevelUpDialog = MutableStateFlow<Int?>(null)
    val showLevelUpDialog: StateFlow<Int?> = _showLevelUpDialog.asStateFlow()

    // --- Added Fishing Spot States ---
    private val _currentSpot = MutableStateFlow(FishingSpot.WINDY_VALLEY)
    val currentSpot = _currentSpot.asStateFlow()

    // --- Added Quests and Achievement System States ---
    private val _dailyQuestCatchCount = MutableStateFlow(0)
    val dailyQuestCatchCount = _dailyQuestCatchCount.asStateFlow()

    private val _dailyQuestRareCount = MutableStateFlow(0)
    val dailyQuestRareCount = _dailyQuestRareCount.asStateFlow()

    private val _dailyQuestGoldEarned = MutableStateFlow(0)
    val dailyQuestGoldEarned = _dailyQuestGoldEarned.asStateFlow()

    private val _dailyQuestCatchClaimed = MutableStateFlow(false)
    val dailyQuestCatchClaimed = _dailyQuestCatchClaimed.asStateFlow()

    private val _dailyQuestRareClaimed = MutableStateFlow(false)
    val dailyQuestRareClaimed = _dailyQuestRareClaimed.asStateFlow()

    private val _dailyQuestGoldClaimed = MutableStateFlow(false)
    val dailyQuestGoldClaimed = _dailyQuestGoldClaimed.asStateFlow()

    // Achievement claimed states
    private val _achCatch10Claimed = MutableStateFlow(false)
    val achCatch10Claimed = _achCatch10Claimed.asStateFlow()

    private val _achMythicClaimed = MutableStateFlow(false)
    val achMythicClaimed = _achMythicClaimed.asStateFlow()

    private val _achRod3Claimed = MutableStateFlow(false)
    val achRod3Claimed = _achRod3Claimed.asStateFlow()

    private val _achSell20Claimed = MutableStateFlow(false)
    val achSell20Claimed = _achSell20Claimed.asStateFlow()

    private val _achCoins2000Claimed = MutableStateFlow(false)
    val achCoins2000Claimed = _achCoins2000Claimed.asStateFlow()

    // Collection goal milestones
    private val _colMilestone3Claimed = MutableStateFlow(false)
    val colMilestone3Claimed = _colMilestone3Claimed.asStateFlow()

    private val _colMilestone6Claimed = MutableStateFlow(false)
    val colMilestone6Claimed = _colMilestone6Claimed.asStateFlow()

    private val _colMilestone9Claimed = MutableStateFlow(false)
    val colMilestone9Claimed = _colMilestone9Claimed.asStateFlow()

    private val _colMilestone12Claimed = MutableStateFlow(false)
    val colMilestone12Claimed = _colMilestone12Claimed.asStateFlow()

    // Celebration event window state
    private val _currentCelebration = MutableStateFlow<CelebrationData?>(null)
    val currentCelebration = _currentCelebration.asStateFlow()

    // Lifetime stats
    private val _totalFishCaught = MutableStateFlow(0)
    val totalFishCaught = _totalFishCaught.asStateFlow()

    private val _totalFishSold = MutableStateFlow(0)
    val totalFishSold = _totalFishSold.asStateFlow()

    private val _hasCaughtLegendaryOrMythic = MutableStateFlow(false)
    val hasCaughtLegendaryOrMythic = _hasCaughtLegendaryOrMythic.asStateFlow()

    // --- Added In-App Alert State ---
    private val _inAppAlert = MutableStateFlow<NotificationAlert?>(null)
    val inAppAlert = _inAppAlert.asStateFlow()

    // --- Google Play Games Integration Simulation State ---
    private val _isGpgSignedIn = MutableStateFlow(false)
    val isGpgSignedIn = _isGpgSignedIn.asStateFlow()

    // Bait System States and Methods
    private val _activeBait = MutableStateFlow(BaitType.BASIC)
    val activeBait: StateFlow<BaitType> = _activeBait.asStateFlow()

    private val _baitWormCount = MutableStateFlow(0)
    val baitWormCount: StateFlow<Int> = _baitWormCount.asStateFlow()

    private val _baitShrimpCount = MutableStateFlow(0)
    val baitShrimpCount: StateFlow<Int> = _baitShrimpCount.asStateFlow()

    private val _baitGoldenCount = MutableStateFlow(0)
    val baitGoldenCount: StateFlow<Int> = _baitGoldenCount.asStateFlow()

    fun selectBait(bait: BaitType) {
        _activeBait.value = bait
        prefs.edit().putString("active_bait_id", bait.id).apply()
    }

    fun buyBait(bait: BaitType, quantity: Int = 1): Boolean {
        if (bait == BaitType.BASIC) return false
        val cost = bait.price * quantity
        if (_coins.value >= cost) {
            _coins.value -= cost
            prefs.edit().putInt("coins", _coins.value).apply()
            
            when (bait) {
                BaitType.WORM -> {
                    _baitWormCount.value += quantity
                    prefs.edit().putInt("bait_worm_count", _baitWormCount.value).apply()
                }
                BaitType.SHRIMP -> {
                    _baitShrimpCount.value += quantity
                    prefs.edit().putInt("bait_shrimp_count", _baitShrimpCount.value).apply()
                }
                BaitType.GOLDEN -> {
                    _baitGoldenCount.value += quantity
                    prefs.edit().putInt("bait_golden_count", _baitGoldenCount.value).apply()
                }
                else -> {}
            }
            // Select automatically if we had basic selected
            if (_activeBait.value == BaitType.BASIC) {
                selectBait(bait)
            }
            
            // Play neat chime!
            viewModelScope.launch {
                audioSynthesizer?.let { synth ->
                    synth.triggerChime(587.33f, 0.15f) // D5
                    delay(80)
                    synth.triggerChime(880.00f, 0.25f) // A5
                }
            }
            return true
        }
        return false
    }

    fun getBaitCount(bait: BaitType): Int {
        return when (bait) {
            BaitType.BASIC -> 99999
            BaitType.WORM -> _baitWormCount.value
            BaitType.SHRIMP -> _baitShrimpCount.value
            BaitType.GOLDEN -> _baitGoldenCount.value
        }
    }

    fun getXpNeeded(level: Int): Int {
        return level * 100
    }

    fun getUpgradeCost(): Int {
        return _rodLevel.value * 150
    }

    fun upgradeRod(): Boolean {
        val cost = getUpgradeCost()
        if (_coins.value >= cost) {
            _coins.value -= cost
            _rodLevel.value += 1
            
            prefs.edit()
                .putInt("coins", _coins.value)
                .putInt("rod_level", _rodLevel.value)
                .apply()

            // Trigger beautiful rising pentatonic chime to celebrate success!
            viewModelScope.launch {
                audioSynthesizer?.let { synth ->
                    synth.triggerChime(523.25f, 0.15f) // C5
                    delay(100)
                    synth.triggerChime(659.25f, 0.15f) // E5
                    delay(100)
                    synth.triggerChime(783.99f, 0.15f) // G5
                    delay(100)
                    synth.triggerChime(987.77f, 0.15f) // B5
                    delay(100)
                    synth.triggerChime(1046.50f, 0.35f) // C6
                }
            }
            return true
        }
        return false
    }

    fun dismissLevelUpDialog() {
        _showLevelUpDialog.value = null
    }

    // Position of bobber on screen (0f - 1f coordinates relative to water canvas)
    private val _bobberPositionX = MutableStateFlow(0.5f)
    val bobberPositionX: StateFlow<Float> = _bobberPositionX.asStateFlow()

    private val _bobberPositionY = MutableStateFlow(0.7f)
    val bobberPositionY: StateFlow<Float> = _bobberPositionY.asStateFlow()

    // Timer states for the bite timing window
    private var fishingJob: Job? = null
    private var autoTimeCycleJob: Job? = null
    private var autoWeatherCycleJob: Job? = null
    private var autoNatureSoundCycleJob: Job? = null
    var audioSynthesizer: AudioSynthesizer? = null

    init {
        // Load persistent language setting
        val savedLangNum = prefs.getInt("lang_v2", -1)
        if (savedLangNum != -1) {
            _language.value = AppLanguage.values()[savedLangNum]
        } else {
            val systemLanguage = java.util.Locale.getDefault().language
            _language.value = when (systemLanguage) {
                "ja" -> AppLanguage.JA
                "en" -> AppLanguage.EN
                else -> AppLanguage.KO
            }
        }
        _isEnglish.value = (_language.value == AppLanguage.EN)

        // Load persistent stats
        _fishingLevel.value = prefs.getInt("level", 1)
        _fishingXp.value = prefs.getInt("xp", 0)
        _hasCastBefore.value = prefs.getBoolean("has_cast_before", false)
        _coins.value = prefs.getInt("coins", 0)
        _rodLevel.value = prefs.getInt("rod_level", 1)

        _baitWormCount.value = prefs.getInt("bait_worm_count", 0)
        _baitShrimpCount.value = prefs.getInt("bait_shrimp_count", 0)
        _baitGoldenCount.value = prefs.getInt("bait_golden_count", 0)
        val activeBaitId = prefs.getString("active_bait_id", "basic") ?: "basic"
        _activeBait.value = BaitType.values().find { it.id == activeBaitId } ?: BaitType.BASIC

        // Collect caught fish from database
        viewModelScope.launch {
            repository.allCaughtFish.collect { list ->
                _caughtFishList.value = list
            }
        }

        // Load persistent daily quests and achievement statuses
        loadDailyQuestsAndAchievements()

        // Start slow auto daytime cycle (e.g., changes every 90 seconds)
        startAutoTimeCycle()

        // Start auto weather cycle (e.g., shifts/checks every 50 seconds)
        startAutoWeatherCycle()

        // Start background nature sounds auto-cycling
        startAutoNatureSoundCycle()
    }

    fun setSynthesizer(synth: AudioSynthesizer) {
        this.audioSynthesizer = synth
        _isMuted.value = synth.isMuted()
        synth.setNatureSound(_natureSound.value)
        synth.setTimeOfDay(_timeOfDay.value)
        synth.setWeather(_weather.value)
    }

    private fun startAutoTimeCycle() {
        autoTimeCycleJob?.cancel()
        autoTimeCycleJob = viewModelScope.launch {
            while (true) {
                delay(90000) // 90 seconds per time phase
                progressTimeOfDay()
            }
        }
    }

    private fun startAutoWeatherCycle() {
        autoWeatherCycleJob?.cancel()
        autoWeatherCycleJob = viewModelScope.launch {
            while (true) {
                delay(50000) // Evaluate weather every 50 seconds
                val roll = Random.nextFloat()
                val nextWeather = when {
                    roll < 0.45f -> Weather.CLEAR
                    roll < 0.75f -> Weather.MIST
                    else -> Weather.RAIN
                }
                if (_weather.value != nextWeather) {
                    _weather.value = nextWeather
                    audioSynthesizer?.setWeather(nextWeather)
                    playWeatherChangeSound(nextWeather)
                }
            }
        }
    }

    private fun startAutoNatureSoundCycle() {
        autoNatureSoundCycleJob?.cancel()
        autoNatureSoundCycleJob = viewModelScope.launch {
            while (true) {
                delay(75000) // Cycles through nature sounds every 75 seconds
                progressNatureSound()
            }
        }
    }

    fun progressNatureSound() {
        val nextSound = when (_natureSound.value) {
            NatureSound.WATER_LAP -> NatureSound.WIND
            NatureSound.WIND -> NatureSound.CRICKETS
            NatureSound.CRICKETS -> NatureSound.WATER_LAP
        }
        setNatureSound(nextSound)
    }

    fun setNatureSound(sound: NatureSound) {
        _natureSound.value = sound
        audioSynthesizer?.setNatureSound(sound)
        
        // Cozy brief chime for atmosphere transitions
        audioSynthesizer?.let { synth ->
            when (sound) {
                NatureSound.WATER_LAP -> synth.triggerChime(493.88f, 0.08f) // B4
                NatureSound.WIND -> synth.triggerChime(587.33f, 0.08f) // D5
                NatureSound.CRICKETS -> synth.triggerChime(659.25f, 0.08f) // E5
            }
        }
    }

    fun progressWeather() {
        val nextWeather = when (_weather.value) {
            Weather.CLEAR -> Weather.MIST
            Weather.MIST -> Weather.RAIN
            Weather.RAIN -> Weather.CLEAR
        }
        _weather.value = nextWeather
        audioSynthesizer?.setWeather(nextWeather)
        playWeatherChangeSound(nextWeather)
    }

    private fun playWeatherChangeSound(w: Weather) {
        audioSynthesizer?.let { synth ->
            when (w) {
                Weather.CLEAR -> {
                    synth.triggerChime(659.25f, 0.08f) // E5
                }
                Weather.MIST -> {
                    synth.triggerChime(523.25f, 0.08f) // C5
                }
                Weather.RAIN -> {
                    synth.triggerChime(349.23f, 0.08f) // F4
                }
            }
        }
    }

    fun progressTimeOfDay() {
        val nextTime = when (_timeOfDay.value) {
            TimeOfDay.DAY -> TimeOfDay.SUNSET
            TimeOfDay.SUNSET -> TimeOfDay.NIGHT
            TimeOfDay.NIGHT -> TimeOfDay.DAY
        }
        _timeOfDay.value = nextTime
        audioSynthesizer?.setTimeOfDay(nextTime)
        // Warm synth chimes on time shift
        playTimeShiftSound(nextTime)
    }

    fun setTimeOfDay(time: TimeOfDay) {
        _timeOfDay.value = time
        audioSynthesizer?.setTimeOfDay(time)
        playTimeShiftSound(time)
    }

    private fun playTimeShiftSound(time: TimeOfDay) {
        audioSynthesizer?.let { synth ->
            when (time) {
                TimeOfDay.DAY -> {
                    synth.triggerChime(587.33f, 0.15f) // D5
                    synth.triggerChime(783.99f, 0.10f) // G5
                }
                TimeOfDay.SUNSET -> {
                    synth.triggerChime(440.00f, 0.15f) // A4
                    synth.triggerChime(659.25f, 0.10f) // E5
                }
                TimeOfDay.NIGHT -> {
                    synth.triggerChime(329.63f, 0.15f) // E4
                    synth.triggerChime(493.88f, 0.12f) // B4
                }
            }
        }
    }

    fun toggleMute() {
        audioSynthesizer?.let { synth ->
            synth.toggleMute()
            _isMuted.value = synth.isMuted()
        }
    }

    fun toggleLanguage() {
        val nextLang = when (_language.value) {
            AppLanguage.KO -> AppLanguage.EN
            AppLanguage.EN -> AppLanguage.JA
            AppLanguage.JA -> AppLanguage.KO
        }
        _language.value = nextLang
        _isEnglish.value = (nextLang == AppLanguage.EN)
        prefs.edit().putInt("lang_v2", nextLang.ordinal).apply()
    }

    /**
     * Handle general screen tap. This single point of interaction enables
     * highly intuitive, minimalist mechanics without unneeded buttons.
     */
    fun handleScreenTap(tapX: Float = 0.5f, tapY: Float = 0.65f) {
        when (_fishingState.value) {
            FishingState.IDLE -> {
                castLine(tapX, tapY)
            }
            FishingState.CASTING -> {
                // Do nothing while casting is animating
            }
            FishingState.WAITING -> {
                // Taping too early before fish bites -> line retrieved empty
                cancelFishing(lost = false)
            }
            FishingState.NIBBLE -> {
                // Taping on nibble -> fish escapes!
                cancelFishing(lost = true)
            }
            FishingState.BITE -> {
                // Perfect hit! Hook the fish!
                reelIn()
            }
            FishingState.REELING -> {
                // Interactive rhythm mini-game tap!
                handleRhythmTap()
            }
            FishingState.SPLASHING -> {
                // Tap during the jumping splash phase skips straight to stats dialog
                showCaughtDialog()
            }
            FishingState.CAUGHT -> {
                // Dismiss details dialog back to peaceful idle
                _fishingState.value = FishingState.IDLE
            }
            FishingState.LOST -> {
                // Tap on fail screen resets to peaceful idle
                _fishingState.value = FishingState.IDLE
            }
        }
    }

    private fun castLine(targetX: Float, targetY: Float) {
        if (!_hasCastBefore.value) {
            _hasCastBefore.value = true
            prefs.edit().putBoolean("has_cast_before", true).apply()
        }
        _fishingState.value = FishingState.CASTING
        // Bind bobber target coordinate inside water zone
        _bobberPositionX.value = targetX.coerceIn(0.15f, 0.85f)
        _bobberPositionY.value = targetY.coerceIn(0.55f, 0.85f)

        audioSynthesizer?.triggerReelClick()

        // Deduct bait count if premium bait is selected
        val active = _activeBait.value
        if (active != BaitType.BASIC) {
            val count = getBaitCount(active)
            if (count > 0) {
                when (active) {
                    BaitType.WORM -> {
                        _baitWormCount.value--
                        prefs.edit().putInt("bait_worm_count", _baitWormCount.value).apply()
                    }
                    BaitType.SHRIMP -> {
                        _baitShrimpCount.value--
                        prefs.edit().putInt("bait_shrimp_count", _baitShrimpCount.value).apply()
                    }
                    BaitType.GOLDEN -> {
                        _baitGoldenCount.value--
                        prefs.edit().putInt("bait_golden_count", _baitGoldenCount.value).apply()
                    }
                    else -> {}
                }
            } else {
                // Out of specialized baits, fallback to basic bait!
                selectBait(BaitType.BASIC)
            }
        }

        fishingJob?.cancel()
        fishingJob = viewModelScope.launch {
            // Cast animation takes 1.2 seconds
            delay(1200)
            
            // Splash sound when bobber hits water
            audioSynthesizer?.triggerSplashChime()
            _fishingState.value = FishingState.WAITING

            // --- FASCINATING FISHING WAITING CYCLE ---
            // Wait 3 to 7 seconds before nibbles occur (upgraded rod level shortens wait times)
            val waitReduction = (_rodLevel.value - 1) * 350L
            val rawTime = Random.nextLong(3000, 7000) - waitReduction
            val patientWaitTime = if (_activeBait.value == BaitType.WORM) {
                (rawTime * 0.65f).toLong().coerceAtLeast(1000L) // worm bait speeds it up significantly!
            } else {
                rawTime.coerceAtLeast(1500L)
            }
            delay(patientWaitTime)

            if (_fishingState.value != FishingState.WAITING) return@launch

            // 1. Nibble phase (1-3 small nibbles)
            val nibbleCount = Random.nextInt(1, 4)
            for (i in 0 until nibbleCount) {
                if (_fishingState.value != FishingState.WAITING) return@launch
                
                // Set status to NIBBLE
                _fishingState.value = FishingState.NIBBLE
                audioSynthesizer?.triggerChime(392.00f, 0.05f) // Soft sound
                
                delay(Random.nextLong(400, 900))
                
                if (_fishingState.value != FishingState.NIBBLE) return@launch
                // Recover back to waiting
                _fishingState.value = FishingState.WAITING
                
                // Interval between nibbles
                delay(Random.nextLong(1000, 2500))
            }

            // 2. Real BITE phase!
            if (_fishingState.value != FishingState.WAITING) return@launch
            _fishingState.value = FishingState.BITE
            
            // Major chime and splash indicator
            audioSynthesizer?.triggerSplashChime()
            
            // Bite window lasts for 1.5 to 2.2 seconds based on rarity of current time of day
            val biteWindow = Random.nextLong(1500, 2200)
            delay(biteWindow)

            // If still in BITE, user missed the chance! Fish swam away.
            if (_fishingState.value == FishingState.BITE) {
                _fishingState.value = FishingState.LOST
                audioSynthesizer?.triggerChime(220f, 0.15f) // Disappointing low chime
                
                delay(3000)
                if (_fishingState.value == FishingState.LOST) {
                    _fishingState.value = FishingState.IDLE
                }
            }
        }
    }

    private fun handleRhythmTap() {
        if (!_rhythmBeatActive.value || _rhythmFeedbackText.value != null) return

        val scale = _rhythmRingScale.value
        // Sweet spot is centering around 0.35f (upgraded rod widens the sweet spot window)
        val tolerance = ((_rodLevel.value - 1) * 0.012f).coerceAtMost(0.08f)
        val perfectMin = 0.23f - tolerance
        val perfectMax = 0.43f + tolerance
        val goodMin = 0.11f - tolerance * 1.5f
        val goodMax = 0.52f + tolerance * 1.5f

        if (scale in perfectMin..perfectMax) {
            _rhythmFeedbackText.value = "PERFECT!"
            _rhythmHitCount.value++
            audioSynthesizer?.triggerChime(880.00f, 0.1f) // high A
            viewModelScope.launch {
                delay(80)
                audioSynthesizer?.triggerChime(1046.50f, 0.1f) // high C
            }
        } else if (scale in goodMin..goodMax) {
            _rhythmFeedbackText.value = "GOOD!"
            _rhythmHitCount.value++
            audioSynthesizer?.triggerChime(659.25f, 0.1f) // E5
        } else {
            _rhythmFeedbackText.value = "MISS"
            _rhythmMissCount.value++
            audioSynthesizer?.triggerChime(220.00f, 0.12f) // low low A
        }
    }

    private fun reelIn() {
        fishingJob?.cancel()
        _fishingState.value = FishingState.REELING
        _rhythmHitCount.value = 0
        _rhythmMissCount.value = 0
        _rhythmFeedbackText.value = null
        _rhythmBeatActive.value = false
        _rhythmRingScale.value = 1.0f

        audioSynthesizer?.triggerReelClick()

        fishingJob = viewModelScope.launch {
            // Play up to 5 rhythm beats. Correctly hitting 3 times catches the fish.
            // 3 misses means the fish gets away!
            while (_rhythmHitCount.value < 3 && _rhythmMissCount.value < 3) {
                delay(400)
                if (_fishingState.value != FishingState.REELING) break

                runRhythmBeat()
            }

            if (_fishingState.value != FishingState.REELING) return@launch

            if (_rhythmHitCount.value >= 3) {
                // Success! Catch the fish!
                val fish = rollFishSpecies(_timeOfDay.value, _weather.value)
                
                val length = Random.nextDouble(fish.baseLengthMin.toDouble(), fish.baseLengthMax.toDouble()).toFloat()
                val weight = Random.nextDouble(fish.baseWeightMin.toDouble(), fish.baseWeightMax.toDouble()).toFloat()
                
                val mappedTime = when (_timeOfDay.value) {
                    TimeOfDay.DAY -> "day"
                    TimeOfDay.SUNSET -> "sunset"
                    TimeOfDay.NIGHT -> "night"
                }

                val entity = CaughtFishEntity(
                    speciesId = fish.id,
                    weight = weight,
                    length = length,
                    timeOfDay = mappedTime
                )

                _lastCaughtFish.value = entity
                
                // Trigger beautiful double high-splash sound
                audioSynthesizer?.triggerSplashChime()
                _fishingState.value = FishingState.SPLASHING

                delay(2200)
                showCaughtDialog()
            } else {
                // Fail! The fish broke free
                _fishingState.value = FishingState.LOST
                audioSynthesizer?.triggerChime(220f, 0.15f) // Disappointing low chime
                
                delay(3000)
                if (_fishingState.value == FishingState.LOST) {
                    _fishingState.value = FishingState.IDLE
                }
            }
        }
    }

    private suspend fun runRhythmBeat() {
        _rhythmRingScale.value = 1.0f
        _rhythmBeatActive.value = true
        _rhythmFeedbackText.value = null

        // Contracting the ring from 1.0 to 0.0 over 1400 milliseconds (smooth timing)
        val durationMs = 1400L
        val steps = 35
        val stepDelay = durationMs / steps

        var hasFeedbackOccurred = false

        for (i in 0..steps) {
            if (_fishingState.value != FishingState.REELING) break
            if (_rhythmFeedbackText.value != null) {
                hasFeedbackOccurred = true
            }

            _rhythmRingScale.value = 1.0f - (i.toFloat() / steps)
            
            // Soft click sound at regular tick phases to guide the player rhythmically
            if (i == 10 || i == 20) {
                audioSynthesizer?.triggerReelClick()
            }
            
            delay(stepDelay)
        }

        _rhythmBeatActive.value = false

        // Missing the beat completely counts as a MISS
        if (!hasFeedbackOccurred && _fishingState.value == FishingState.REELING) {
            _rhythmFeedbackText.value = "MISS"
            _rhythmMissCount.value++
            audioSynthesizer?.triggerChime(220f, 0.12f)
            delay(400)
        } else {
            // Keep feedback visible briefly for the next beat transition
            delay(300)
        }
    }

    private fun showCaughtDialog() {
        fishingJob?.cancel()
        val entity = _lastCaughtFish.value ?: return
        
        _fishingState.value = FishingState.CAUGHT
        
        // Persist to Room Database!
        viewModelScope.launch {
            repository.insert(entity)
            
            // Award experience points when fish is caught successfully
            val fish = FishSpecies.find(entity.speciesId)
            if (fish != null) {
                addExperienceForFish(fish)
                checkAndUpdateCatchStats(fish, entity.length, entity.weight)
            }
        }
    }

    private fun addExperienceForFish(fish: FishSpecies) {
        val xpGain = when (fish.rarity) {
            "일반" -> 20
            "희귀" -> 40
            "전설" -> 150
            "신화" -> 500
            else -> 15
        }
        addXp(xpGain)

        // Award Coins based on rarity, boosted by current Rod level
        val baseCoins = when (fish.rarity) {
            "일반" -> 15
            "희귀" -> 40
            "전설" -> 120
            "신화" -> 450
            else -> 10
        }
        val rodMultiplier = 1.0f + 0.15f * (_rodLevel.value - 1)
        val coinsEarned = (baseCoins * rodMultiplier).toInt()
        
        _coins.value += coinsEarned
        prefs.edit().putInt("coins", _coins.value).apply()
    }

    private fun addXp(amount: Int) {
        var currentXp = _fishingXp.value + amount
        var currentLevel = _fishingLevel.value
        var leveledUp = false

        while (currentXp >= getXpNeeded(currentLevel)) {
            currentXp -= getXpNeeded(currentLevel)
            currentLevel++
            leveledUp = true
        }

        _fishingXp.value = currentXp
        _fishingLevel.value = currentLevel

        prefs.edit()
            .putInt("level", currentLevel)
            .putInt("xp", currentXp)
            .apply()

        if (leveledUp) {
            // Play a pleasant level up chime chime chain
            viewModelScope.launch {
                audioSynthesizer?.let { synth ->
                    synth.triggerChime(523.25f, 0.15f) // C5
                    delay(120)
                    synth.triggerChime(659.25f, 0.15f) // E5
                    delay(120)
                    synth.triggerChime(783.99f, 0.15f) // G5
                    delay(120)
                    synth.triggerChime(1046.50f, 0.30f) // C6
                }
            }
            _showLevelUpDialog.value = currentLevel
        }
    }

    private fun cancelFishing(lost: Boolean) {
        fishingJob?.cancel()
        if (lost) {
            _fishingState.value = FishingState.LOST
            audioSynthesizer?.triggerChime(220f, 0.15f)
            viewModelScope.launch {
                delay(2500)
                if (_fishingState.value == FishingState.LOST) {
                    _fishingState.value = FishingState.IDLE
                }
            }
        } else {
            _fishingState.value = FishingState.IDLE
        }
    }

    /**
     * Probability calculation based on active environmental lighting / Time of Day and Weather
     */
    private fun rollFishSpecies(time: TimeOfDay, w: Weather = Weather.CLEAR): FishSpecies {
        val candidates = FishSpecies.list
        val timeString = when (time) {
            TimeOfDay.DAY -> "낮"
            TimeOfDay.SUNSET -> "노을"
            TimeOfDay.NIGHT -> "밤"
        }

        // Weight fish species by rarity, optimal times, and weather conditions
        val scoredList = candidates.map { fish ->
            var score = 1.0
            
            // Boost if matches optimal time
            if (fish.optimalTime == timeString) {
                score *= 3.0
            } else if (fish.optimalTime == "언제나") {
                score *= 1.5
            } else {
                score *= 0.3 // rare to catch out of time
            }

            // Boost if matches optimal weather
            val isOptimalWeather = when (w) {
                Weather.CLEAR -> fish.id in listOf("gold_crucian", "shiri", "rainbow_trout", "koi")
                Weather.MIST -> fish.id in listOf("mandarin_fish", "gobs", "cherry_salmon", "sunset_butterfly")
                Weather.RAIN -> fish.id in listOf("catfish", "sweetfish", "moonlight_catfish", "star_whale")
            }
            if (isOptimalWeather) {
                score *= 4.0 // significantly reinforce probability in preferred weather
            } else {
                score *= 0.4
            }

            // Adjust by rarity factor, boosted by active specialized bait
            val rarityWeight = when (fish.rarity) {
                "일반" -> 1.0
                "희귀" -> {
                    val multiplier = if (_activeBait.value == BaitType.SHRIMP) 2.2 else 1.0
                    0.4 * multiplier
                }
                "전설" -> {
                    val multiplier = when (_activeBait.value) {
                        BaitType.SHRIMP -> 1.8
                        BaitType.GOLDEN -> 3.6
                        else -> 1.0
                    }
                    0.1 * multiplier
                }
                "신화" -> {
                    val multiplier = when (_activeBait.value) {
                        BaitType.SHRIMP -> 1.5
                        BaitType.GOLDEN -> 5.5
                        else -> 1.0
                    }
                    0.02 * multiplier
                }
                else -> 1.0
            }
            score *= rarityWeight
            
            // Apply boost based on active fishing spot
            val currentSpotId = _currentSpot.value.id
            var spotMultiplier = 1.0
            when (currentSpotId) {
                "windy_valley" -> {
                    // Windy Valley boosts typical peaceful freshwater favorites
                    if (fish.id in listOf("gold_crucian", "rainbow_trout", "sweetfish", "shiri")) {
                        spotMultiplier = 2.5
                    }
                }
                "galaxy_lake" -> {
                    // Galaxy Lake boosts deep mysterious and nocturnal fish
                    if (fish.id in listOf("mandarin_fish", "koi", "gobs", "moonlight_catfish")) {
                        spotMultiplier = 2.5
                    }
                }
                "wave_beach" -> {
                    // Wave Beach boosts tropical ocean style fish & legendary marine life
                    if (fish.id in listOf("sunset_butterfly", "star_whale", "catfish", "cherry_salmon")) {
                        spotMultiplier = 2.5
                    }
                }
            }
            score *= spotMultiplier
            
            Pair(fish, score)
        }

        val totalWeight = scoredList.sumOf { it.second }
        var randomPos = Random.nextDouble(0.0, totalWeight)

        for (pair in scoredList) {
            randomPos -= pair.second
            if (randomPos <= 0.0) {
                return pair.first
            }
        }
        return candidates.first()
    }

    fun releaseAllCaughtFish() {
        viewModelScope.launch {
            repository.clear()
            _lastCaughtFish.value = null
            _fishingLevel.value = 1
            _fishingXp.value = 0
            _hasCastBefore.value = false
            _coins.value = 0
            _rodLevel.value = 1
            prefs.edit()
                .putInt("level", 1)
                .putInt("xp", 0)
                .putBoolean("has_cast_before", false)
                .putInt("coins", 0)
                .putInt("rod_level", 1)
                .apply()
        }
    }

    fun getFishValue(entity: CaughtFishEntity): Int {
        val fish = FishSpecies.find(entity.speciesId) ?: return 10
        val basePrice = when (fish.rarity) {
            "일반" -> 20
            "희귀" -> 50
            "전설" -> 200
            "신화" -> 600
            else -> 10
        }
        val multiplier = if (fish.baseLengthMin > 0) entity.length / fish.baseLengthMin else 1.0f
        return (basePrice * multiplier).toInt().coerceAtLeast(basePrice)
    }

    fun sellFish(entity: CaughtFishEntity) {
        val value = getFishValue(entity)
        _coins.value += value
        prefs.edit().putInt("coins", _coins.value).apply()
        trackSaleStats(value, 1)
        
        viewModelScope.launch {
            repository.delete(entity.id)
            audioSynthesizer?.let { synth ->
                synth.triggerChime(783.99f, 0.12f) // G5
                delay(100)
                synth.triggerChime(1046.50f, 0.15f) // C6
            }
        }
    }

    fun sellAllFish(list: List<CaughtFishEntity>) {
        if (list.isEmpty()) return
        var totalGain = 0
        list.forEach { entity ->
            totalGain += getFishValue(entity)
        }
        _coins.value += totalGain
        prefs.edit().putInt("coins", _coins.value).apply()
        trackSaleStats(totalGain, list.size)

        viewModelScope.launch {
            repository.clear()
            audioSynthesizer?.let { synth ->
                synth.triggerChime(523.25f, 0.1f) // C5
                delay(80)
                synth.triggerChime(659.25f, 0.1f) // E5
                delay(80)
                synth.triggerChime(783.99f, 0.1f) // G5
                delay(80)
                synth.triggerChime(1046.50f, 0.25f) // C6
            }
        }
    }

    private fun trackSaleStats(value: Int, fishCount: Int) {
        _totalFishSold.value += fishCount
        prefs.edit().putInt("total_fish_sold_count", _totalFishSold.value).apply()

        if (_dailyQuestGoldEarned.value < 120) {
            _dailyQuestGoldEarned.value = (_dailyQuestGoldEarned.value + value).coerceAtMost(120)
            prefs.edit().putInt("daily_quest_gold_earned", _dailyQuestGoldEarned.value).apply()
        }
    }

    private fun loadDailyQuestsAndAchievements() {
        // Load custom lifetime stats
        _totalFishCaught.value = prefs.getInt("total_fish_caught_count", 0)
        _totalFishSold.value = prefs.getInt("total_fish_sold_count", 0)
        _hasCaughtLegendaryOrMythic.value = prefs.getBoolean("has_caught_legend_myth", false)

        _currentSpot.value = FishingSpot.values().find { it.id == prefs.getString("current_spot_id", "windy_valley") } ?: FishingSpot.WINDY_VALLEY

        // Check if day changed
        val sdf = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
        val todayStr = sdf.format(java.util.Date())
        val lastSavedDate = prefs.getString("daily_quest_date", "") ?: ""

        if (lastSavedDate != todayStr) {
            // New day! Reset daily quests progress and claimed statuses
            prefs.edit().apply {
                putString("daily_quest_date", todayStr)
                putInt("daily_quest_catch_count", 0)
                putInt("daily_quest_rare_count", 0)
                putInt("daily_quest_gold_earned", 0)
                putBoolean("daily_quest_catch_claimed", false)
                putBoolean("daily_quest_rare_claimed", false)
                putBoolean("daily_quest_gold_claimed", false)
                apply()
            }
        }

        // Load daily progresses
        _dailyQuestCatchCount.value = prefs.getInt("daily_quest_catch_count", 0)
        _dailyQuestRareCount.value = prefs.getInt("daily_quest_rare_count", 0)
        _dailyQuestGoldEarned.value = prefs.getInt("daily_quest_gold_earned", 0)
        
        _dailyQuestCatchClaimed.value = prefs.getBoolean("daily_quest_catch_claimed", false)
        _dailyQuestRareClaimed.value = prefs.getBoolean("daily_quest_rare_claimed", false)
        _dailyQuestGoldClaimed.value = prefs.getBoolean("daily_quest_gold_claimed", false)

        // Load achievement claim statuses
        _achCatch10Claimed.value = prefs.getBoolean("ach_catch_10_claimed", false)
        _achMythicClaimed.value = prefs.getBoolean("ach_mythic_claimed", false)
        _achRod3Claimed.value = prefs.getBoolean("ach_rod_3_claimed", false)
        _achSell20Claimed.value = prefs.getBoolean("ach_sell_20_claimed", false)
        _achCoins2000Claimed.value = prefs.getBoolean("ach_coins_2000_claimed", false)

        // Load collection milestone claim statuses
        _colMilestone3Claimed.value = prefs.getBoolean("col_milestone_3_claimed", false)
        _colMilestone6Claimed.value = prefs.getBoolean("col_milestone_6_claimed", false)
        _colMilestone9Claimed.value = prefs.getBoolean("col_milestone_9_claimed", false)
        _colMilestone12Claimed.value = prefs.getBoolean("col_milestone_12_claimed", false)
    }

    private fun checkAndUpdateCatchStats(fish: FishSpecies, length: Float, weight: Float) {
        // Increment lifetime total
        _totalFishCaught.value += 1
        prefs.edit().putInt("total_fish_caught_count", _totalFishCaught.value).apply()

        // Increment daily count
        if (_dailyQuestCatchCount.value < 3) {
            _dailyQuestCatchCount.value += 1
            prefs.edit().putInt("daily_quest_catch_count", _dailyQuestCatchCount.value).apply()
        }

        // Check if rare or higher
        val isRareOrHigher = fish.rarity in listOf("희귀", "전설", "신화")
        if (isRareOrHigher && _dailyQuestRareCount.value < 1) {
            _dailyQuestRareCount.value += 1
            prefs.edit().putInt("daily_quest_rare_count", _dailyQuestRareCount.value).apply()
        }

        // Check for legendary or mythic
        if (fish.rarity in listOf("전설", "신화")) {
            _hasCaughtLegendaryOrMythic.value = true
            prefs.edit().putBoolean("has_caught_legend_myth", true).apply()
        }

        // Show In-App Alert and Local Notification
        val nameToDisplay = if (language.value == AppLanguage.EN) fish.nameEn else fish.name
        sendLocalNotification(nameToDisplay, length, weight)
        
        val alert = NotificationAlert(
            title = if (language.value == AppLanguage.KO) "🎣 대어 획득 편지!" else if (language.value == AppLanguage.JA) "🎣 釣り上げました！" else "🎣 Big Catch!",
            message = if (language.value == AppLanguage.KO) {
                "${fish.name} (${String.format("%.1f", length)}cm) 획득 성공!"
            } else if (language.value == AppLanguage.JA) {
                "${fish.name} (${String.format("%.1f", length)}cm) 獲得成功！"
            } else {
                "${fish.nameEn} (${String.format("%.1f", length)}cm) caught successfully!"
            },
            emoji = "🐠",
            color = Color(fish.color.toArgb())
        )
        _inAppAlert.value = alert
        
        viewModelScope.launch {
            delay(4000)
            if (_inAppAlert.value == alert) {
                _inAppAlert.value = null
            }
        }
    }

    fun dismissInAppAlert() {
        _inAppAlert.value = null
    }

    fun toggleGpgSignIn() {
        _isGpgSignedIn.value = !_isGpgSignedIn.value
        playRewardChime()
    }

    fun selectSpot(spot: FishingSpot) {
        _currentSpot.value = spot
        prefs.edit().putString("current_spot_id", spot.id).apply()
        
        // Trigger tranquil ripple theme chime
        audioSynthesizer?.let { synth ->
            viewModelScope.launch {
                synth.triggerChime(293.66f, 0.15f) // D4
                delay(150)
                synth.triggerChime(392.00f, 0.20f) // G4
                delay(150)
                synth.triggerChime(587.33f, 0.12f) // D5
            }
        }
    }

    fun triggerSoundChime(freq: Float, volume: Float = 0.15f) {
        audioSynthesizer?.triggerChime(freq, volume)
    }

    private fun sendLocalNotification(fishName: String, length: Float, weight: Float) {
        try {
            val context = getApplication<Application>().applicationContext
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "healing_fishing_channel",
                    "Healing Fishing Game",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notification channel for healing catches"
                }
                notificationManager.createNotificationChannel(channel)
            }
            
            val title = if (language.value == AppLanguage.KO) "🎣 대어 어획 성공!" else if (language.value == AppLanguage.JA) "🎣 大物ゲット！" else "🎣 Legendary Catch!"
            val content = if (language.value == AppLanguage.KO) {
                "방금 $fishName (${String.format("%.1f", length)}cm)을(를) 낚았습니다! 도감에서 확인해보세요."
            } else if (language.value == AppLanguage.JA) {
                "今 $fishName (${String.format("%.1f", length)}cm)を釣り上げました！図鑑で確認してみましょう。"
            } else {
                "Just caught $fishName (${String.format("%.1f", length)}cm)! Check it in your book."
            }
            
            val builder = NotificationCompat.Builder(context, "healing_fishing_channel")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                
            notificationManager.notify(777, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun claimDailyQuest(questType: String) {
        when (questType) {
            "catch" -> {
                if (_dailyQuestCatchCount.value >= 3 && !_dailyQuestCatchClaimed.value) {
                    _dailyQuestCatchClaimed.value = true
                    prefs.edit().putBoolean("daily_quest_catch_claimed", true).apply()
                    _coins.value += 50
                    addXp(50)
                    _baitWormCount.value += 2
                    saveBaitCountsToPrefs()
                    
                    triggerCelebration(CelebrationData(
                        id = "quest_catch",
                        titleKo = "일일 미션 완료: 유유자적 물놀이",
                        titleEn = "Daily Quest Claimed: Leisurely Catch",
                        descriptionKo = "오늘 아무 물고기나 3마리 포획하기 목표를 정성스레 달성했습니다.",
                        descriptionEn = "You have successfully caught 3 fish of any kind today.",
                        type = "DAILY_QUEST",
                        rewards = listOf(
                            CelebrationReward("coins", 50, "50 골드", "50 Gold"),
                            CelebrationReward("xp", 50, "경험치 50 XP", "50 XP"),
                            CelebrationReward("bait_worm", 2, "갯지렁이 미끼 2개", "2 Worm Baits")
                        ),
                        badgeEmoji = "🎯"
                    ))
                }
            }
            "rare" -> {
                if (_dailyQuestRareCount.value >= 1 && !_dailyQuestRareClaimed.value) {
                    _dailyQuestRareClaimed.value = true
                    prefs.edit().putBoolean("daily_quest_rare_claimed", true).apply()
                    _coins.value += 80
                    addXp(80)
                    _baitShrimpCount.value += 1
                    saveBaitCountsToPrefs()
                    
                    triggerCelebration(CelebrationData(
                        id = "quest_rare",
                        titleKo = "일일 미션 완료: 희귀종 발견!",
                        titleEn = "Daily Quest Claimed: Rare Sighting",
                        descriptionKo = "오늘 희귀 등급 이상의 신선한 물고기를 1마리 이상 건져 올렸습니다.",
                        descriptionEn = "You have successfully caught at least 1 Rare or higher tier fish today.",
                        type = "DAILY_QUEST",
                        rewards = listOf(
                            CelebrationReward("coins", 80, "80 골드", "80 Gold"),
                            CelebrationReward("xp", 80, "경험치 80 XP", "80 XP"),
                            CelebrationReward("bait_shrimp", 1, "크릴새우 미끼 1개", "1 Shrimp Bait")
                        ),
                        badgeEmoji = "🌟"
                    ))
                }
            }
            "gold" -> {
                if (_dailyQuestGoldEarned.value >= 120 && !_dailyQuestGoldClaimed.value) {
                    _dailyQuestGoldClaimed.value = true
                    prefs.edit().putBoolean("daily_quest_gold_claimed", true).apply()
                    _coins.value += 100
                    addXp(100)
                    _baitGoldenCount.value += 1
                    saveBaitCountsToPrefs()
                    
                    triggerCelebration(CelebrationData(
                        id = "quest_gold",
                        titleKo = "일일 미션 완료: 물결 위의 부귀",
                        titleEn = "Daily Quest Claimed: Wealth on the Waves",
                        descriptionKo = "오늘 물고기 골드 매각 수입 합계가 120골드 이상을 돌파하였습니다.",
                        descriptionEn = "You have earned 120 or more gold through selling fish today.",
                        type = "DAILY_QUEST",
                        rewards = listOf(
                            CelebrationReward("coins", 100, "100 골드", "100 Gold"),
                            CelebrationReward("xp", 100, "경험치 100 XP", "100 XP"),
                            CelebrationReward("bait_golden", 1, "황금 고농축 떡밥 1개", "1 Golden Bait")
                        ),
                        badgeEmoji = "💰"
                    ))
                }
            }
        }
        prefs.edit().putInt("coins", _coins.value).apply()
    }

    private fun saveBaitCountsToPrefs() {
        prefs.edit().apply {
            putInt("bait_worm_count", _baitWormCount.value)
            putInt("bait_shrimp_count", _baitShrimpCount.value)
            putInt("bait_golden_count", _baitGoldenCount.value)
            apply()
        }
    }

    private fun playRewardChime() {
        audioSynthesizer?.let { synth ->
            viewModelScope.launch {
                synth.triggerChime(523.25f, 0.15f) // C5
                delay(120)
                synth.triggerChime(659.25f, 0.15f) // E5
                delay(120)
                synth.triggerChime(783.99f, 0.15f) // G5
                delay(120)
                synth.triggerChime(1046.50f, 0.25f) // C6
            }
        }
    }

    fun dismissCelebration() {
        _currentCelebration.value = null
    }

    fun triggerCelebration(data: CelebrationData) {
        _currentCelebration.value = data
        playRewardChime()
    }

    fun claimCollectionMilestone(milestone: Int) {
        val uniqueCount = _caughtFishList.value.map { it.speciesId }.distinct().size
        when (milestone) {
            3 -> {
                if (uniqueCount >= 3 && !_colMilestone3Claimed.value) {
                    _colMilestone3Claimed.value = true
                    prefs.edit().putBoolean("col_milestone_3_claimed", true).apply()
                    _coins.value += 200
                    _baitWormCount.value += 2
                    saveBaitCountsToPrefs()
                    
                    triggerCelebration(CelebrationData(
                        id = "col_milestone_3",
                        titleKo = "도감 마일스톤 달성: 입문 소집가",
                        titleEn = "Collection Goal: Novice Collector",
                        descriptionKo = "물고기 도감에 3종 이상의 서로 다른 어류를 아름답게 기록하셨습니다.",
                        descriptionEn = "You have recorded 3 or more distinct species in your log book.",
                        type = "COLLECTION",
                        rewards = listOf(
                            CelebrationReward("coins", 200, "200 골드", "200 Gold"),
                            CelebrationReward("bait_worm", 2, "갯지렁이 미끼 2개", "2 Worm Baits")
                        ),
                        badgeEmoji = "📜"
                    ))
                }
            }
            6 -> {
                if (uniqueCount >= 6 && !_colMilestone6Claimed.value) {
                    _colMilestone6Claimed.value = true
                    prefs.edit().putBoolean("col_milestone_6_claimed", true).apply()
                    _coins.value += 500
                    _baitShrimpCount.value += 2
                    saveBaitCountsToPrefs()
                    
                    triggerCelebration(CelebrationData(
                        id = "col_milestone_6",
                        titleKo = "도감 마일스톤 달성: 노련한 학자",
                        titleEn = "Collection Goal: Adept Angler",
                        descriptionKo = "물고기 도감에 6종 이상의 서로 다른 어류를 훌륭히 등재하셨습니다.",
                        descriptionEn = "You have recorded 6 or more distinct species in your log book.",
                        type = "COLLECTION",
                        rewards = listOf(
                            CelebrationReward("coins", 500, "500 골드", "500 Gold"),
                            CelebrationReward("bait_shrimp", 2, "크릴새우 미끼 2개", "2 Shrimp Baits")
                        ),
                        badgeEmoji = "🐠"
                    ))
                }
            }
            9 -> {
                if (uniqueCount >= 9 && !_colMilestone9Claimed.value) {
                    _colMilestone9Claimed.value = true
                    prefs.edit().putBoolean("col_milestone_9_claimed", true).apply()
                    _coins.value += 1000
                    _baitGoldenCount.value += 2
                    saveBaitCountsToPrefs()
                    
                    triggerCelebration(CelebrationData(
                        id = "col_milestone_9",
                        titleKo = "도감 마일스톤 달성: 생태 탐구자",
                        titleEn = "Collection Goal: Freshwater Ecologist",
                        descriptionKo = "물고기 도감에 9종 이상의 서로 다른 어류를 찾아내어 생존을 보존했습니다.",
                        descriptionEn = "You have recorded 9 or more distinct species in your log book.",
                        type = "COLLECTION",
                        rewards = listOf(
                            CelebrationReward("coins", 1000, "1000 골드", "1000 Gold"),
                            CelebrationReward("bait_golden", 2, "황금 고농축 떡밥 2개", "2 Golden Baits")
                        ),
                        badgeEmoji = "🔬"
                    ))
                }
            }
            12 -> {
                if (uniqueCount >= 12 && !_colMilestone12Claimed.value) {
                    _colMilestone12Claimed.value = true
                    prefs.edit().putBoolean("col_milestone_12_claimed", true).apply()
                    _coins.value += 2500
                    _baitGoldenCount.value += 5
                    saveBaitCountsToPrefs()
                    
                    triggerCelebration(CelebrationData(
                        id = "col_milestone_12",
                        titleKo = "도감 마일스톤 달성: 대완성 마스터!",
                        titleEn = "Collection Goal: Grand Dictionary Master!",
                        descriptionKo = "모든 12종의 다양한 신형 생태 물고기를 도감에 완벽히 정복하셨습니다!",
                        descriptionEn = "You have successfully completed your log book with all 12 unique species!",
                        type = "COLLECTION",
                        rewards = listOf(
                            CelebrationReward("coins", 2500, "2500 골드", "2500 Gold"),
                            CelebrationReward("bait_golden", 5, "황금 고농축 떡밥 5개", "5 Golden Baits")
                        ),
                        badgeEmoji = "👑"
                    ))
                }
            }
        }
        prefs.edit().putInt("coins", _coins.value).apply()
    }

    fun claimAchievement(achId: String) {
        when (achId) {
            "catch_10" -> {
                if (_totalFishCaught.value >= 10 && !_achCatch10Claimed.value) {
                    _achCatch10Claimed.value = true
                    prefs.edit().putBoolean("ach_catch_10_claimed", true).apply()
                    _coins.value += 100
                    
                    triggerCelebration(CelebrationData(
                        id = "ach_catch_10",
                        titleKo = "업적 달성: 초보 강태공",
                        titleEn = "Achievement: Rookie Angler",
                        descriptionKo = "물의 흐름을 파악하는 법을 배우며 누적 10마리 이상 낚시에 정성껏 성공하였습니다.",
                        descriptionEn = "You have successfully caught a lifetime total of 10 or more fish.",
                        type = "ACHIEVEMENT",
                        rewards = listOf(
                            CelebrationReward("coins", 100, "100 골드", "100 Gold")
                        ),
                        badgeEmoji = "🎣"
                    ))
                }
            }
            "mythic" -> {
                if (_hasCaughtLegendaryOrMythic.value && !_achMythicClaimed.value) {
                    _achMythicClaimed.value = true
                    prefs.edit().putBoolean("ach_mythic_claimed", true).apply()
                    _coins.value += 250
                    
                    triggerCelebration(CelebrationData(
                        id = "ach_mythic",
                        titleKo = "업적 달성: 전설을 마주하다",
                        titleEn = "Achievement: Legend Met",
                        descriptionKo = "수심 깊은 은하의 고요 아래 숨쉬던 전설 또는 신화 등급의 귀족 어종을 생포했습니다.",
                        descriptionEn = "You have fought and caught an elusive Legendary or Mythic grade specimen.",
                        type = "ACHIEVEMENT",
                        rewards = listOf(
                            CelebrationReward("coins", 250, "250 골드", "250 Gold")
                        ),
                        badgeEmoji = "🦄"
                    ))
                }
            }
            "rod_3" -> {
                if (_rodLevel.value >= 3 && !_achRod3Claimed.value) {
                    _achRod3Claimed.value = true
                    prefs.edit().putBoolean("ach_rod_3_claimed", true).apply()
                    _coins.value += 400
                    
                    triggerCelebration(CelebrationData(
                        id = "ach_rod_3",
                        titleKo = "업적 달성: 정밀한 마스터",
                        titleEn = "Achievement: Precision Hand",
                        descriptionKo = "낚싯대 개조 레벨을 3개성 수준으로 정교히 가꾸어 한 단계 높였습니다.",
                        descriptionEn = "You have customized and upgraded your fishing rod to level 3 or higher.",
                        type = "ACHIEVEMENT",
                        rewards = listOf(
                            CelebrationReward("coins", 400, "400 골드", "400 Gold")
                        ),
                        badgeEmoji = "🛠️"
                    ))
                }
            }
            "sell_20" -> {
                if (_totalFishSold.value >= 20 && !_achSell20Claimed.value) {
                    _achSell20Claimed.value = true
                    prefs.edit().putBoolean("ach_sell_20_claimed", true).apply()
                    _coins.value += 500
                    
                    triggerCelebration(CelebrationData(
                        id = "ach_sell_20",
                        titleKo = "업적 달성: 노련한 지배자",
                        titleEn = "Achievement: Shrewd Merchant",
                        descriptionKo = "누적 20마리 이상 물고기를 맑은 자연으로 방생하여 호수의 조화를 지켰습니다.",
                        descriptionEn = "You have successfully sold or released a lifetime total of 20 or more fish.",
                        type = "ACHIEVEMENT",
                        rewards = listOf(
                            CelebrationReward("coins", 500, "500 골드", "500 Gold")
                        ),
                        badgeEmoji = "⚖️"
                    ))
                }
            }
            "coins_2000" -> {
                if (_coins.value >= 2000 && !_achCoins2000Claimed.value) {
                    _achCoins2000Claimed.value = true
                    prefs.edit().putBoolean("ach_coins_2000_claimed", true).apply()
                    _coins.value += 1000
                    _baitWormCount.value += 3
                    _baitShrimpCount.value += 3
                    _baitGoldenCount.value += 3
                    saveBaitCountsToPrefs()
                    
                    triggerCelebration(CelebrationData(
                        id = "ach_coins_2000",
                        titleKo = "업적 달성: 황금빛 만선",
                        titleEn = "Achievement: Golden Harvest",
                        descriptionKo = "수중에 차곡차곡 모아낸 고유 자금이 드디어 2000골드를 시원하게 돌파했습니다.",
                        descriptionEn = "You have accumulated a personal budget of 2,000 or more gold.",
                        type = "ACHIEVEMENT",
                        rewards = listOf(
                            CelebrationReward("coins", 1000, "1000 골드", "1000 Gold"),
                            CelebrationReward("bait_worm", 3, "갯지렁이 미끼 3개", "3 Worm Baits"),
                            CelebrationReward("bait_shrimp", 3, "크릴새우 미끼 3개", "3 Shrimp Baits"),
                            CelebrationReward("bait_golden", 3, "황금 고농축 떡밥 3개", "3 Golden Baits")
                        ),
                        badgeEmoji = "💫"
                    ))
                }
            }
        }
        prefs.edit().putInt("coins", _coins.value).apply()
    }

    override fun onCleared() {
        super.onCleared()
        fishingJob?.cancel()
        autoTimeCycleJob?.cancel()
        autoNatureSoundCycleJob?.cancel()
    }
}

/**
 * ViewModelProvider Factory for clean instantiation
 */
class FishingViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FishingViewModel::class.java)) {
            val database = AppDatabase.getDatabase(application)
            val repository = FishingRepository(database.caughtFishDao())
            return FishingViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

enum class FishingSpot(
    val id: String,
    val nameKo: String,
    val nameEn: String,
    val nameJa: String,
    val descriptionKo: String,
    val descriptionEn: String,
    val descriptionJa: String,
    val emoji: String,
    val minLevel: Int
) {
    WINDY_VALLEY(
        "windy_valley",
        "바람의 계곡", "Windy Valley", "風の谷",
        "맑고 포근한 바람이 부는 청정한 계곡가입니다. 평화로운 강바닥에서 자라난 다양한 민물고기를 낚을 수 있습니다.",
        "A clear, cozy valley stream with refreshing breezes. Ideal for catching a variety of peaceful freshwater fish.",
        "穏やかな風が吹く清らかな渓谷。平和な川底で育った様々な淡水魚が釣れます。",
        "🏞️", 1
    ),
    GALAXY_LAKE(
        "galaxy_lake",
        "은하빛 호수", "Galaxy Lake", "銀河の湖",
        "호수가 온 우주의 은하수를 담은 듯 비현실적으로 반짝입니다. 신비하고 희귀한 어류들이 밤낮없이 노닙니다.",
        "The lake sparkles like the Milky Way galaxy. Mysterious and rare species hover in these deep, starry pools.",
        "全ての星空が溶け込んだような幻想的な湖。神秘的で珍しい魚たちが優雅に泳ぎ回ります。",
        "🌌", 2
    ),
    WAVE_BEACH(
        "wave_beach",
        "파도소리 비치", "Wave Sound Beach", "波音의 砂浜",
        "부드럽게 밀려오는 에메랄드빛 바다와 철썩이는 파도소리가 편안한 마음을 선사하는 넓은 해변입니다.",
        "An emerald sea where gentle, rolling waves and soft sounds bring immense peace and legendary marine life.",
        "エメラルドグリーンの砂浜に押し寄せる美しい波が、心安らぐ時間と大物の出会いをもたらします。",
        "🏖️", 4
    )
}

data class NotificationAlert(
    val title: String,
    val message: String,
    val emoji: String,
    val color: Color
)

data class CelebrationReward(
    val type: String, // "coins", "bait_worm", "bait_shrimp", "bait_golden", "xp"
    val count: Int,
    val nameKo: String,
    val nameEn: String
)

data class CelebrationData(
    val id: String,
    val titleKo: String,
    val titleEn: String,
    val descriptionKo: String,
    val descriptionEn: String,
    val type: String, // "DAILY_QUEST", "ACHIEVEMENT", "COLLECTION"
    val rewards: List<CelebrationReward>,
    val badgeEmoji: String = "🏆"
)
