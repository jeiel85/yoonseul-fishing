package com.jeiel85.healingfishing.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeiel85.healingfishing.data.CaughtFishEntity
import com.jeiel85.healingfishing.data.FishSpecies
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

// Splash particle class
data class SplashParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var baseSize: Float,
    var alpha: Float = 1f,
    val color: Color,
    val shapeType: Int = 0, // 0: Triangle, 1: Diamond, 2: Hexagon
    var rotation: Float = 0f,
    val rotationSpeed: Float = 0f
)

// Glistening reflection particle representing "Yoonseul" (윤슬)
data class YoonseulSparkle(
    val relX: Float, // 0f to 1f
    val relY: Float, // 0f to 1f
    val scaleFactor: Float,
    var currentPhase: Float
)

// Floating water bubble and sparkling star for high-fidelity fishing success celebration Card
data class CelebrationBubble(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var alpha: Float,
    val color: Color,
    var age: Float = 0f,
    val maxAge: Float = 120f,
    val scaleSpeed: Float = 1f
)

data class RainStroke(
    var x: Float,
    var y: Float,
    val length: Float,
    val speed: Float
)

data class RainRipple(
    var x: Float,
    var y: Float,
    var radius: Float,
    var maxRadius: Float,
    var alpha: Float,
    val speed: Float
)

data class TapWaterRipple(
    val x: Float,
    val y: Float,
    var radius: Float,
    var maxRadius: Float,
    var alpha: Float,
    val speed: Float,
    val isUserTap: Boolean = false
)

data class WindStroke(
    var x: Float,
    var y: Float,
    val length: Float,
    val width: Float,
    val speed: Float,
    val opacity: Float
)

data class CelebrationSparkle(
    var x: Float,
    var y: Float,
    var speedX: Float,
    var speedY: Float,
    val color: Color,
    val size: Float,
    val maxLife: Float,
    var life: Float
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HealingFishingGame(
    viewModel: FishingViewModel,
    modifier: Modifier = Modifier
) {
    val timeOfDay by viewModel.timeOfDay.collectAsState()
    val weather by viewModel.weather.collectAsState()
    val fishingState by viewModel.fishingState.collectAsState()
    val caughtFishList by viewModel.caughtFishList.collectAsState()
    val lastCaughtFish by viewModel.lastCaughtFish.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val isEnglish by viewModel.isEnglish.collectAsState()
    val language by viewModel.language.collectAsState()
    val natureSound by viewModel.natureSound.collectAsState()
    val hasCastBefore by viewModel.hasCastBefore.collectAsState()

    // Rhythm Mini-game Subscriptions
    val rhythmRingScale by viewModel.rhythmRingScale.collectAsState()
    val rhythmHitCount by viewModel.rhythmHitCount.collectAsState()
    val rhythmMissCount by viewModel.rhythmMissCount.collectAsState()
    val rhythmFeedbackText by viewModel.rhythmFeedbackText.collectAsState()
    val rhythmBeatActive by viewModel.rhythmBeatActive.collectAsState()

    val bobberXState by viewModel.bobberPositionX.collectAsState()
    val bobberYState by viewModel.bobberPositionY.collectAsState()

    // Level System UI States
    val fishingLevel by viewModel.fishingLevel.collectAsState()
    val fishingXp by viewModel.fishingXp.collectAsState()
    val coins by viewModel.coins.collectAsState()
    val rodLevel by viewModel.rodLevel.collectAsState()
    val showLevelUpDialog by viewModel.showLevelUpDialog.collectAsState()

    // Bait State collections
    val activeBait by viewModel.activeBait.collectAsState()
    val baitWormCount by viewModel.baitWormCount.collectAsState()
    val baitShrimpCount by viewModel.baitShrimpCount.collectAsState()
    val baitGoldenCount by viewModel.baitGoldenCount.collectAsState()
    var showBaitQuickSelect by remember { mutableStateOf(false) }

    var isAmbientPanelExpanded by remember { mutableStateOf(false) }
    var isUpgradeOpen by remember { mutableStateOf(false) }
    var isShopOpen by remember { mutableStateOf(false) }
    var isQuestOpen by remember { mutableStateOf(false) }
    var isSpotSelectOpen by remember { mutableStateOf(false) }
    var isGpgRankingOpen by remember { mutableStateOf(false) }
    var isTutorialOpen by remember { mutableStateOf(false) }

    LaunchedEffect(hasCastBefore) {
        if (!hasCastBefore) {
            isTutorialOpen = true
        }
    }

    // Dynamic screenshake effect configuration for bite / reel tension
    val shakeIntensity = when (fishingState) {
        FishingState.BITE -> 16f     // High intensity sudden pull tension
        FishingState.REELING -> 6f   // Moderate dynamic reel tension vibration
        FishingState.NIBBLE -> 2.5f  // Very subtle predictive wiggle vibration
        else -> 0f                   // Perfectly steady
    }

    val shakeTransition = rememberInfiniteTransition(label = "screen_shake")
    val shakePhaseX by shakeTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shakePhaseX"
    )
    val shakePhaseY by shakeTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(110, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shakePhaseY"
    )

    val shakeOffsetX = if (shakeIntensity > 0) {
        (sin(shakePhaseX) * 0.72f + cos(shakePhaseX * 2.3f) * 0.28f) * shakeIntensity
    } else 0f
    val shakeOffsetY = if (shakeIntensity > 0) {
        (sin(shakePhaseY) * 0.65f + cos(shakePhaseY * 1.7f) * 0.35f) * shakeIntensity
    } else 0f

    // Screen details for local dimension binding
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    // Particle state for active physical water splash
    val particles = remember { mutableStateListOf<SplashParticle>() }
    
    // Celebration particles for successful catches
    val caughtBubbles = remember { mutableStateListOf<CelebrationBubble>() }
    // Star state for night sky twinkles
    val stars = remember {
        mutableStateListOf<Offset>().apply {
            repeat(45) {
                add(Offset(Random.nextFloat(), Random.nextFloat() * 0.45f))
            }
        }
    }

    // Yoonseul water shimmering coordinates
    val yoonseulSparkles = remember {
        mutableStateListOf<YoonseulSparkle>().apply {
            repeat(60) {
                add(
                    YoonseulSparkle(
                        relX = 0.05f + Random.nextFloat() * 0.90f,
                        relY = 0.58f + Random.nextFloat() * 0.38f,
                        scaleFactor = 0.4f + Random.nextFloat() * 1.2f,
                        currentPhase = Random.nextFloat() * 2f * Math.PI.toFloat()
                    )
                )
            }
        }
    }

    // Rain states for dynamic weather
    val rainStrokes = remember {
        mutableStateListOf<RainStroke>().apply {
            repeat(35) {
                add(
                    RainStroke(
                        x = Random.nextFloat(),
                        y = Random.nextFloat(),
                        length = 15f + Random.nextFloat() * 25f,
                        speed = 0.012f + Random.nextFloat() * 0.010f
                    )
                )
            }
        }
    }

    val rainRipples = remember {
        mutableStateListOf<RainRipple>().apply {
            repeat(20) {
                add(
                    RainRipple(
                        x = Random.nextFloat(),
                        y = 0.53f + Random.nextFloat() * 0.40f,
                        radius = Random.nextFloat() * 12f,
                        maxRadius = 12f + Random.nextFloat() * 18f,
                        alpha = Random.nextFloat(),
                        speed = 0.25f + Random.nextFloat() * 0.4f
                    )
                )
            }
        }
    }

    val tapRipples = remember {
        mutableStateListOf<TapWaterRipple>()
    }

    val windStrokes = remember {
        mutableStateListOf<WindStroke>().apply {
            repeat(8) {
                add(
                    WindStroke(
                        x = Random.nextFloat(),
                        y = 0.15f + Random.nextFloat() * 0.45f,
                        length = 60f + Random.nextFloat() * 100f,
                        width = 1.5f + Random.nextFloat() * 1.5f,
                        speed = 0.003f + Random.nextFloat() * 0.004f,
                        opacity = 0.12f + Random.nextFloat() * 0.18f
                    )
                )
            }
        }
    }

    // Trigger beautiful splash ripples on water surface when bobber lands inside the waiting state (워터 리플 효과)
    LaunchedEffect(fishingState) {
        if (fishingState == FishingState.WAITING) {
            tapRipples.add(
                TapWaterRipple(
                    x = bobberXState,
                    y = bobberYState,
                    radius = 2f,
                    maxRadius = 150f + Random.nextFloat() * 30f,
                    alpha = 0.95f,
                    speed = 2.8f + Random.nextFloat() * 0.8f,
                    isUserTap = true
                )
            )
            tapRipples.add(
                TapWaterRipple(
                    x = bobberXState,
                    y = bobberYState,
                    radius = 1f,
                    maxRadius = 95f + Random.nextFloat() * 20f,
                    alpha = 0.70f,
                    speed = 1.9f + Random.nextFloat() * 0.6f,
                    isUserTap = true
                )
            )
        }
    }

    // Active local frames ticker for fluid animations (60fps)
    var animationTicks by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        while (isActive) {
            withFrameMillis { frameTime ->
                animationTicks = (frameTime - startTime)
                
                // Update splash particles
                val iterator = particles.iterator()
                while (iterator.hasNext()) {
                    val p = iterator.next()
                    p.x += p.vx
                    p.y += p.vy
                    p.vy += 0.45f // Gravity pulling water down
                    p.alpha -= 0.024f
                    p.baseSize *= 0.97f
                    p.rotation += p.rotationSpeed
                    if (p.alpha <= 0f || p.baseSize <= 0.5f) {
                        particles.remove(p)
                    }
                }

                // Update caughtBubbles particles for catching fish dialog
                val bubbleIterator = caughtBubbles.iterator()
                while (bubbleIterator.hasNext()) {
                    val b = bubbleIterator.next()
                    b.x += b.vx
                    b.y += b.vy
                    // Slowly drift left-right like water currents
                    b.vx += (sin(animationTicks * 0.015f + b.y * 0.01f).toFloat()) * 0.03f
                    b.age += b.scaleSpeed
                    b.alpha = (1f - b.age / b.maxAge).coerceIn(0f, 1f)
                    if (b.age >= b.maxAge || b.alpha <= 0f) {
                        caughtBubbles.remove(b)
                    }
                }

                // Continuously spawn cozy floating bubbles and sparkles when caught card is shown
                if (fishingState == FishingState.CAUGHT) {
                    if (caughtBubbles.size < 70 && Random.nextFloat() < 0.28f) {
                        val widthVal = if (canvasSize.width > 0) canvasSize.width else 1080f
                        val heightVal = if (canvasSize.height > 0) canvasSize.height else 2160f
                        
                        caughtBubbles.add(
                            CelebrationBubble(
                                x = widthVal * 0.5f + (-220f + Random.nextFloat() * 440f),
                                y = heightVal * 0.85f + (-40f + Random.nextFloat() * 80f),
                                vx = (-1.0f + Random.nextFloat() * 2.0f),
                                vy = (-2.0f - Random.nextFloat() * 2.8f),
                                size = 6f + Random.nextFloat() * 15f,
                                alpha = 0.9f,
                                color = when (Random.nextInt(4)) {
                                    0 -> Color(0xFFA5E6D8) // Pastel Mint
                                    1 -> Color(0xFFFFB2A0) // Coral
                                    2 -> Color(0xFFD4B2E8) // Lavender
                                    else -> Color.White.copy(alpha = 0.85f)
                                },
                                maxAge = 110f + Random.nextFloat() * 90f,
                                scaleSpeed = 0.8f + Random.nextFloat() * 0.8f
                            )
                        )
                    }
                } else {
                    if (caughtBubbles.isNotEmpty() && fishingState != FishingState.SPLASHING) {
                        caughtBubbles.clear()
                    }
                }

                // Gently update Yoonseul sparkles phase
                for (idx in yoonseulSparkles.indices) {
                    val sp = yoonseulSparkles[idx]
                    yoonseulSparkles[idx] = sp.copy(
                        currentPhase = sp.currentPhase + 0.05f
                    )
                }

                // Update rain strokes & ripples smoothly
                if (weather == Weather.RAIN) {
                    for (idx in rainStrokes.indices) {
                        val rs = rainStrokes[idx]
                        rs.y += rs.speed
                        if (rs.y > 1.0f) {
                            rs.y = 0f
                            rs.x = Random.nextFloat()
                        }
                    }
                    for (idx in rainRipples.indices) {
                        val rr = rainRipples[idx]
                        rr.radius += rr.speed
                        rr.alpha -= 0.015f
                        if (rr.alpha <= 0f || rr.radius >= rr.maxRadius) {
                            rr.x = Random.nextFloat()
                            rr.y = 0.53f + Random.nextFloat() * 0.40f
                            rr.radius = 0f
                            rr.alpha = 0.6f + Random.nextFloat() * 0.4f
                            rr.maxRadius = 12f + Random.nextFloat() * 18f
                        }
                    }
                } else {
                    // Let existing rain cascade off screen beautifully
                    for (idx in rainStrokes.indices) {
                        val rs = rainStrokes[idx]
                        if (rs.y in 0f..1f) {
                            rs.y += rs.speed
                        }
                    }
                    for (idx in rainRipples.indices) {
                        val rr = rainRipples[idx]
                        if (rr.alpha > 0f) {
                            rr.radius += rr.speed
                            rr.alpha -= 0.02f
                        }
                    }
                }

                // Spontaneous gentle ambient water ripples (잔잔한 물결 효과)
                if (animationTicks % 140L == 0L) {
                    tapRipples.add(
                        TapWaterRipple(
                            x = 0.05f + Random.nextFloat() * 0.9f,
                            y = 0.53f + Random.nextFloat() * 0.42f,
                            radius = 1f,
                            maxRadius = 35f + Random.nextFloat() * 45f,
                            alpha = 0.14f + Random.nextFloat() * 0.16f,
                            speed = 0.35f + Random.nextFloat() * 0.35f,
                            isUserTap = false
                        )
                    )
                }

                // Update interactive & ambient water ripples
                val tri = tapRipples.iterator()
                while (tri.hasNext()) {
                    val rp = tri.next()
                    rp.radius += rp.speed
                    val fadeAmount = if (rp.isUserTap) 0.012f else 0.004f
                    rp.alpha -= fadeAmount
                    if (rp.alpha <= 0f || rp.radius >= rp.maxRadius) {
                        tri.remove()
                    }
                }

                // Update flowing wind strokes (바람 효과)
                for (idx in windStrokes.indices) {
                    val ws = windStrokes[idx]
                    ws.x += ws.speed
                    if (ws.x > 1.2f) {
                        ws.x = -0.3f
                        ws.y = 0.15f + Random.nextFloat() * 0.45f
                    }
                }
            }
        }
    }

    val colorAccentTarget = when (timeOfDay) {
        TimeOfDay.DAY -> Color(0xFFE2F3F0)
        TimeOfDay.SUNSET -> Color(0xFFFFD1B3)
        TimeOfDay.NIGHT -> Color(0xFFD3E2F2)
    }
    val colorAccent by animateColorAsState(targetValue = colorAccentTarget, animationSpec = tween(3000), label = "colorAccent")

    // Animated progress tracking of the active fish jump (0f to 1f over 2200ms)
    var splashProgress by remember { mutableStateOf(0f) }
    var reentrySplashTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(fishingState) {
        if (fishingState == FishingState.SPLASHING) {
            reentrySplashTriggered = false
            val anim = Animatable(0f)
            anim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 2200, easing = LinearEasing)
            ) {
                splashProgress = value
            }
        } else {
            splashProgress = 0f
            reentrySplashTriggered = false
        }
    }

    // Trailing low-poly particles generated dynamically as the fish travels in its arc
    LaunchedEffect(splashProgress) {
        if (fishingState == FishingState.SPLASHING && splashProgress > 0.05f && splashProgress < 0.92f) {
            val w = canvasSize.width
            val h = canvasSize.height
            if (w > 0f && h > 0f) {
                val bXNum = bobberXState * w
                val bYNum = bobberYState * h
                
                // Keep the fish jump width proportional
                val fishX = bXNum + (splashProgress - 0.5f) * 200f
                val arcHeight = 220f * sin(splashProgress * Math.PI.toFloat())
                val fishY = bYNum - arcHeight

                repeat(5) {
                    val angle = Random.nextDouble(-Math.PI, Math.PI)
                    val speed = Random.nextDouble(1.0, 5.0)
                    particles.add(
                        SplashParticle(
                            x = fishX,
                            y = fishY,
                            vx = (cos(angle) * speed).toFloat() - (splashProgress - 0.5f) * 3f,
                            vy = (sin(angle) * speed).toFloat() + 0.5f,
                            baseSize = 4f + Random.nextFloat() * 7f,
                            alpha = 0.85f + Random.nextFloat() * 0.15f,
                            color = if (Random.nextBoolean()) Color.White else colorAccent,
                            shapeType = Random.nextInt(3),
                            rotation = Random.nextFloat() * 360f,
                            rotationSpeed = -15f + Random.nextFloat() * 30f
                        )
                    )
                }
            }
        }
    }

    // Secondary splash trigger: reentry back into water
    LaunchedEffect(splashProgress) {
        if (fishingState == FishingState.SPLASHING && splashProgress >= 0.91f && !reentrySplashTriggered) {
            reentrySplashTriggered = true
            val w = canvasSize.width
            val h = canvasSize.height
            if (w > 0f && h > 0f) {
                val bXNum = bobberXState * w + 0.41f * 200f
                val bYNum = bobberYState * h
                
                // Massive re-entry geometric splash
                repeat(45) {
                    val angle = Random.nextDouble(-Math.PI * 0.85, -Math.PI * 0.15)
                    val speed = Random.nextDouble(4.0, 14.0)
                    particles.add(
                        SplashParticle(
                            x = bXNum,
                            y = bYNum - 12f,
                            vx = (cos(angle) * speed).toFloat(),
                            vy = (sin(angle) * speed).toFloat(),
                            baseSize = 7f + Random.nextFloat() * 11f,
                            alpha = 0.95f,
                            color = if (Random.nextBoolean()) Color.White else colorAccent,
                            shapeType = Random.nextInt(3),
                            rotation = Random.nextFloat() * 360f,
                            rotationSpeed = -25f + Random.nextFloat() * 50f
                        )
                    )
                }
            }
        }
    }

    // Generate physical splash particles when state changes to SPLASHING or CASTING
    val density = LocalDensity.current

    LaunchedEffect(fishingState) {
        if (fishingState == FishingState.SPLASHING) {
            // Big fish hook-out jump splash! Make it massive splash
            val bXNum = bobberXState * canvasSize.width
            val bYNum = bobberYState * canvasSize.height
            repeat(55) {
                val angle = Random.nextDouble(-Math.PI * 0.82, -Math.PI * 0.18)
                val speed = Random.nextDouble(5.0, 16.0)
                particles.add(
                    SplashParticle(
                        x = bXNum,
                        y = bYNum - 20f,
                        vx = (cos(angle) * speed).toFloat(),
                        vy = (sin(angle) * speed).toFloat(),
                        baseSize = 8f + Random.nextFloat() * 13f,
                        alpha = 0.9f + Random.nextFloat() * 0.1f,
                        color = if (Random.nextBoolean()) Color.White else colorAccent,
                        shapeType = Random.nextInt(3),
                        rotation = Random.nextFloat() * 360f,
                        rotationSpeed = -20f + Random.nextFloat() * 40f
                    )
                )
            }
        } else if (fishingState == FishingState.CASTING) {
            // Little spooling splash
            val bXNum = bobberXState * canvasSize.width
            val bYNum = bobberYState * canvasSize.height
            repeat(20) {
                val angle = Random.nextDouble(-Math.PI * 0.72, -Math.PI * 0.28)
                val speed = Random.nextDouble(3.0, 8.0)
                particles.add(
                    SplashParticle(
                        x = bXNum,
                        y = bYNum - 5f,
                        vx = (cos(angle) * speed).toFloat(),
                        vy = (sin(angle) * speed).toFloat(),
                        baseSize = 4f + Random.nextFloat() * 7f,
                        alpha = 0.8f + Random.nextFloat() * 0.2f,
                        color = Color.White,
                        shapeType = Random.nextInt(2),
                        rotation = Random.nextFloat() * 360f,
                        rotationSpeed = -10f + Random.nextFloat() * 20f
                    )
                )
            }
        }
    }

    // Drawer state for the sliding Fish Encyclopedia (도감)
    var isEncyclopediaOpen by remember { mutableStateOf(false) }
    var isAquariumOpen by remember { mutableStateOf(false) }
    var selectedFishDetail by remember { mutableStateOf<FishSpecies?>(null) }

    // --- 2.5D DYNAMIC ROD PHYSICS ANIMATIONS ---
    val rodBendTarget = when (fishingState) {
        FishingState.IDLE -> 0f
        FishingState.CASTING -> -35f
        FishingState.WAITING -> 2f + sin(animationTicks * 0.005f).toFloat() * 1.5f
        FishingState.NIBBLE -> 15f + sin(animationTicks * 0.03f).toFloat() * 6f
        FishingState.BITE -> 48f + sin(animationTicks * 0.08f).toFloat() * 3f
        FishingState.REELING -> 40f + (if (rhythmBeatActive) sin(animationTicks * 0.08f).toFloat() * 4f else sin(animationTicks * 0.04f).toFloat() * 2f)
        FishingState.SPLASHING -> 5f
        FishingState.LOST -> -12f
        else -> 0f
    }

    val rodAngleTarget = when (fishingState) {
        FishingState.CASTING -> -28f
        FishingState.BITE, FishingState.REELING -> 15f
        FishingState.LOST -> -8f
        else -> 0f
    }

    val rodScaleTarget = when (fishingState) {
        FishingState.CASTING -> 0.78f // foreshortening in Z-axis (2.5D)
        FishingState.BITE, FishingState.REELING -> 1.15f // pulling forward on tension
        else -> 1.0f
    }

    val animatedRodBend by animateFloatAsState(
        targetValue = rodBendTarget,
        animationSpec = when (fishingState) {
            FishingState.CASTING -> spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium)
            FishingState.BITE, FishingState.LOST -> spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh)
            else -> spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
        },
        label = "animatedRodBend"
    )

    val animatedRodAngle by animateFloatAsState(
        targetValue = rodAngleTarget,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "animatedRodAngle"
    )

    val animatedRodScale by animateFloatAsState(
        targetValue = rodScaleTarget,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "animatedRodScale"
    )

    // Real-time smooth transition states for passage of time (gradient shifts)
    val skyTopTarget = when (timeOfDay) {
        TimeOfDay.DAY -> Color(0xFFBFE3E8)
        TimeOfDay.SUNSET -> Color(0xFF514068)
        TimeOfDay.NIGHT -> Color(0xFF0D1224)
    }

    val skyMiddleTarget = when (timeOfDay) {
        TimeOfDay.DAY -> Color(0xFFD1E9EC)
        TimeOfDay.SUNSET -> Color(0xFFEB8A74)
        TimeOfDay.NIGHT -> Color(0xFF141A33)
    }

    val skyBottomTarget = when (timeOfDay) {
        TimeOfDay.DAY -> Color(0xFFE3F0F4)
        TimeOfDay.SUNSET -> Color(0xFFFCCC9B)
        TimeOfDay.NIGHT -> Color(0xFF1B2342)
    }

    val waterTopTarget = when (timeOfDay) {
        TimeOfDay.DAY -> Color(0xFF90C2C8)
        TimeOfDay.SUNSET -> Color(0xFFD4746A)
        TimeOfDay.NIGHT -> Color(0xFF13192F)
    }

    val waterMiddleTarget = when (timeOfDay) {
        TimeOfDay.DAY -> Color(0xFF72A9B0)
        TimeOfDay.SUNSET -> Color(0xFFBA5E5C)
        TimeOfDay.NIGHT -> Color(0xFF1E2849)
    }

    val waterBottomTarget = when (timeOfDay) {
        TimeOfDay.DAY -> Color(0xFF558E95)
        TimeOfDay.SUNSET -> Color(0xFF863B48)
        TimeOfDay.NIGHT -> Color(0xFF26325C)
    }

    val mountainPrimaryTarget = when (timeOfDay) {
        TimeOfDay.DAY -> Color(0xFF85AFAF)
        TimeOfDay.SUNSET -> Color(0xFF704A6B)
        TimeOfDay.NIGHT -> Color(0xFF19213D)
    }

    val mountainSecondaryTarget = when (timeOfDay) {
        TimeOfDay.DAY -> Color(0xFF9EBFBF)
        TimeOfDay.SUNSET -> Color(0xFF94618E)
        TimeOfDay.NIGHT -> Color(0xFF222C52)
    }

    // Animation specs
    val transitionSpec = tween<Color>(durationMillis = 3000)

    val skyTop by animateColorAsState(targetValue = skyTopTarget, animationSpec = transitionSpec, label = "skyTop")
    val skyMiddle by animateColorAsState(targetValue = skyMiddleTarget, animationSpec = transitionSpec, label = "skyMiddle")
    val skyBottom by animateColorAsState(targetValue = skyBottomTarget, animationSpec = transitionSpec, label = "skyBottom")

    val waterTop by animateColorAsState(targetValue = waterTopTarget, animationSpec = transitionSpec, label = "waterTop")
    val waterMiddle by animateColorAsState(targetValue = waterMiddleTarget, animationSpec = transitionSpec, label = "waterMiddle")
    val waterBottom by animateColorAsState(targetValue = waterBottomTarget, animationSpec = transitionSpec, label = "waterBottom")

    val mountainPrimary by animateColorAsState(targetValue = mountainPrimaryTarget, animationSpec = transitionSpec, label = "mountainPrimary")
    val mountainSecondary by animateColorAsState(targetValue = mountainSecondaryTarget, animationSpec = transitionSpec, label = "mountainSecondary")

    // Celestial smooth transitions (Alphas)
    val daySunAlpha by animateFloatAsState(targetValue = if (timeOfDay == TimeOfDay.DAY) 1f else 0f, animationSpec = tween(3000), label = "daySunAlpha")
    val sunsetSunAlpha by animateFloatAsState(targetValue = if (timeOfDay == TimeOfDay.SUNSET) 1f else 0f, animationSpec = tween(3000), label = "sunsetSunAlpha")
    val moonAlpha by animateFloatAsState(targetValue = if (timeOfDay == TimeOfDay.NIGHT) 1f else 0f, animationSpec = tween(3000), label = "moonAlpha")
    val cloudsAlpha by animateFloatAsState(targetValue = if (timeOfDay != TimeOfDay.NIGHT) 1f else 0f, animationSpec = tween(3000), label = "cloudsAlpha")

    // Theme values derived smoothly
    val skyGradient = Brush.verticalGradient(listOf(skyTop, skyMiddle, skyBottom))
    val waterColors = listOf(waterTop, waterMiddle, waterBottom)

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("healing_fishing_container")
    ) {
        // --- 1. THE 2.5D LOW-POLY RENDER CANVAS ---
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = shakeOffsetX
                    translationY = shakeOffsetY
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // Handle tap in water region (strictly bottom 45% of the screen)
                        val relX = offset.x / canvasSize.width
                        val relY = offset.y / canvasSize.height
                        if (relY in 0.52f..0.85f) {
                            // Add beautiful cascading water ripples (워터 리플 애니메이션)
                            tapRipples.add(
                                TapWaterRipple(
                                    x = relX,
                                    y = relY,
                                    radius = 1f,
                                    maxRadius = 140f + Random.nextFloat() * 40f,
                                    alpha = 0.95f,
                                    speed = 2.6f + Random.nextFloat() * 0.8f,
                                    isUserTap = true
                                )
                            )
                            tapRipples.add(
                                TapWaterRipple(
                                    x = relX,
                                    y = relY,
                                    radius = 1f,
                                    maxRadius = 90f + Random.nextFloat() * 30f,
                                    alpha = 0.70f,
                                    speed = 1.8f + Random.nextFloat() * 0.6f,
                                    isUserTap = true
                                )
                            )
                            viewModel.handleScreenTap(relX, relY)
                        } else {
                            viewModel.handleScreenTap() // Generic tap elsewhere
                        }
                    }
                }
        ) {
            canvasSize = size
            val w = size.width
            val h = size.height

            // 1. Draw Sky Backdrop (Smooth Horizon)
            drawRect(brush = skyGradient, size = size)

            // Draw Clouds (Day / Sunset) or Twinkling Stars (Night) smoothly transitioning
            if (moonAlpha > 0.01f) {
                drawNightSky(stars, animationTicks, moonAlpha)
            }
            if (cloudsAlpha > 0.01f) {
                drawPastelClouds(animationTicks, cloudsAlpha)
            }

            // Sun/Moon casting light based on time with smooth opacity fading
            drawCelestialSource(daySunAlpha, sunsetSunAlpha, moonAlpha, h, skyTop)

            // 2. Draw Overlapping Low-Poly Mountain Ranges
            drawMountainRange(mountainPrimary, mountainSecondary, w, h)

            // 3. Draw Water Base with Poly Facets
            drawWaterLowPoly(
                waterColors = waterColors,
                yoonseulSparkles = yoonseulSparkles,
                ticks = animationTicks,
                fishingState = fishingState,
                bobberX = bobberXState,
                bobberY = bobberYState,
                weather = weather,
                rainRipples = rainRipples,
                tapRipples = tapRipples
            )

            // --- DRAW HORIZON MIST/FOG DURING MIST WEATHER ---
            if (weather == Weather.MIST) {
                val mistY = h * 0.52f
                // Render 3 overlapping drifting lines representing low-poly rolling lake fog
                for (layer in 0..2) {
                    val path = Path()
                    val layerY = mistY + layer * 16f
                    path.moveTo(0f, layerY - 15f)
                    val steps = 6
                    val stepW = w / steps
                    for (step in 0..steps) {
                        val px = step * stepW
                        val py = layerY + sin((animationTicks * 0.0006f) + step * 1.5f + layer * 2f) * 12f
                        path.lineTo(px, py)
                    }
                    path.lineTo(w, layerY + 50f)
                    path.lineTo(0f, layerY + 50f)
                    path.close()
                    // Mist color is a very soft, soothing semi-transparent white
                    drawPath(
                        path = path,
                        color = Color.White.copy(alpha = 0.08f - layer * 0.02f)
                    )
                }
            }

            // --- DRAW AMBIENT BLOWING WIND EFFECTS (바람 효과) ---
            for (ws in windStrokes) {
                val startX = ws.x * w
                val startY = ws.y * h
                val endX = startX + ws.length
                val endY = startY + (ws.length * 0.05f) // slightly slanted downwards representing breeze
                drawLine(
                    color = Color.White.copy(alpha = ws.opacity),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = ws.width,
                    cap = StrokeCap.Round
                )
            }

            // 4. Draw Floating Fisherman inside Wooden Boat (Procedural bobbing/rolling)
            drawWoodenBoatAndFisherman(
                time = timeOfDay,
                w = w,
                h = h,
                ticks = animationTicks,
                fishingState = fishingState,
                bobberX = bobberXState,
                bobberY = bobberYState,
                animatedBend = animatedRodBend,
                animatedAngle = animatedRodAngle,
                animatedScale = animatedRodScale
            )

            // 5. Draw Jumping Fish in midair during SPLASHING state
            if (fishingState == FishingState.SPLASHING && lastCaughtFish != null) {
                val species = FishSpecies.find(lastCaughtFish!!.speciesId)
                if (species != null) {
                    val bXNum = bobberXState * w
                    val bYNum = bobberYState * h
                    
                    val fishX = bXNum + (splashProgress - 0.5f) * 200f
                    val arcHeight = 220f * sin(splashProgress * Math.PI.toFloat())
                    val fishY = bYNum - arcHeight

                    // Angle along the trajectory parabola, wiggling as it flies
                    val trajectoryAngle = -50f + splashProgress * 100f
                    val wiggle = sin(animationTicks * 0.04f).toFloat() * 15f
                    val fishRot = trajectoryAngle + wiggle

                    withTransform({
                        translate(fishX, fishY)
                        scale(1.2f, 1.2f, pivot = Offset.Zero)
                        rotate(degrees = fishRot, pivot = Offset.Zero)
                        // Mirror vertically on downhill arc for organic look
                        if (splashProgress > 0.5f) {
                            scale(scaleX = 1f, scaleY = -1f, pivot = Offset.Zero)
                        }
                    }) {
                        drawLowPolyJumpingFish(species, animationTicks)
                    }
                }
            }

            // 6. Draw Water Splash Particles in midair (low-poly geometric shards)
            for (p in particles) {
                val path = Path()
                when (p.shapeType) {
                    0 -> { // Triangle
                        val r = p.baseSize
                        val angleRad = Math.toRadians(p.rotation.toDouble())
                        val p1 = Offset(
                            p.x + (r * cos(angleRad)).toFloat(),
                            p.y + (r * sin(angleRad)).toFloat()
                        )
                        val p2 = Offset(
                            p.x + (r * cos(angleRad + 2 * Math.PI / 3)).toFloat(),
                            p.y + (r * sin(angleRad + 2 * Math.PI / 3)).toFloat()
                        )
                        val p3 = Offset(
                            p.x + (r * cos(angleRad + 4 * Math.PI / 3)).toFloat(),
                            p.y + (r * sin(angleRad + 4 * Math.PI / 3)).toFloat()
                        )
                        path.moveTo(p1.x, p1.y)
                        path.lineTo(p2.x, p2.y)
                        path.lineTo(p3.x, p3.y)
                        path.close()
                    }
                    1 -> { // Diamond / Gem
                        val r = p.baseSize
                        val angleRad = Math.toRadians(p.rotation.toDouble())
                        val p1 = Offset(p.x + (r * 1.3f * cos(angleRad)).toFloat(), p.y + (r * 1.3f * sin(angleRad)).toFloat())
                        val p2 = Offset(p.x + (r * 0.7f * cos(angleRad + Math.PI/2)).toFloat(), p.y + (r * 0.7f * sin(angleRad + Math.PI/2)).toFloat())
                        val p3 = Offset(p.x + (r * 1.3f * cos(angleRad + Math.PI)).toFloat(), p.y + (r * 1.3f * sin(angleRad + Math.PI)).toFloat())
                        val p4 = Offset(p.x + (r * 0.7f * cos(angleRad + 3*Math.PI/2)).toFloat(), p.y + (r * 0.7f * sin(angleRad + 3*Math.PI/2)).toFloat())
                        path.moveTo(p1.x, p1.y)
                        path.lineTo(p2.x, p2.y)
                        path.lineTo(p3.x, p3.y)
                        path.lineTo(p4.x, p4.y)
                        path.close()
                    }
                    else -> { // Hexagon
                        val r = p.baseSize
                        val angleRad = Math.toRadians(p.rotation.toDouble())
                        for (i in 0 until 6) {
                            val cornerX = p.x + (r * cos(angleRad + i * Math.PI / 3)).toFloat()
                            val cornerY = p.y + (r * sin(angleRad + i * Math.PI / 3)).toFloat()
                            if (i == 0) path.moveTo(cornerX, cornerY) else path.lineTo(cornerX, cornerY)
                        }
                        path.close()
                    }
                }
                drawPath(
                    path = path,
                    color = p.color.copy(alpha = p.alpha)
                )
            }

            // --- DRAW FALLING GENTLE RAIN STREAKS ---
            for (rs in rainStrokes) {
                if (rs.y in 0f..1.1f) {
                    val startX = rs.x * w
                    val startY = rs.y * h
                    val endX = startX - 4f
                    val endY = startY + rs.length
                    drawLine(
                        color = Color.White.copy(alpha = 0.22f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 1.5f
                    )
                }
            }
        }

        // --- 2. THE MINIMALIST FLOATING TOOLBARS ---
        // Left top: Mute, Reset Database, Help info & Level Progress HUD
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopStart),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.toggleMute() },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.35f), CircleShape)
                        .testTag("mute_button")
                ) {
                    Text(
                        text = if (isMuted) "🔇" else "🔈",
                        fontSize = 18.sp
                    )
                }

                IconButton(
                    onClick = { isEncyclopediaOpen = true },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.35f), CircleShape)
                        .testTag("open_encyclopedia_button")
                ) {
                    Text(text = "📘", fontSize = 18.sp)
                }

                IconButton(
                    onClick = { isAquariumOpen = true },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.35f), CircleShape)
                        .testTag("open_aquarium_button")
                ) {
                    Text(text = "🐠", fontSize = 18.sp)
                }

                IconButton(
                    onClick = { isTutorialOpen = true },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.35f), CircleShape)
                        .testTag("open_tutorial_button")
                ) {
                    Text(text = "❓", fontSize = 18.sp)
                }

                IconButton(
                    onClick = { viewModel.toggleLanguage() },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.35f), CircleShape)
                        .testTag("language_toggle_button")
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.KO -> "🇰🇷"
                            AppLanguage.EN -> "🇺🇸"
                            AppLanguage.JA -> "🇯🇵"
                        },
                        fontSize = 18.sp
                    )
                }
            }

            // Beautiful glassmorphic Level Badge & Progress Bar Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(175.dp)
                    .testTag("level_hud_card")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEnglish) "Lv.$fishingLevel" else "레벨 $fishingLevel",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        val xpNeeded = viewModel.getXpNeeded(fishingLevel)
                        Text(
                            text = "$fishingXp/$xpNeeded XP",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Light,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    
                    val progress = (fishingXp.toFloat() / viewModel.getXpNeeded(fishingLevel)).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(2.5.dp))
                            .background(Color.White.copy(alpha = 0.25f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .clip(RoundedCornerShape(2.5.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFFA5E6D8), // Pastel Mint
                                            Color(0xFFFFB2A0)  // Pastel Coral/Sunset Rose
                                        )
                                    )
                                )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🪙 $coins G",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            text = if (isEnglish) "🎣 Rod Lv.$rodLevel" else "🎣 낚싯대 Lv.$rodLevel",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.95f)
                        )
                    }
                }
            }
        }

        // Right top: Ambient Cycle Switcher (Collapsed by default, expands to show Time, Weather, Sound)
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopEnd),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!isAmbientPanelExpanded) {
                IconButton(
                    onClick = { isAmbientPanelExpanded = true },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.35f), CircleShape)
                        .testTag("ambient_panel_toggle")
                ) {
                    Text(
                        text = "🌿",
                        fontSize = 18.sp
                    )
                }
            } else {
                Card(
                    modifier = Modifier.widthIn(max = 240.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (language) {
                                    AppLanguage.KO -> "주변 환경"
                                    AppLanguage.JA -> "環境設定"
                                    else -> "Environment"
                                },
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { isAmbientPanelExpanded = false },
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                    .testTag("ambient_panel_close")
                            ) {
                                Text("✖️", fontSize = 10.sp, color = Color.White)
                            }
                        }

                        Divider(color = Color.White.copy(alpha = 0.15f), thickness = 1.dp)

                        // 1. Time of Day Control Block
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { viewModel.progressTimeOfDay() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.White.copy(alpha = 0.25f), CircleShape)
                                    .testTag("time_cycle_button")
                            ) {
                                Text(
                                    text = when (timeOfDay) {
                                        TimeOfDay.DAY -> "☀️"
                                        TimeOfDay.SUNSET -> "🌅"
                                        TimeOfDay.NIGHT -> "🌙"
                                    },
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = when (timeOfDay) {
                                    TimeOfDay.DAY -> when (language) {
                                        AppLanguage.KO -> "평화로운 한낮"
                                        AppLanguage.JA -> "穏やかな昼下がり"
                                        else -> "Peaceful Noon"
                                    }
                                    TimeOfDay.SUNSET -> when (language) {
                                        AppLanguage.KO -> "노을빛 강가"
                                        AppLanguage.JA -> "夕映えの川辺"
                                        else -> "Golden Sunset"
                                    }
                                    TimeOfDay.NIGHT -> when (language) {
                                        AppLanguage.KO -> "개인 별빛 밤"
                                        AppLanguage.JA -> "満天の星空"
                                        else -> "Starry Night"
                                    }
                                },
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // 2. Weather Control Block
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { viewModel.progressWeather() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.White.copy(alpha = 0.25f), CircleShape)
                                    .testTag("weather_cycle_button")
                            ) {
                                Text(
                                    text = when (weather) {
                                        Weather.CLEAR -> "☀️"
                                        Weather.MIST -> "🌫️"
                                        Weather.RAIN -> "🌧️"
                                    },
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = when (weather) {
                                    Weather.CLEAR -> when (language) {
                                        AppLanguage.KO -> "맑은 하늘"
                                        AppLanguage.JA -> "澄んだ晴天"
                                        else -> "Clear Sky"
                                    }
                                    Weather.MIST -> when (language) {
                                        AppLanguage.KO -> "고요한 안개"
                                        AppLanguage.JA -> "穏やかな霧"
                                        else -> "Soft Mist"
                                    }
                                    Weather.RAIN -> when (language) {
                                        AppLanguage.KO -> "단비 내리는 날"
                                        AppLanguage.JA -> "優しい雨"
                                        else -> "Gentle Rain"
                                    }
                                },
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // 3. Nature Sound Control Block
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { viewModel.progressNatureSound() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.White.copy(alpha = 0.25f), CircleShape)
                                    .testTag("nature_sound_cycle_button")
                            ) {
                                Text(
                                    text = when (natureSound) {
                                        NatureSound.WATER_LAP -> "🌊"
                                        NatureSound.WIND -> "🍃"
                                        NatureSound.CRICKETS -> "🦗"
                                    },
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = when (natureSound) {
                                    NatureSound.WATER_LAP -> when (language) {
                                        AppLanguage.KO -> "물결 소리"
                                        AppLanguage.JA -> "さざ波の音"
                                        else -> "Water Lapping"
                                    }
                                    NatureSound.WIND -> when (language) {
                                        AppLanguage.KO -> "산들바람"
                                        AppLanguage.JA -> "柔らかなそよ風"
                                        else -> "Soft Breeze"
                                    }
                                    NatureSound.CRICKETS -> when (language) {
                                        AppLanguage.KO -> "귀뚜라미 밤"
                                        AppLanguage.JA -> "コオロギの声"
                                        else -> "Night Crickets"
                                    }
                                },
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // --- 2.5. SUBTLE ONBOARDING OVERLAY ---
        AnimatedVisibility(
            visible = fishingState == FishingState.IDLE && !hasCastBefore,
            enter = fadeIn(animationSpec = tween(1200)) + expandVertically(),
            exit = fadeOut(animationSpec = tween(800)) + shrinkVertically(),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.38f)
                .align(Alignment.BottomCenter)
                .padding(bottom = 195.dp).testTag("onboarding_overlay")
        ) {
            val onboardingTransition = rememberInfiniteTransition(label = "onboard_glow")
            val pulseScale by onboardingTransition.animateFloat(
                initialValue = 0.9f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseScale"
            )
            val pulseAlpha by onboardingTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 0.85f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseAlpha"
            )

            val textShadow = if (timeOfDay == TimeOfDay.NIGHT) {
                Shadow(color = Color.Black, offset = Offset(1f, 1f), blurRadius = 4f)
            } else null
            val textColor = if (timeOfDay == TimeOfDay.NIGHT) Color.White.copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.7f)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // Pulsating concentric ring representing the casting target area in the river water
                Box(
                    modifier = Modifier
                        .size(115.dp)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                            alpha = pulseAlpha
                        }
                        .drawBehind {
                            drawCircle(
                                color = if (timeOfDay == TimeOfDay.NIGHT) Color.White.copy(alpha = 0.5f) else Color(0xFF558E95).copy(alpha = 0.45f),
                                radius = size.minDimension / 2.3f,
                                style = Stroke(
                                    width = 2f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                                )
                            )
                            drawCircle(
                                color = if (timeOfDay == TimeOfDay.NIGHT) Color.White.copy(alpha = 0.12f) else Color(0xFF558E95).copy(alpha = 0.08f),
                                radius = size.minDimension / 4.5f
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✨",
                        fontSize = 20.sp,
                        color = if (timeOfDay == TimeOfDay.NIGHT) Color.White else Color(0xFF558E95),
                        modifier = Modifier.graphicsLayer {
                            val wigglePhase = (animationTicks % 60) / 60f * 2 * Math.PI.toFloat()
                            translationY = sin(wigglePhase) * 6f
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Beautiful, delicate, clear helper text matching the aesthetic theme
                Box(
                    modifier = Modifier
                        .background(
                            color = if (timeOfDay == TimeOfDay.NIGHT) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.04f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (language) {
                                AppLanguage.KO -> "조용히 강가를 터치하여 첫 낚싯줄을 흘려보세요"
                                AppLanguage.JA -> "静かに川面をタップして、最初の釣り糸を流してみましょう"
                                else -> "Gently tap the river water to cast your first fishing line"
                            },
                            fontSize = 13.sp,
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            style = LocalTextStyle.current.copy(shadow = textShadow)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (language) {
                                AppLanguage.KO -> "물결과 바람을 느끼며 차분하게 기다립니다"
                                AppLanguage.JA -> "心地よい波と風を感じながら、穏やかに待ちましょう"
                                else -> "Listen to the wave ripples and relax in quiet focus"
                            },
                            fontSize = 11.sp,
                            color = textColor.copy(alpha = 0.55f),
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            style = LocalTextStyle.current.copy(shadow = textShadow)
                        )
                    }
                }
            }
        }

        // --- 2.8. INTERACTIVE RHYTHM-BASED FISHING REELING MINI-GAME ---
        AnimatedVisibility(
            visible = fishingState == FishingState.REELING,
            enter = fadeIn(animationSpec = tween(500)) + scaleIn(animationSpec = spring(dampingRatio = 0.8f)),
            exit = fadeOut(animationSpec = tween(400)) + scaleOut(animationSpec = tween(300)),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp).testTag("rhythm_minigame_overlay")
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (timeOfDay == TimeOfDay.NIGHT) {
                        Color(0xFF13192F).copy(alpha = 0.92f)
                    } else {
                        Color(0xFFF7F9FF).copy(alpha = 0.96f)
                    }
                ),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .widthIn(max = 350.dp)
                    .shadow(16.dp, RoundedCornerShape(32.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Beautiful Title
                    Text(
                        text = when (language) {
                            AppLanguage.KO -> "리듬에 맞춰 낚아채세요!"
                            AppLanguage.JA -> "リズムに合わせてタップ！"
                            else -> "Reel with the Rhythm!"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (timeOfDay == TimeOfDay.NIGHT) Color(0xFFFFB4AB) else Color(0xFF558E95),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = when (language) {
                            AppLanguage.KO -> "줄어드는 푸른 고리가 붉은 과녁에 맞을 때 터치!"
                            AppLanguage.JA -> "青い円が赤い的と重なるタイミングでタップ！"
                            else -> "Tap when the powder-blue ring hits the pink circle"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (timeOfDay == TimeOfDay.NIGHT) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // THE CORE VISUAL RHYTHM RING
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(160.dp)
                            .drawBehind {
                                // Draw target sweet-spot ring in beautiful pastel peach/pink color
                                drawCircle(
                                    color = Color(0xFFFFD1D1).copy(alpha = 0.65f), // Pastel light pink target
                                    radius = size.minDimension * 0.35f,
                                    style = Stroke(
                                        width = 14f,
                                        cap = StrokeCap.Round
                                    )
                                )
                                
                                // Draw sweet spot outline
                                drawCircle(
                                    color = Color(0xFFFFE3E3),
                                    radius = size.minDimension * 0.35f,
                                    style = Stroke(
                                        width = 2f,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                    )
                                )

                                // Draw contracting interactive ring in bright, glowing pastel blue/lavender
                                if (rhythmBeatActive) {
                                    drawCircle(
                                        color = Color(0xFFB0E0E6).copy(alpha = 0.9f), // Powder blue
                                        radius = size.minDimension * 0.35f * (rhythmRingScale / 0.35f),
                                        style = Stroke(
                                            width = 8f,
                                            cap = StrokeCap.Round
                                        )
                                    )
                                }
                            }
                    ) {
                        // Center icon/graphic: A lovely fish or bobber icon
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    color = if (timeOfDay == TimeOfDay.NIGHT) Color(0xFF2E3B5C) else Color(0xFFEBF5F7),
                                    shape = CircleShape
                                )
                        ) {
                            Text(
                                text = "🐟",
                                fontSize = 28.sp,
                                modifier = Modifier.graphicsLayer {
                                    // Gentle struggle wagging animation when reeling
                                    val wiggle = sin((animationTicks * 0.012f).toDouble()).toFloat() * 10f
                                    rotationZ = wiggle
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // REAL-TIME FEEDBACK ("PERFECT!", "GOOD!", "MISS")
                    Box(
                        modifier = Modifier.height(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = rhythmFeedbackText,
                            transitionSpec = {
                                scaleIn(animationSpec = spring(dampingRatio = 0.6f)).togetherWith(fadeOut(animationSpec = tween(200)))
                            }
                        ) { text ->
                            if (text != null) {
                                val feedbackColor = when (text) {
                                    "PERFECT!" -> Color(0xFF5ABCA3) // Pastel mint
                                    "GOOD!" -> Color(0xFFFEC89A)    // Pastel peach
                                    else -> Color(0xFFFF9B9B)       // Pastel light red/pink
                                }
                                Text(
                                    text = text,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = feedbackColor,
                                    letterSpacing = 1.2.sp
                                )
                            } else {
                                Text(
                                    text = "•••",
                                    fontSize = 16.sp,
                                    color = Color.Gray.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // SCORE PROGRESS PIPS (HITS / MISSES)
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(0.95f)
                    ) {
                        // Hits progress
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = when (language) {
                                    AppLanguage.KO -> "성공 (3회 필요)"
                                    AppLanguage.JA -> "成功 (3回必要)"
                                    else -> "Hits (Need 3)"
                                },
                                fontSize = 9.sp,
                                color = if (timeOfDay == TimeOfDay.NIGHT) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(3) { index ->
                                    val isHit = index < rhythmHitCount
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(
                                                color = if (isHit) Color(0xFFB4F8C8) else Color.Gray.copy(alpha = 0.2f),
                                                shape = CircleShape
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isHit) Color(0xFF86E3CE) else Color.Transparent,
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }
                        }

                        // Misses limit
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = when (language) {
                                    AppLanguage.KO -> "놓침 (최대 3회)"
                                    AppLanguage.JA -> "ミス (最大3回)"
                                    else -> "Misses (Max 3)"
                                },
                                fontSize = 9.sp,
                                color = if (timeOfDay == TimeOfDay.NIGHT) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(3) { index ->
                                    val isMiss = index < rhythmMissCount
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(
                                                color = if (isMiss) Color(0xFFFFAAA6) else Color.Gray.copy(alpha = 0.2f),
                                                shape = CircleShape
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isMiss) Color(0xFFFF8B94) else Color.Transparent,
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // A large clickable / touchable interactive box for convenience
                    Button(
                        onClick = { viewModel.handleScreenTap() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (timeOfDay == TimeOfDay.NIGHT) Color(0xFFFFB4AB).copy(alpha = 0.85f) else Color(0xFF90C2C8)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth().testTag("rhythm_tap_button")
                    ) {
                        Text(
                            text = when (language) {
                                AppLanguage.KO -> "지금 터치!"
                                AppLanguage.JA -> "タイミングよくタップ！"
                                else -> "TAP NOW!"
                            },
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // --- 3. MINIMALIST STATE INSTRUCTIONS ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = fishingState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(600)) with fadeOut(animationSpec = tween(400))
                }
            ) { targetState ->
                val hint = when (targetState) {
                    FishingState.IDLE -> when (language) {
                        AppLanguage.KO -> "강가를 터치하여 찌를 드리우세요."
                        AppLanguage.JA -> "川面をタップして釣り糸を垂らしましょう。"
                        else -> "Tap the river to cast your line."
                    }
                    FishingState.CASTING -> when (language) {
                        AppLanguage.KO -> "낚싯줄을 가볍게 던집니다..."
                        AppLanguage.JA -> "釣り糸をうっすらと投げ入れています..."
                        else -> "Casting the line into the water..."
                    }
                    FishingState.WAITING -> when (language) {
                        AppLanguage.KO -> "물이 잔잔합니다. 느긋함을 배우세요."
                        AppLanguage.JA -> "水が静かです。のんびりと待ちましょう。"
                        else -> "The water is calm. Enjoy the wait."
                    }
                    FishingState.NIBBLE -> when (language) {
                        AppLanguage.KO -> "찌가 미세하게 움찔거립니다..."
                        AppLanguage.JA -> "ウキがかすかにピクピクと動き出しました..."
                        else -> "The bobber is twitching slightly..."
                    }
                    FishingState.BITE -> when (language) {
                        AppLanguage.KO -> "챔질을 시작하세요! 화면을 터치하세요!"
                        AppLanguage.JA -> "アタリ！今すぐ画面をタップしてください！"
                        else -> "Strike! Tap the screen now!"
                    }
                    FishingState.REELING -> when (language) {
                        AppLanguage.KO -> "낚싯줄을 감아올리고 물고기와 힘을 겨룹니다!"
                        AppLanguage.JA -> "リールを巻いています！魚との心地よい駆け引き..."
                        else -> "Reeling in! Struggling with the fish..."
                    }
                    FishingState.SPLASHING -> when (language) {
                        AppLanguage.KO -> "물고기가 은빛 비늘을 흔들며 물을 튀깁니다!"
                        AppLanguage.JA -> "魚が飛び跳ね、銀色のウロコが光を放ちました！"
                        else -> "The fish leaps, splashing silver scales!"
                    }
                    FishingState.CAUGHT -> when (language) {
                        AppLanguage.KO -> "대단한 수확입니다!"
                        AppLanguage.JA -> "素晴らしい大物をつり上げました！"
                        else -> "A wonderful catch!"
                    }
                    FishingState.LOST -> when (language) {
                        AppLanguage.KO -> "아깝게 물고기가 유턴했습니다."
                        AppLanguage.JA -> "惜しい！魚がすり抜けて戻っていきました。"
                        else -> "Aww, the fish swam away..."
                    }
                }

                val textColor = if (timeOfDay == TimeOfDay.NIGHT) Color.White.copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.65f)
                val textShadow = if (timeOfDay == TimeOfDay.NIGHT) {
                    Shadow(color = Color.Black, offset = Offset(1f, 1f), blurRadius = 4f)
                } else null

                Text(
                    text = hint,
                    color = textColor,
                    fontSize = 15.sp,
                    style = LocalTextStyle.current.copy(shadow = textShadow),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // --- 4. EXHILARATING FISH SPLASH/CAUGHT FULL CARD DIALOG ---
        if (fishingState == FishingState.SPLASHING || fishingState == FishingState.CAUGHT) {
            lastCaughtFish?.let { fishEntity ->
                val species = FishSpecies.find(fishEntity.speciesId)
                if (species != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                            .pointerInput(Unit) { detectTapGestures { viewModel.handleScreenTap() } },
                        contentAlignment = Alignment.Center
                    ) {
                        // Celebrating water bubbles flowing beautifully in the background
                        if (fishingState == FishingState.CAUGHT) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                for (b in caughtBubbles) {
                                    // Outer soft glow
                                    drawCircle(
                                        color = b.color.copy(alpha = b.alpha * 0.18f),
                                        radius = b.size + 4f,
                                        center = Offset(b.x, b.y)
                                    )
                                    // Shimmering bubble border
                                    drawCircle(
                                        color = b.color.copy(alpha = b.alpha),
                                        radius = b.size,
                                        center = Offset(b.x, b.y),
                                        style = Stroke(width = 2f)
                                    )
                                    // Specular lighting reflection glint
                                    drawCircle(
                                        color = Color.White.copy(alpha = b.alpha * 0.82f),
                                        radius = b.size * 0.22f,
                                        center = Offset(b.x - b.size * 0.35f, b.y - b.size * 0.35f)
                                    )
                                }
                            }
                        }

                        // Rotation transition simulating real 3D cards in Godot/Unity
                        val infiniteTransition = rememberInfiniteTransition()
                        val rotateY by infiniteTransition.animateFloat(
                            initialValue = -12f,
                            targetValue = 12f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2200, easing = EaseInOutSine),
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                        val scaleCard by transitionScaleFloat()

                        Card(
                            modifier = Modifier
                                .widthIn(max = 350.dp)
                                .padding(24.dp)
                                .scale(scaleCard)
                                .rotate(rotateY)
                                .testTag("caught_fish_card"),
                            colors = CardDefaults.cardColors(
                                containerColor = species.color.copy(alpha = 0.95f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 24.dp),
                            shape = RoundedCornerShape(32.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = species.localizedRarity(language),
                                    color = Color.Black.copy(alpha = 0.55f),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.6.sp,
                                    fontSize = 11.sp,
                                    modifier = Modifier
                                        .background(
                                            Color.White.copy(alpha = 0.5f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = species.localizedName(language),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black.copy(alpha = 0.85f),
                                    fontSize = 28.sp
                                )

                                Text(
                                    text = species.scientificName,
                                    style = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                                    color = Color.Black.copy(alpha = 0.45f),
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Render beautiful stylized Poly-Fish on its own Mini Canvas
                                Canvas(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                        .padding(8.dp)
                                ) {
                                    drawLowPolyCapturedFish(species, animationTicks)
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = when (language) {
                                                AppLanguage.KO -> "길이"
                                                AppLanguage.JA -> "長さ"
                                                else -> "Length"
                                            },
                                            fontSize = 11.sp,
                                            color = Color.Black.copy(alpha = 0.45f)
                                        )
                                        Text(
                                            text = String.format("%.1f cm", fishEntity.length),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black.copy(alpha = 0.82f)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = when (language) {
                                                AppLanguage.KO -> "무게"
                                                AppLanguage.JA -> "重さ"
                                                else -> "Weight"
                                            },
                                            fontSize = 11.sp,
                                            color = Color.Black.copy(alpha = 0.45f)
                                        )
                                        Text(
                                            text = String.format("%.2f kg", fishEntity.weight),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black.copy(alpha = 0.82f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = species.localizedDescription(language),
                                    color = Color.Black.copy(alpha = 0.72f),
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                     text = when (language) {
                                         AppLanguage.KO -> "화면을 터치해서 강가로 복귀"
                                         AppLanguage.JA -> "画面をタッチして川辺に戻る"
                                         else -> "Tap anywhere to return to the river"
                                     },
                                     fontSize = 11.sp,
                                     fontWeight = FontWeight.Medium,
                                     color = Color.Black.copy(alpha = 0.4f)
                                 )
                             }
                         }
                     }
                 }
             }
         }

         // --- 5. THE CORNER-ALIGNED INTERACTIVE FISH BOOK (MINIMAL ENCYCLOPEDIA) ---
         Box(
             modifier = Modifier
                 .padding(start = 16.dp, bottom = 16.dp)
                 .align(Alignment.BottomStart)
                 .navigationBarsPadding(),
             contentAlignment = Alignment.BottomStart
         ) {
             // Anchor Handle Always visible so users know they can view the book in a minimalist way
             if (!isEncyclopediaOpen && !isAquariumOpen && !isUpgradeOpen && !isShopOpen && !isQuestOpen && !isSpotSelectOpen && !isGpgRankingOpen && fishingState == FishingState.IDLE) {
                 Column(
                     modifier = Modifier.padding(bottom = 8.dp),
                     verticalArrangement = Arrangement.spacedBy(8.dp),
                     horizontalAlignment = Alignment.Start
                 ) {
                     // Floating Quick Bait Select Overlay
                     androidx.compose.animation.AnimatedVisibility(
                         visible = showBaitQuickSelect,
                         enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                         exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
                     ) {
                         Card(
                             colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
                             shape = RoundedCornerShape(16.dp),
                             border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                             modifier = Modifier.width(280.dp).shadow(12.dp, RoundedCornerShape(16.dp))
                         ) {
                             Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                 Text(
                                     text = if (isEnglish) "Select Fishing Bait" else "🎣 사용할 미끼 선택",
                                     fontSize = 12.sp,
                                     fontWeight = FontWeight.Bold,
                                     color = MaterialTheme.colorScheme.primary
                                 )
                                 Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                 
                                 BaitType.values().forEach { bait ->
                                     val owned = viewModel.getBaitCount(bait)
                                     val isSelected = activeBait == bait
                                     
                                     Row(
                                         modifier = Modifier
                                             .fillMaxWidth()
                                             .clip(RoundedCornerShape(8.dp))
                                             .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent)
                                             .clickable {
                                                 if (bait == BaitType.BASIC || owned > 0) {
                                                     viewModel.selectBait(bait)
                                                     showBaitQuickSelect = false
                                                 }
                                             }
                                             .padding(horizontal = 8.dp, vertical = 6.dp),
                                         verticalAlignment = Alignment.CenterVertically
                                     ) {
                                         Text(text = bait.emoji, fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
                                         Column(modifier = Modifier.weight(1f)) {
                                             Text(
                                                 text = if (language == AppLanguage.EN) bait.nameEn else if (language == AppLanguage.JA) bait.nameJa else bait.nameKo,
                                                 fontSize = 11.sp,
                                                 fontWeight = FontWeight.Bold
                                             )
                                             Text(
                                                 text = if (language == AppLanguage.EN) bait.descriptionEn else if (language == AppLanguage.JA) bait.descriptionJa else bait.descriptionKo,
                                                 fontSize = 9.sp,
                                                 color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                             )
                                         }
                                         Text(
                                             text = if (bait == BaitType.BASIC) "∞" else "${owned}개",
                                             fontSize = 10.sp,
                                             fontWeight = FontWeight.Bold,
                                             color = if (owned > 0 || bait == BaitType.BASIC) MaterialTheme.colorScheme.primary else Color.Gray
                                         )
                                     }
                                 }
                             }
                         }
                     }

                     // Scrollable Action Cards Row
                     Row(
                         modifier = Modifier
                             .fillMaxWidth()
                             .horizontalScroll(rememberScrollState()),
                         horizontalArrangement = Arrangement.spacedBy(8.dp),
                         verticalAlignment = Alignment.CenterVertically
                     ) {
                     Card(
                         modifier = Modifier
                             .clickable { isEncyclopediaOpen = true }
                             .shadow(6.dp, RoundedCornerShape(20.dp)),
                         colors = CardDefaults.cardColors(
                             containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                         ),
                         shape = RoundedCornerShape(20.dp)
                     ) {
                         Row(
                             modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                             verticalAlignment = Alignment.CenterVertically,
                             horizontalArrangement = Arrangement.spacedBy(6.dp)
                         ) {
                             Text(text = "📘", fontSize = 13.sp)
                             Text(
                                 text = if (isEnglish) "Fish Book" else "물고기 도감",
                                 fontSize = 11.sp,
                                 fontWeight = FontWeight.Bold,
                                 color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                             )
                         }
                     }

                     Card(
                         modifier = Modifier
                             .clickable { isAquariumOpen = true }
                             .shadow(6.dp, RoundedCornerShape(20.dp)),
                         colors = CardDefaults.cardColors(
                             containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                         ),
                         shape = RoundedCornerShape(20.dp)
                     ) {
                         Row(
                             modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                             verticalAlignment = Alignment.CenterVertically,
                             horizontalArrangement = Arrangement.spacedBy(6.dp)
                         ) {
                             Text(text = "🐠", fontSize = 13.sp)
                             Text(
                                 text = if (isEnglish) "Aquarium" else "나의 수족관",
                                 fontSize = 11.sp,
                                 fontWeight = FontWeight.Bold,
                                 color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                             )
                         }
                     }

                     Card(
                         modifier = Modifier
                             .clickable { isUpgradeOpen = true }
                             .shadow(6.dp, RoundedCornerShape(20.dp)),
                         colors = CardDefaults.cardColors(
                             containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                         ),
                         shape = RoundedCornerShape(20.dp)
                     ) {
                         Row(
                             modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                             verticalAlignment = Alignment.CenterVertically,
                             horizontalArrangement = Arrangement.spacedBy(6.dp)
                         ) {
                             Text(text = "🔧", fontSize = 13.sp)
                             Text(
                                 text = if (isEnglish) "Upgrade" else "장비 강화",
                                 fontSize = 11.sp,
                                 fontWeight = FontWeight.Bold,
                                 color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                             )
                         }
                     }

                     Card(
                         modifier = Modifier
                             .clickable { isShopOpen = true }
                             .shadow(6.dp, RoundedCornerShape(20.dp)),
                         colors = CardDefaults.cardColors(
                             containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                         ),
                         shape = RoundedCornerShape(20.dp)
                     ) {
                         Row(
                             modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                             verticalAlignment = Alignment.CenterVertically,
                             horizontalArrangement = Arrangement.spacedBy(6.dp)
                         ) {
                             Text(text = "🏪", fontSize = 13.sp)
                             Text(
                                 text = if (isEnglish) "Market" else "물고기 상점",
                                 fontSize = 11.sp,
                                 fontWeight = FontWeight.Bold,
                                 color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                             )
                         }
                     }

                                           // New Action Cards: Quests, Spot Select, Rankings
                      Card(
                          modifier = Modifier
                              .clickable { isQuestOpen = true }
                              .shadow(6.dp, RoundedCornerShape(20.dp)),
                          colors = CardDefaults.cardColors(
                              containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                          ),
                          shape = RoundedCornerShape(20.dp)
                      ) {
                          Row(
                              modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                              verticalAlignment = Alignment.CenterVertically,
                              horizontalArrangement = Arrangement.spacedBy(6.dp)
                          ) {
                              Text(text = "🏆", fontSize = 13.sp)
                              Text(
                                  text = if (isEnglish) "Quests" else "퀘스트 & 업적",
                                  fontSize = 11.sp,
                                  fontWeight = FontWeight.Bold,
                                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                              )
                          }
                      }

                      Card(
                          modifier = Modifier
                              .clickable { isSpotSelectOpen = true }
                              .shadow(6.dp, RoundedCornerShape(20.dp)),
                          colors = CardDefaults.cardColors(
                              containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                          ),
                          shape = RoundedCornerShape(20.dp)
                      ) {
                          Row(
                              modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                              verticalAlignment = Alignment.CenterVertically,
                              horizontalArrangement = Arrangement.spacedBy(6.dp)
                          ) {
                              Text(text = viewModel.currentSpot.collectAsState().value.emoji, fontSize = 13.sp)
                              Text(
                                  text = if (isEnglish) "Spots" else "야외 낚시터",
                                  fontSize = 11.sp,
                                  fontWeight = FontWeight.Bold,
                                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                              )
                          }
                      }

                      Card(
                          modifier = Modifier
                              .clickable { isGpgRankingOpen = true }
                              .shadow(6.dp, RoundedCornerShape(20.dp)),
                          colors = CardDefaults.cardColors(
                              containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                          ),
                          shape = RoundedCornerShape(20.dp)
                      ) {
                          Row(
                              modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                              verticalAlignment = Alignment.CenterVertically,
                              horizontalArrangement = Arrangement.spacedBy(6.dp)
                          ) {
                              Text(text = "🟢", fontSize = 13.sp)
                              Text(
                                  text = if (isEnglish) "Rankings" else "실시간 랭킹",
                                  fontSize = 11.sp,
                                  fontWeight = FontWeight.Bold,
                                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                              )
                          }
                      }

                      // Active/Equipped Bait Indicator Card
                     Card(
                         modifier = Modifier
                             .clickable { showBaitQuickSelect = !showBaitQuickSelect }
                             .shadow(6.dp, RoundedCornerShape(20.dp)),
                         colors = CardDefaults.cardColors(
                             containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
                         ),
                         shape = RoundedCornerShape(20.dp)
                     ) {
                         Row(
                             modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                             verticalAlignment = Alignment.CenterVertically,
                             horizontalArrangement = Arrangement.spacedBy(6.dp)
                         ) {
                             Text(text = activeBait.emoji, fontSize = 13.sp)
                             Text(
                                 text = when (language) {
                                     AppLanguage.KO -> activeBait.nameKo
                                     AppLanguage.JA -> activeBait.nameJa
                                     else -> activeBait.nameEn
                                 } + if (activeBait != BaitType.BASIC) " (${viewModel.getBaitCount(activeBait)})" else " (∞)",
                                 fontSize = 11.sp,
                                 fontWeight = FontWeight.Bold,
                                 color = MaterialTheme.colorScheme.onPrimaryContainer
                             )
                         }
                     }
                  }
              }
          }
          }

         if (false) {
             if (!isEncyclopediaOpen && fishingState == FishingState.IDLE) {
                 Card(
                     modifier = Modifier
                         .clickable { isEncyclopediaOpen = true }
                         .shadow(6.dp, RoundedCornerShape(20.dp)),
                     colors = CardDefaults.cardColors(
                         containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                     ),
                     shape = RoundedCornerShape(20.dp)
                 ) {
                     Row(
                         modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                         verticalAlignment = Alignment.CenterVertically,
                         horizontalArrangement = Arrangement.spacedBy(6.dp)
                     ) {
                         Text(text = "📘", fontSize = 13.sp)
                         Text(
                             text = if (isEnglish) "Fish Book" else "물고기 도감",
                             fontSize = 11.sp,
                             fontWeight = FontWeight.Bold,
                             color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                         )
                     }
                 }
             }

             AnimatedVisibility(
                 visible = isEncyclopediaOpen,
                 enter = slideInVertically(
                     initialOffsetY = { it },
                     animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
                 ),
                 exit = slideOutVertically(
                     targetOffsetY = { it },
                     animationSpec = tween(350, easing = FastOutSlowInEasing)
                 )
             ) {
                 EncyclopediaDrawerContent(
                    speciesList = FishSpecies.list,
                    caughtList = caughtFishList,
                    language = language,
                    onClose = { isEncyclopediaOpen = false },
                    onSelectFish = { selectedFishDetail = it },
                    onReset = { viewModel.releaseAllCaughtFish() }
                )
            }

             AnimatedVisibility(
                 visible = isAquariumOpen,
                 enter = slideInVertically(
                     initialOffsetY = { it },
                     animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
                 ),
                 exit = slideOutVertically(
                     targetOffsetY = { it },
                     animationSpec = tween(350, easing = FastOutSlowInEasing)
                 )
             ) {
                 AquariumDrawerContent(
                     viewModel = viewModel,
                     caughtList = caughtFishList,
                     language = language,
                     animationTicks = animationTicks,
                     onClose = { isAquariumOpen = false },
                     onSelectFish = { selectedFishDetail = it },
                     onReset = { viewModel.releaseAllCaughtFish() }
                 )
             }

             AnimatedVisibility(
                 visible = isUpgradeOpen,
                 enter = slideInVertically(
                     initialOffsetY = { it },
                     animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
                 ),
                 exit = slideOutVertically(
                     targetOffsetY = { it },
                     animationSpec = tween(350, easing = FastOutSlowInEasing)
                 )
             ) {
                 UpgradeDrawerContent(
                     coins = coins,
                     rodLevel = rodLevel,
                     upgradeCost = viewModel.getUpgradeCost(),
                     language = language,
                     onClose = { isUpgradeOpen = false },
                     onUpgrade = { viewModel.upgradeRod() }
                 )
             }

             AnimatedVisibility(
                 visible = isShopOpen,
                 enter = slideInVertically(
                     initialOffsetY = { it },
                     animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
                 ),
                 exit = slideOutVertically(
                     targetOffsetY = { it },
                     animationSpec = tween(350, easing = FastOutSlowInEasing)
                 )
             ) {
                 FishShopDrawerContent(
                     caughtList = caughtFishList,
                     language = language,
                     onClose = { isShopOpen = false },
                     onSellFish = { viewModel.sellFish(it) },
                     onSellAll = { viewModel.sellAllFish(caughtFishList) },
                     viewModel = viewModel
                 )
             }
        }

                 // --- Added 5b. Daily Quests and Achievements Drawer ---
         AnimatedVisibility(
             visible = isQuestOpen,
             enter = slideInVertically(
                 initialOffsetY = { it },
                 animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
             ),
             exit = slideOutVertically(
                 targetOffsetY = { it },
                 animationSpec = tween(350, easing = FastOutSlowInEasing)
             )
         ) {
             QuestDrawerContent(
                 viewModel = viewModel,
                 onClose = { isQuestOpen = false }
             )
         }

         // --- Added 5c. Fishing Spot Select Drawer ---
         AnimatedVisibility(
             visible = isSpotSelectOpen,
             enter = slideInVertically(
                 initialOffsetY = { it },
                 animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
             ),
             exit = slideOutVertically(
                 targetOffsetY = { it },
                 animationSpec = tween(350, easing = FastOutSlowInEasing)
             )
         ) {
             SpotSelectDrawerContent(
                 viewModel = viewModel,
                 onClose = { isSpotSelectOpen = false }
             )
         }

         // --- Added 5d. Google Play Games Leaderboard Drawer ---
         AnimatedVisibility(
             visible = isGpgRankingOpen,
             enter = slideInVertically(
                 initialOffsetY = { it },
                 animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
             ),
             exit = slideOutVertically(
                 targetOffsetY = { it },
                 animationSpec = tween(350, easing = FastOutSlowInEasing)
             )
         ) {
             GpgRankingDrawerContent(
                 viewModel = viewModel,
                 onClose = { isGpgRankingOpen = false }
             )
         }

         // --- Added top-level Floating In-App Success Alert ---
         val inAppAlertState by viewModel.inAppAlert.collectAsState()
         AnimatedVisibility(
             visible = inAppAlertState != null,
             enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
             exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
             modifier = Modifier.align(Alignment.TopCenter).padding(top = 40.dp).zIndex(99f)
         ) {
             inAppAlertState?.let { alert ->
                 Card(
                     modifier = Modifier
                         .fillMaxWidth(0.9f)
                         .clickable { viewModel.dismissInAppAlert() }
                         .shadow(12.dp, RoundedCornerShape(16.dp)),
                     colors = CardDefaults.cardColors(
                         containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.96f)
                     ),
                     shape = RoundedCornerShape(16.dp),
                     border = BorderStroke(2.dp, alert.color.copy(alpha = 0.6f))
                 ) {
                     Row(
                         modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                         verticalAlignment = Alignment.CenterVertically,
                         horizontalArrangement = Arrangement.spacedBy(11.dp)
                     ) {
                         Text(text = alert.emoji, fontSize = 24.sp)
                         Column(modifier = Modifier.weight(1f)) {
                             Text(
                                 text = alert.title,
                                 fontWeight = FontWeight.Bold,
                                 fontSize = 14.sp,
                                 color = MaterialTheme.colorScheme.onSurfaceVariant
                             )
                             Spacer(modifier = Modifier.height(2.dp))
                             Text(
                                 text = alert.message,
                                 fontSize = 12.sp,
                                 color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                             )
                         }
                         IconButton(
                             onClick = { viewModel.dismissInAppAlert() },
                             modifier = Modifier.size(24.dp)
                         ) {
                             Text(text = "✕", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                         }
                     }
                 }
             }
         }

         // --- 6. INDIVIDUAL FISH DETAILS POPUP ---
        if (selectedFishDetail != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                val fish = selectedFishDetail!!
                val catches = caughtFishList.filter { it.speciesId == fish.id }
                val maxLen = catches.maxOfOrNull { it.length } ?: 0f
                val maxWt = catches.maxOfOrNull { it.weight } ?: 0f

                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = fish.color.copy(alpha = 0.98f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (language) {
                                    AppLanguage.KO -> "도감 정보"
                                    AppLanguage.JA -> "図鑑詳細"
                                    else -> "Species Detail"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black.copy(alpha = 0.4f)
                            )
                            IconButton(onClick = { selectedFishDetail = null }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = when (language) {
                                        AppLanguage.KO -> "닫기"
                                        AppLanguage.JA -> "閉じる"
                                        else -> "Close"
                                    },
                                    tint = Color.Black.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Text(
                            text = fish.localizedName(language),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black.copy(alpha = 0.85f)
                        )
                        Text(
                            text = fish.scientificName,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Black.copy(alpha = 0.5f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Draw Fish in canvas
                        Canvas(
                            modifier = Modifier
                                .size(120.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            drawLowPolyCapturedFish(fish, animationTicks)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = when (language) {
                                        AppLanguage.KO -> "희귀도"
                                        AppLanguage.JA -> "レア度"
                                        else -> "Rarity"
                                    },
                                    fontSize = 11.sp,
                                    color = Color.Black.copy(alpha = 0.45f)
                                )
                                Text(
                                    text = fish.localizedRarity(language),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (fish.rarity) {
                                        "전설" -> Color(0xFFD43100)
                                        "신화" -> Color(0xFF6F00D4)
                                        else -> Color.Black.copy(alpha = 0.7f)
                                    }
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = when (language) {
                                        AppLanguage.KO -> "출몰 시간"
                                        AppLanguage.JA -> "出現時間"
                                        else -> "Preferred Time"
                                    },
                                    fontSize = 11.sp,
                                    color = Color.Black.copy(alpha = 0.45f)
                                )
                                Text(
                                    text = fish.localizedOptimalTime(language),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black.copy(alpha = 0.7f)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = when (language) {
                                        AppLanguage.KO -> "선호 날씨"
                                        AppLanguage.JA -> "好まれる天候"
                                        else -> "Fav Weather"
                                    },
                                    fontSize = 11.sp,
                                    color = Color.Black.copy(alpha = 0.45f)
                                )
                                Text(
                                    text = fish.localizedOptimalWeather(language),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = when (language) {
                                    AppLanguage.KO -> "나의 기록 (${catches.size}회 포획)"
                                    AppLanguage.JA -> "獲得した記録 (${catches.size}回捕獲)"
                                    else -> "My Record (${catches.size} caught)"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black.copy(alpha = 0.5f)
                             )
                             if (catches.isNotEmpty()) {
                                 Text(
                                     text = when (language) {
                                         AppLanguage.KO -> String.format("최대 길이: %.1f cm", maxLen)
                                         AppLanguage.JA -> String.format("最大体長: %.1f cm", maxLen)
                                         else -> String.format("Max Length: %.1f cm", maxLen)
                                     },
                                     fontSize = 13.sp,
                                     color = Color.Black.copy(alpha = 0.8f)
                                 )
                                 Text(
                                     text = when (language) {
                                         AppLanguage.KO -> String.format("최고 무게: %.2f kg", maxWt)
                                         AppLanguage.JA -> String.format("最大重量: %.2f kg", maxWt)
                                         else -> String.format("Max Weight: %.2f kg", maxWt)
                                     },
                                     fontSize = 13.sp,
                                     color = Color.Black.copy(alpha = 0.8f)
                                 )
                             } else {
                                 Text(
                                     text = when (language) {
                                         AppLanguage.KO -> "아직 포획하지 않았습니다."
                                         AppLanguage.JA -> "まだ獲得していません。"
                                         else -> "Not caught yet."
                                     },
                                     fontSize = 13.sp,
                                     color = Color.Black.copy(alpha = 0.5f)
                                 )
                             }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = fish.localizedDescription(language),
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = Color.Black.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // --- 7. LEVEL UP CELEBRATORY CONGRATULATIONS DIALOG OVERLAY ---
        if (showLevelUpDialog != null) {
            val reachedLevel = showLevelUpDialog!!
            
            // Particles system state
            val sparkles = remember { mutableStateListOf<CelebrationSparkle>() }
            
            // Animation values for spring card entrance
            val scale = remember { androidx.compose.animation.core.Animatable(0.5f) }
            val alphaVal = remember { androidx.compose.animation.core.Animatable(0f) }
            
            // Breathing aura pulse
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val auraPulse by infiniteTransition.animateFloat(
                initialValue = 0.85f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2200, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "aura"
            )

            LaunchedEffect(reachedLevel) {
                // Initialize particles
                sparkles.clear()
                val random = java.util.Random()
                repeat(40) {
                    sparkles.add(
                        CelebrationSparkle(
                            x = random.nextFloat(),
                            y = random.nextFloat() * 0.8f + 0.1f,
                            speedX = (random.nextFloat() - 0.5f) * 0.003f,
                            speedY = -0.0015f - random.nextFloat() * 0.003f,
                            color = when (random.nextInt(4)) {
                                0 -> Color(0xFFA5E6D8) // Pastel Mint
                                1 -> Color(0xFFFFB2A0) // Pastel Coral
                                2 -> Color(0xFFFFF6B8) // Pastel Yellow
                                else -> Color(0xFFE5C0F5) // Pastel Violet
                            },
                            size = 5f + random.nextFloat() * 12f,
                            maxLife = 1.5f + random.nextFloat() * 2.5f,
                            life = random.nextFloat() * 1.5f
                        )
                    )
                }

                // Elegantly scale card inside
                launch {
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
                launch {
                    alphaVal.animateTo(1f, animationSpec = tween(600))
                }

                // Smooth particle update cycle
                while (isActive) {
                    for (i in sparkles.indices.reversed()) {
                        val s = sparkles[i]
                        s.y += s.speedY
                        s.x += s.speedX
                        s.life -= 0.016f
                        if (s.life <= 0f || s.y < -0.05f) {
                            sparkles[i] = CelebrationSparkle(
                                x = random.nextFloat(),
                                y = 1.05f,
                                speedX = (random.nextFloat() - 0.5f) * 0.003f,
                                speedY = -0.0015f - random.nextFloat() * 0.003f,
                                color = when (random.nextInt(4)) {
                                    0 -> Color(0xFFA5E6D8)
                                    1 -> Color(0xFFFFB2A0)
                                    2 -> Color(0xFFFFF6B8)
                                    else -> Color(0xFFE5C0F5)
                                },
                                size = 5f + random.nextFloat() * 12f,
                                maxLife = 1.8f + random.nextFloat() * 2f,
                                life = 1.8f + random.nextFloat() * 2f
                            )
                        }
                    }
                    delay(16)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f * alphaVal.value))
                    .pointerInput(Unit) {
                        detectTapGestures { viewModel.dismissLevelUpDialog() }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Background celebratory canvas drawing radiant glowing auras and floating magical sparkles
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerOffset = Offset(size.width / 2f, size.height / 2f)
                    
                    // 1. Soft pulsing dynamic aura
                    val currentAuraRadius = 300.dp.toPx() * auraPulse
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFB2A0).copy(alpha = 0.22f * alphaVal.value),
                                Color(0xFFA5E6D8).copy(alpha = 0.12f * alphaVal.value),
                                Color.Transparent
                            ),
                            center = centerOffset,
                            radius = currentAuraRadius
                        ),
                        radius = currentAuraRadius,
                        center = centerOffset
                    )

                    // 2. Starburst spark particles
                    sparkles.forEach { s ->
                        val alpha = (s.life / s.maxLife).coerceIn(0f, 1f) * alphaVal.value
                        val cx = s.x * size.width
                        val cy = s.y * size.height
                        
                        // Draw standard glowing sparkles using 4-pointed star path
                        val path = Path().apply {
                            moveTo(cx, cy - s.size)
                            quadraticTo(cx, cy, cx + s.size, cy)
                            quadraticTo(cx, cy, cx, cy + s.size)
                            quadraticTo(cx, cy, cx - s.size, cy)
                            quadraticTo(cx, cy, cx, cy - s.size)
                            close()
                        }
                        drawPath(
                            path = path,
                            color = s.color.copy(alpha = alpha)
                        )
                        
                        // Draw soft halo for a magical flare effect
                        if (s.size > 8f) {
                            drawCircle(
                                color = s.color.copy(alpha = alpha * 0.12f),
                                radius = s.size * 2.8f,
                                center = Offset(cx, cy)
                            )
                        }
                    }
                }

                // Interactive Celebratory Level Up Card (Slightly scales and fades)
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(20.dp)
                        .scale(scale.value)
                        .shadow(32.dp, RoundedCornerShape(28.dp))
                        .testTag("level_up_dialog_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
                    ),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFB2A0), // Pastel Coral
                                Color(0xFFA5E6D8), // Pastel Mint
                                Color(0xFFFFF6B8)  // Pastel Gold
                            )
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(26.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val titleColorCycle by infiniteTransition.animateColor(
                            initialValue = Color(0xFFFFB2A0),
                            targetValue = Color(0xFFFFF6B8),
                            animationSpec = infiniteRepeatable(
                                animation = tween(1400, easing = EaseInOutSine),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "titleColorCycle"
                        )

                        Text(
                            text = "🎉 LEVEL UP 🎉",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = titleColorCycle,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Glowing level upgrade shield/badge
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFFFB2A0).copy(alpha = 0.15f),
                                            Color(0xFFA5E6D8).copy(alpha = 0.15f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = when (language) {
                                        AppLanguage.KO -> "레벨"
                                        AppLanguage.JA -> "レベル"
                                        else -> "LEVEL"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "$reachedLevel",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = when (language) {
                                AppLanguage.KO -> "낚시 숙련도 상승!"
                                AppLanguage.JA -> "釣り熟練度がアップしました！"
                                else -> "Mastery Level Gained!"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Lv.${reachedLevel - 1}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                            )
                            Text(
                                text = "➡️",
                                fontSize = 16.sp,
                                color = Color(0xFFA5E6D8) // Mint
                            )
                            Text(
                                text = "Lv.$reachedLevel",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFB2A0)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = when (language) {
                                    AppLanguage.KO -> "강물 아래 흐르는 물결을 느끼는 손끝의 감각이 더욱 깊어졌습니다. 더 깊은 차분함 속에서 희귀한 물고기가 당신을 찾아옵니다."
                                    AppLanguage.JA -> "川面の下を流れる波を感じる指先の感覚が、より一層深まりました。さらなる静寂の中で、めずらしい魚があなたを訪れます。"
                                    else -> "Your fishing technique shines more brightly. You can now attract quiet fish with greater calm and focus."
                                },
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                lineHeight = 19.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.dismissLevelUpDialog() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFA5E6D8) // Beautiful Mint green CTA
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(
                                text = when (language) {
                                    AppLanguage.KO -> "호흡 고르기"
                                    AppLanguage.JA -> "呼吸を整える"
                                    else -> "Deepen Breath"
                                },
                                color = Color.DarkGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
        
        // --- 8. MULTI-TARGET CELEBRATORY CELEBRATION WINDOW OVERLAY ---
        CelebrationOverlay(viewModel = viewModel, language = language)

        if (isTutorialOpen) {
            FishingTutorialDialog(
                language = language,
                onDismiss = { isTutorialOpen = false }
            )
        }
    }
}

/**
 * Composite function for the slide-up Fish Encyclopedia
 */
@Composable
fun EncyclopediaDrawerContent(
    speciesList: List<FishSpecies>,
    caughtList: List<CaughtFishEntity>,
    language: AppLanguage,
    onClose: () -> Unit,
    onSelectFish: (FishSpecies) -> Unit,
    onReset: () -> Unit
) {
    val isEnglish = language == AppLanguage.EN
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.75f)
            .testTag("encyclopedia_drawer"),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Drag handle and close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (language) {
                            AppLanguage.KO -> "강가의 생물 백과"
                            AppLanguage.JA -> "川の生物図鑑"
                            else -> "River Wildlife Encyclopedia"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    
                    val discoveredCount = speciesList.count { sp -> caughtList.any { it.speciesId == sp.id } }
                    val totalCount = speciesList.size
                    val ratio = if (totalCount > 0) discoveredCount.toFloat() / totalCount else 0f
                    val percentage = (ratio * 100).toInt()

                    Text(
                        text = (when (language) {
                            AppLanguage.KO -> "도감 발견 수: "
                            AppLanguage.JA -> "発見した種: "
                            else -> "Species Discovered: "
                        }) + "$discoveredCount / $totalCount",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(end = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(ratio.coerceIn(0.01f, 1f))
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFFA5E6D8), // Pastel Mint
                                                Color(0xFFE5C0F5)  // Pastel Violet
                                            )
                                        )
                                    )
                            )
                        }
                        Text(
                            text = when (language) {
                                AppLanguage.KO -> "수집률 $percentage%"
                                AppLanguage.JA -> "収集率 $percentage%"
                                else -> "Rate $percentage%"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onReset,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = if (isEnglish) "Release All (Reset)" else "방생하기(초기화)",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isEnglish) "Close" else "닫기",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            var searchQuery by remember { mutableStateOf("") }
            var selectedStatus by remember { mutableStateOf(0) } // 0: All, 1: Discovered, 2: Undiscovered
            var selectedRarity by remember { mutableStateOf("All") } // "All", "일반", "희귀", "전설", "신화"

            val filteredList = speciesList.filter { fish ->
                // 1. Search Query filter
                val matchesSearch = if (searchQuery.isBlank()) {
                    true
                } else {
                    val query = searchQuery.lowercase().trim()
                    fish.name.lowercase().contains(query) || fish.nameEn.lowercase().contains(query)
                }

                // 2. Status filter
                val isDiscovered = caughtList.any { it.speciesId == fish.id }
                val matchesStatus = when (selectedStatus) {
                    1 -> isDiscovered
                    2 -> !isDiscovered
                    else -> true
                }

                // 3. Rarity filter
                val matchesRarity = if (selectedRarity == "All") {
                    true
                } else {
                    fish.rarity == selectedRarity
                }

                matchesSearch && matchesStatus && matchesRarity
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Custom Glassmorphic Search Bar
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("encyclopedia_search_input")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    // Simple elegant Custom BasicTextField
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = when (language) {
                                        AppLanguage.KO -> "물고기 이름 검색..."
                                        AppLanguage.JA -> "魚の名前で検索..."
                                        else -> "Search fish name..."
                                    },
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                            innerTextField()
                        }
                    )

                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status Filter Row (All, Caught, Shadows)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    0 to when (language) {
                        AppLanguage.KO -> "전체 어종"
                        AppLanguage.JA -> "すべての種類"
                        else -> "All Species"
                    },
                    1 to when (language) {
                        AppLanguage.KO -> "발견함"
                        AppLanguage.JA -> "発見済み"
                        else -> "Caught"
                    },
                    2 to when (language) {
                        AppLanguage.KO -> "미발견"
                        AppLanguage.JA -> "未発見"
                        else -> "Shadows"
                    }
                ).forEach { (statusVal, label) ->
                    val isSelected = selectedStatus == statusVal
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                            .clickable { selectedStatus = statusVal }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rarity Filter Horizontal Scrollable Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val rarities = listOf(
                    "All" to when (language) {
                        AppLanguage.KO -> "전체 등급"
                        AppLanguage.JA -> "全レア度"
                        else -> "All Rarity"
                    },
                    "일반" to when (language) {
                        AppLanguage.KO -> "일반"
                        AppLanguage.JA -> "ノーマル"
                        else -> "Common"
                    },
                    "희귀" to when (language) {
                        AppLanguage.KO -> "희귀"
                        AppLanguage.JA -> "レア"
                        else -> "Rare"
                    },
                    "전설" to when (language) {
                        AppLanguage.KO -> "전설"
                        AppLanguage.JA -> "伝説"
                        else -> "Legendary"
                    },
                    "신화" to when (language) {
                        AppLanguage.KO -> "신화"
                        AppLanguage.JA -> "神話"
                        else -> "Mythic"
                    }
                )
                rarities.forEach { (rarityVal, label) ->
                    item {
                        val isSelected = selectedRarity == rarityVal
                        val chipColor = when (rarityVal) {
                            "일반" -> Color(0xFFA5E6D8) // Pastel Mint
                            "희귀" -> Color(0xFFA6C5E3) // Pastel Blue
                            "전설" -> Color(0xFFE5C0F5) // Pastel Violet
                            "신화" -> Color(0xFFFFB2A0) // Pastel Coral
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) chipColor
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .clickable { selectedRarity = rarityVal }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) {
                                    if (rarityVal == "All") MaterialTheme.colorScheme.onSecondaryContainer else Color.DarkGray
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // If empty, show a pleasant healing placeholder state
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🍃", fontSize = 32.sp)
                        Text(
                            text = if (isEnglish) "No matching biological traces found." else "부합하는 생태학적 흔적이 발견되지 않았습니다.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Scrollable Grid of filtered species
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList) { fish ->
                    val catches = caughtList.filter { it.speciesId == fish.id }
                    val isDiscovered = catches.isNotEmpty()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectFish(fish) }
                            .testTag("fish_grid_item_${fish.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDiscovered) fish.color.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(18.dp),
                        border = if (isDiscovered) BorderStroke(1.5.dp, fish.color) else null
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (isDiscovered) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(Color.White.copy(alpha = 0.4f), CircleShape)
                                // Restored code to avoid nested modifier errors or missing properties
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawLowPolyCapturedFish(fish, 0L)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = fish.localizedName(language),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = when (language) {
                                        AppLanguage.KO -> "최대 ${String.format("%.1f", catches.maxOfOrNull { it.length } ?: 0f)}cm"
                                        AppLanguage.JA -> "最大 ${String.format("%.1f", catches.maxOfOrNull { it.length } ?: 0f)}cm"
                                        else -> "Max ${String.format("%.1f", catches.maxOfOrNull { it.length } ?: 0f)}cm"
                                    },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(Color.Black.copy(alpha = 0.08f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "❓", fontSize = 24.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = when (language) {
                                        AppLanguage.KO -> "미발견 어종"
                                        AppLanguage.JA -> "未発見の魚"
                                        else -> "Undiscovered"
                                    },
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = (when (language) {
                                        AppLanguage.KO -> "희귀도: "
                                        AppLanguage.JA -> "レア度: "
                                        else -> "Rarity: "
                                    }) + fish.localizedRarity(language),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}

// Draw Canvas elements
private fun DrawScope.drawNightSky(stars: List<Offset>, ticks: Long, alphaMultiplier: Float) {
    for (idx in stars.indices) {
        val star = stars[idx]
        // Twinling oscillation
        val twinkleSpeed = 0.0016f + (idx % 4) * 0.0004f
        val phase = ticks * twinkleSpeed
        val starAlpha = (0.35f + 0.6f * (0.5f + 0.5f * sin(phase))) * alphaMultiplier
        
        drawCircle(
            color = Color.White.copy(alpha = starAlpha),
            radius = if (idx % 5 == 0) 2.5f else 1.5f,
            center = Offset(star.x * size.width, star.y * size.height)
        )
    }
}

private fun DrawScope.drawPastelClouds(ticks: Long, alphaMultiplier: Float) {
    val cloudSpeedX = (ticks * 0.015f) % size.width
    
    // Draw 3 layered geometric low-poly clouds
    val positions = listOf(
        Offset(200f, 150f),
        Offset(size.width - 300f, 220f),
        Offset(size.width / 2f, 100f)
    )

    for (pos in positions) {
        val smoothX = (pos.x + cloudSpeedX) % size.width
        drawCircle(
            color = Color.White.copy(alpha = 0.25f * alphaMultiplier),
            radius = 35f,
            center = Offset(smoothX, pos.y)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.25f * alphaMultiplier),
            radius = 50f,
            center = Offset(smoothX + 30f, pos.y + 10f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.25f * alphaMultiplier),
            radius = 35f,
            center = Offset(smoothX + 60f, pos.y)
        )
    }
}

private fun DrawScope.drawCelestialSource(
    daySunAlpha: Float,
    sunsetSunAlpha: Float,
    moonAlpha: Float,
    screenH: Float,
    skyTopColor: Color
) {
    // 1. Bright pastel sun (Day)
    if (daySunAlpha > 0.01f) {
        drawCircle(
            color = Color(0xFFFFF7DB).copy(alpha = 0.9f * daySunAlpha),
            radius = 65f,
            center = Offset(size.width * 0.8f, screenH * 0.18f)
        )
        drawCircle(
            color = Color(0xFFFFF7DB).copy(alpha = 0.20f * daySunAlpha),
            radius = 120f,
            center = Offset(size.width * 0.8f, screenH * 0.18f)
        )
    }

    // 2. Deep orange/red sunset sun sinks lower (Sunset)
    if (sunsetSunAlpha > 0.01f) {
        drawCircle(
            color = Color(0xFFFF9B6B).copy(alpha = 0.95f * sunsetSunAlpha),
            radius = 70f,
            center = Offset(size.width * 0.75f, screenH * 0.44f)
        )
        drawCircle(
            color = Color(0xFFFF6B6B).copy(alpha = 0.25f * sunsetSunAlpha),
            radius = 150f,
            center = Offset(size.width * 0.75f, screenH * 0.44f)
        )
    }

    // 3. Radiant crescent moon (Night)
    if (moonAlpha > 0.01f) {
        val moonCenter = Offset(size.width * 0.8f, screenH * 0.18f)
        drawCircle(
            color = Color(0xFFFFF6B8).copy(alpha = 0.9f * moonAlpha),
            radius = 40f,
            center = moonCenter
        )
        // Subtract overlapping offset circle to form crescent (blended with background skyTopColor!)
        drawCircle(
            color = skyTopColor,
            radius = 40f,
            center = Offset(moonCenter.x - 14f, moonCenter.y - 4f)
        )
        drawCircle(
            color = Color(0xFFFFF6B8).copy(alpha = 0.1f * moonAlpha),
            radius = 80f,
            center = moonCenter
        )
    }
}

private fun DrawScope.drawMountainRange(primaryPeakColor: Color, secondaryPeakColor: Color, w: Float, h: Float) {
    val pathFar = Path()
    pathFar.moveTo(0f, h * 0.55f)
    // Far Low-poly peak
    pathFar.lineTo(w * 0.25f, h * 0.35f)
    pathFar.lineTo(w * 0.45f, h * 0.55f)
    pathFar.lineTo(w * 0.72f, h * 0.28f)
    pathFar.lineTo(w, h * 0.55f)
    pathFar.lineTo(w, h)
    pathFar.lineTo(0f, h)
    pathFar.close()
    drawPath(pathFar, color = primaryPeakColor.copy(alpha = 0.6f))

    // Facet shading on mountains (low-poly light orientation)
    val pathFarFacet = Path()
    pathFarFacet.moveTo(w * 0.25f, h * 0.35f)
    pathFarFacet.lineTo(w * 0.45f, h * 0.55f)
    // Left side facet
    pathFarFacet.lineTo(w * 0.25f, h * 0.55f)
    pathFarFacet.close()
    drawPath(pathFarFacet, color = primaryPeakColor.copy(alpha = 0.8f))

    val pathNear = Path()
    pathNear.moveTo(0f, h * 0.55f)
    pathNear.lineTo(w * 0.48f, h * 0.39f)
    pathNear.lineTo(w * 0.88f, h * 0.55f)
    pathNear.lineTo(w, h * 0.55f)
    drawPath(pathNear, color = secondaryPeakColor)

    val pathNearFacet = Path()
    pathNearFacet.moveTo(w * 0.48f, h * 0.39f)
    pathNearFacet.lineTo(w * 0.88f, h * 0.55f)
    pathNearFacet.lineTo(w * 0.48f, h * 0.55f)
    pathNearFacet.close()
    drawPath(pathNearFacet, color = secondaryPeakColor.copy(alpha = 0.88f))
}

private fun DrawScope.drawWaterLowPoly(
    waterColors: List<Color>,
    yoonseulSparkles: List<YoonseulSparkle>,
    ticks: Long,
    fishingState: FishingState,
    bobberX: Float,
    bobberY: Float,
    weather: Weather,
    rainRipples: List<RainRipple>,
    tapRipples: List<TapWaterRipple>
) {
    val h = size.height
    val w = size.width

    // Water starts at 0.53 height
    val waterHorizon = h * 0.52f
    val waterHeight = h - waterHorizon

    // Draw peaceful water background with 3 layered organic low-poly segments
    drawRect(
        color = waterColors[0],
        topLeft = Offset(0f, waterHorizon),
        size = Size(w, waterHeight)
    )

    // Polygonal horizontal wave strips using Compose paths
    val rows = 5
    for (row in 1..rows) {
        val rowY = waterHorizon + (waterHeight / rows) * row
        val prevRowY = waterHorizon + (waterHeight / rows) * (row - 1)
        val path = Path()
        path.moveTo(0f, prevRowY)

        val segments = 8
        val widthStep = w / segments
        for (seg in 0..segments) {
            val currX = seg * widthStep
            // Sin waves with low poly jagged corners
            val waveAmplitude = 12f * (row * 0.5f)
            val waveProgress = (ticks * 0.0019f) + (seg * 0.5f)
            val currY = prevRowY + sin(waveProgress) * waveAmplitude
            path.lineTo(currX, currY)
        }
        path.lineTo(w, rowY)
        path.lineTo(0f, rowY)
        path.close()

        val mixPercent = row.toFloat() / rows
        val stripColor = Color(
            red = waterColors[0].red * (1f - mixPercent) + waterColors[1].red * mixPercent,
            green = waterColors[0].green * (1f - mixPercent) + waterColors[1].green * mixPercent,
            blue = waterColors[0].blue * (1f - mixPercent) + waterColors[1].blue * mixPercent,
            alpha = 1f
        )
        drawPath(path, color = stripColor)
    }

    // --- PROCEDURAL YOONSEUL SPARKLE DRAWING (윤슬 구현) ---
    for (idx in yoonseulSparkles.indices) {
        val ySparkle = yoonseulSparkles[idx]
        val spX = ySparkle.relX * w
        val spY = ySparkle.relY * h

        // Sin wave shimmer oscillation
        val brightness = 0.25f + 0.75f * (0.5f + 0.5f * sin(ySparkle.currentPhase))
        val baseLen = 14f * ySparkle.scaleFactor * brightness
        
        // Horizontal diamond shape mimicking sun/moonlight sparkling paths
        val sparklePath = Path()
        sparklePath.moveTo(spX - baseLen, spY)
        sparklePath.lineTo(spX, spY - 3f)
        sparklePath.lineTo(spX + baseLen, spY)
        sparklePath.lineTo(spX, spY + 3f)
        sparklePath.close()

        val sparkleCol = when (waterColors[0]) {
            // Day: Gold glistening
            Color(0xFF90C2C8) -> Color(0xFFFFF4D4).copy(alpha = (if (weather == Weather.RAIN) 0.35f else if (weather == Weather.MIST) 0.5f else 0.75f) * brightness)
            // Sunset: Orange-amber shining
            Color(0xFFD4746A) -> Color(0xFFFFCCAA).copy(alpha = (if (weather == Weather.RAIN) 0.4f else if (weather == Weather.MIST) 0.55f else 0.8f) * brightness)
            // Night: Silver moonlit reflections
            else -> Color(0xFFD7EAFE).copy(alpha = (if (weather == Weather.RAIN) 0.4f else if (weather == Weather.MIST) 0.6f else 0.85f) * brightness)
        }

        drawPath(sparklePath, color = sparkleCol)
    }

    // --- BOBBER AND RIPPLES DRAW ---
    if (fishingState != FishingState.IDLE && fishingState != FishingState.CASTING) {
        val bobberExactX = bobberX * w
        val bobberExactY = bobberY * h

        // Animated dip offset for waiting bob/nibble/bite
        val bobValue = (ticks * 0.0035f).toDouble()
        val bobOffset = when (fishingState) {
            FishingState.WAITING -> sin(bobValue).toFloat() * 5f
            FishingState.NIBBLE -> sin(bobValue * 3).toFloat() * 10f
            FishingState.BITE -> 16f // Plunged underwater
            else -> 0f
        }

        // Concentric geometric low-poly ripples (wider expansion & higher frequency under rain)
        val ripplePhase = (ticks * (if (weather == Weather.RAIN) 0.0022f else 0.0015f)) % 1.0f
        val rippleColor = Color.White.copy(alpha = 0.6f * (1f - ripplePhase))
        drawLowPolyRipple(
            center = Offset(bobberExactX, bobberExactY),
            radius = 16f + ripplePhase * (if (weather == Weather.RAIN) 75f else 60f),
            color = rippleColor,
            strokeWidth = 2.5f,
            segments = 8
        )

        if (fishingState == FishingState.NIBBLE || fishingState == FishingState.BITE) {
            val microRipple = ((ticks * 0.0035f) % 1.0f)
            drawLowPolyRipple(
                center = Offset(bobberExactX, bobberExactY),
                radius = 8f + microRipple * 30f,
                color = Color.White.copy(alpha = 0.8f * (1f - microRipple)),
                strokeWidth = 3.5f,
                segments = 6
            )
        }

        // Draw physical bobber (red top, white body low-poly cylinder)
        if (fishingState != FishingState.BITE) {
            val topR = Offset(bobberExactX, bobberExactY + bobOffset - 15f)
            val botR = Offset(bobberExactX, bobberExactY + bobOffset)
            
            // Draw floating red tip
            drawCircle(
                color = Color(0xFFFF3C3C),
                radius = 5.5f,
                center = topR
            )
            // Stem
            drawLine(
                color = Color.White,
                start = topR,
                end = botR,
                strokeWidth = 3f
            )
            // Body float
            drawOval(
                color = Color.White,
                topLeft = Offset(bobberExactX - 6f, bobberExactY + bobOffset - 8f),
                size = Size(12f, 10f)
            )
            drawOval(
                color = Color(0xFFFF3C3C),
                topLeft = Offset(bobberExactX - 6f, bobberExactY + bobOffset - 8f),
                size = Size(12f, 5f)
            )
        }
    }

    // --- DRAW DYNAMIC RAIN RIPPLES ---
    if (weather == Weather.RAIN) {
        for (rr in rainRipples) {
            if (rr.alpha > 0f) {
                drawLowPolyRipple(
                    center = Offset(rr.x * w, rr.y * h),
                    radius = rr.radius,
                    color = Color.White.copy(alpha = rr.alpha * 0.28f),
                    strokeWidth = 1.0f,
                    segments = 6 // Fast and clean 6-sided poly ripple
                )
            }
        }
    }

    // --- DRAW AMBIENT & USER INTERACTIVE TAP RIPPLES (워터 리플) ---
    for (rp in tapRipples) {
        if (rp.alpha > 0f) {
            val rippleColor = if (rp.isUserTap) {
                Color.White.copy(alpha = rp.alpha * 0.70f)
            } else {
                Color.White.copy(alpha = rp.alpha * 0.35f)
            }
            val strokeW = if (rp.isUserTap) 3.0f else 1.2f
            val segmentsNum = if (rp.isUserTap) 8 else 6
            drawLowPolyRipple(
                center = Offset(rp.x * w, rp.y * h),
                radius = rp.radius,
                color = rippleColor,
                strokeWidth = strokeW,
                segments = segmentsNum
            )
        }
    }
}

private fun DrawScope.drawWoodenBoatAndFisherman(
    time: TimeOfDay,
    w: Float,
    h: Float,
    ticks: Long,
    fishingState: FishingState,
    bobberX: Float,
    bobberY: Float,
    animatedBend: Float,
    animatedAngle: Float,
    animatedScale: Float
) {
    // Boat placement centered at 0.35 horizontal, sitting floating in the water (roughly 0.62 vertical)
    val boatBaseX = w * 0.36f
    val boatBaseY = h * 0.61f

    // Swaying bobbing math
    val bobTime = ticks * 0.0018f
    val boatSwayY = sin(bobTime).toFloat() * 4.5f
    val boatRollAngle = cos(bobTime).toFloat() * 1.5f // degrees

    withTransform({
        translate(left = boatBaseX, top = boatBaseY + boatSwayY)
        rotate(degrees = boatRollAngle, pivot = Offset.Zero)
    }) {
        // Shading shadows under boat
        drawOval(
            color = Color.Black.copy(alpha = 0.2f),
            topLeft = Offset(-80f, 15f),
            size = Size(160f, 22f)
        )

         // 1. Draw Wooden Canoe/Kayak in Poly Facets (Low-poly 3D look)
         val boatInside = Path().apply {
             moveTo(-70f, -2f)
             lineTo(-18f, 11f)
             lineTo(60f, -2f)
             lineTo(0f, -5f)
             close()
         }
         drawPath(boatInside, color = Color(0xFF382312)) // Dark empty inside cavity representing hollow canoe

         val boatHullLeft = Path().apply {
             moveTo(-75f, 0f)
             lineTo(-20f, 15f)
             lineTo(65f, 0f)
             lineTo(0f, -6f)
             close()
         }
         drawPath(boatHullLeft, color = Color(0xFFBE783A)) // Warm golden brown exterior hull wood

         val boatHullShadow = Path().apply {
             moveTo(-75f, 0f)
             lineTo(-20f, 15f)
             lineTo(0f, 4f)
             close()
         }
         drawPath(boatHullShadow, color = Color(0xFF8C5325)) // Darker shadowed lower facet

         val boatRim = Path().apply {
             moveTo(-75f, 0f)
             lineTo(65f, 0f)
             lineTo(55f, -3f)
             lineTo(-65f, -3f)
             close()
         }
         drawPath(boatRim, color = Color(0xFFE5B083)) // Elegant light yellow-brown wood rim

         // 2. Draw Fisherman sitting inside (Pants inside, white shirt, straw hat with black band)
         val pants = Path().apply {
             moveTo(-16f, 0f)
             lineTo(6f, 0f)
             lineTo(8f, -11f)
             lineTo(-18f, -11f)
             close()
         }
         drawPath(pants, color = Color(0xFF2E5B88)) // Blue denim shorts / leg base inside canoe

         val torso = Path().apply {
             moveTo(-18f, -11f)
             lineTo(8f, -11f)
             lineTo(2f, -36f)
             lineTo(-12f, -36f)
             close()
         }
         drawPath(torso, color = Color(0xFFFBFBFB)) // Beautiful crisp white shirt

         // Neck & Hair base
         drawCircle(color = Color(0xFF282828), radius = 8.5f, center = Offset(-5f, -36f)) // Dark hair
         drawCircle(color = Color(0xFFEBC19F), radius = 6.5f, center = Offset(-5f, -33f)) // Warm peach skin neck

         // Hands holding the rod
         drawLine(
             color = Color(0xFFFBFBFB), // white shirt sleeve
             start = Offset(-11f, -25f),
             end = Offset(-1f, -20f),
             strokeWidth = 6.5f,
             cap = StrokeCap.Round
         )
         drawLine(
             color = Color(0xFFEBC19F), // skin tone arm reaching to rod
             start = Offset(-1f, -20f),
             end = Offset(4f, -24f),
             strokeWidth = 5.2f,
             cap = StrokeCap.Round
         )

         // Straw Hat with black band & flat top (from reference PNG)
         val headCenter = Offset(-5f, -40f)
         
         // Brim
         drawOval(color = Color(0xFFE9CB99), topLeft = Offset(headCenter.x - 22f, headCenter.y + 1f), size = Size(44f, 9f))
         drawOval(color = Color(0xFFD6B57E), topLeft = Offset(headCenter.x - 18f, headCenter.y + 1.5f), size = Size(36f, 7f))

         // Black ribbon band
         drawRect(color = Color(0xFF1C1C1C), topLeft = Offset(headCenter.x - 11f, headCenter.y - 6f), size = Size(22f, 7f))

         // Crown (Flat top cap structure)
         drawRect(color = Color(0xFFE9CB99), topLeft = Offset(headCenter.x - 11f, headCenter.y - 17f), size = Size(22f, 11f))
         drawRect(color = Color(0xFFD6B57E), topLeft = Offset(headCenter.x, headCenter.y - 17f), size = Size(11f, 11f)) // right shaded crown half

         // 3. Curved Bamboo fishing rod
         // 2.5D Perspective math utilizing animated transform parameters
         val rodRoot = Offset(4f, -24f)
         val rodTipDefaultX = 45f
         val rodTipDefaultY = -55f

         val vecX = rodTipDefaultX - rodRoot.x
         val vecY = rodTipDefaultY - rodRoot.y

         val angleRad = (animatedAngle * Math.PI / 180.0).toFloat()
         val cosA = kotlin.math.cos(angleRad)
         val sinA = kotlin.math.sin(angleRad)

         // Rotate and scale to simulate 3D projection foreshortening (2.5D)
         val rotatedX = (vecX * cosA - vecY * sinA) * animatedScale
         val rotatedY = (vecX * sinA + vecY * cosA) * animatedScale

         // Curve bends applied dynamically on top
         val bendFactorX = animatedBend * 0.45f
         val bendFactorY = animatedBend * 0.95f

         val rodTip = Offset(
             rodRoot.x + rotatedX + bendFactorX,
             rodRoot.y + rotatedY + bendFactorY
         )

         // Draw segment nodes on bamboo rod
         drawLine(
             color = Color(0xFFBBB189), // Bamboo core
             start = rodRoot,
             end = rodTip,
             strokeWidth = 3.5f,
             cap = StrokeCap.Round
         )

         // Rod joints (gives low-poly detail)
         val steps = 4
         for (step in 1..steps) {
             val fraction = step.toFloat() / steps
             val JointX = rodRoot.x * (1f - fraction) + rodTip.x * fraction
             val JointY = rodRoot.y * (1f - fraction) + rodTip.y * fraction
             drawCircle(
                 color = Color(0xFF867E52),
                 radius = 2.5f,
                 center = Offset(JointX, JointY)
             )
         }

        // 4. Draw Fishing Line leading into the bobber (Bézier curves)
        if (fishingState != FishingState.IDLE) {
            val exactTipWorldX = boatBaseX + rodTip.x
            val exactTipWorldY = boatBaseY + boatSwayY + rodTip.y

            val targetBobberWorldX = bobberX * size.width
            val targetBobberWorldY = bobberY * size.height

            // Calculate Bézier curve parameter so line droops elegantly
            val controlPointY = (exactTipWorldY + targetBobberWorldY) * 0.5f + 40f // Sags down nicely
            val linePath = Path()
            // We transform coordinates into local draw scope space corresponding to the boat translation
            val localTipX = rodTip.x
            val localTipY = rodTip.y
            val localBobberX = targetBobberWorldX - boatBaseX
            val localBobberY = targetBobberWorldY - (boatBaseY + boatSwayY)

            linePath.moveTo(localTipX, localTipY)
            linePath.quadraticTo(
                (localTipX + localBobberX) * 0.5f,
                (localTipY + localBobberY) * 0.5f + 50f, // Sag offset
                localBobberX,
                localBobberY
            )

            drawPath(
                path = linePath,
                color = Color.White.copy(alpha = 0.55f),
                style = Stroke(width = 1.2f)
            )
        }
    }
}

/**
 * Procedurally render a stunning 3D-feeling rotating fish with facets
 */
private fun DrawScope.drawLowPolyCapturedFish(fish: FishSpecies, ticks: Long) {
    // Center of our drawing workspace inside the dialog circle
    val cx = size.width / 2f
    val cy = size.height / 2f

    // Horizontal wagging tail animation
    val wagSpeed = 0.007f
    val tailPhase = sin(ticks * wagSpeed).toFloat() * 12f

    withTransform({
        translate(cx, cy)
    }) {
        // Left side shadow
        drawOval(
            color = Color.Black.copy(alpha = 0.12f),
            topLeft = Offset(-45f, 35f),
            size = Size(90f, 15f)
        )

        // Draw body facet 1 (Upper back)
        val backPath = Path().apply {
            moveTo(-45f, 0f) // snout
            lineTo(-10f, -18f) // dorsal apex
            lineTo(30f, -5f) // dorsal decay
            lineTo(45f, -2f) // peduncle
            lineTo(0f, 2f) // lateral line spine
            close()
        }
        drawPath(backPath, color = fish.color.copy(alpha = 0.95f))

        // Draw body facet 2 (Belly shade)
        val bellyPath = Path().apply {
            moveTo(-45f, 0f)
            lineTo(-12f, 16f) // belly apex
            lineTo(28f, 6f)
            lineTo(45f, -2f)
            lineTo(0f, 2f)
            close()
        }
        // Tint darker for standard 3D key light simulation
        val darkRatio = 0.82f
        val darkerFishCol = Color(
            red = fish.color.red * darkRatio,
            green = fish.color.green * darkRatio,
            blue = fish.color.blue * darkRatio,
            alpha = 1f
        )
        drawPath(bellyPath, color = darkerFishCol)

        // Tail Fin Polygon (wags on spring ticks)
        val tailFin = Path().apply {
            moveTo(45f, -2f)
            lineTo(62f, -16f + tailPhase)
            lineTo(54f, -2f + tailPhase * 0.4f)
            lineTo(62f, 12f + tailPhase)
            close()
        }
        drawPath(tailFin, color = fish.color.copy(alpha = 0.75f))

        // Big shining eyes
        drawCircle(
            color = Color.White,
            radius = 3.5f,
            center = Offset(-30f, -3f)
        )
        drawCircle(
            color = Color.Black,
            radius = 1.8f,
            center = Offset(-31f, -3f)
        )

        // Pectoral Fin facet
        val pecFin = Path().apply {
            moveTo(-16f, 3f)
            lineTo(-6f, 12f + tailPhase * 0.3f)
            lineTo(-4f, 5f)
            close()
        }
        drawPath(pecFin, color = Color.White.copy(alpha = 0.6f))
    }
}

// Compact spring transitions helper
@Composable
private fun transitionScaleFloat(): State<Float> {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
    return scale.asState()
}

/**
 * Draws a stylized, low-poly jumping fish directly at the center of the local translation scope (0f, 0f).
 */
private fun DrawScope.drawLowPolyJumpingFish(fish: FishSpecies, ticks: Long) {
    // Horizontal wagging tail animation
    val wagSpeed = 0.012f
    val tailPhase = sin(ticks * wagSpeed).toFloat() * 16f

    // Draw body facet 1 (Upper back)
    val backPath = Path().apply {
        moveTo(-45f, 0f) // snout
        lineTo(-10f, -18f) // dorsal apex
        lineTo(30f, -5f) // dorsal decay
        lineTo(45f, -2f) // peduncle
        lineTo(0f, 2f) // lateral line spine
        close()
    }
    drawPath(backPath, color = fish.color.copy(alpha = 0.95f))

    // Draw body facet 2 (Belly shade)
    val bellyPath = Path().apply {
        moveTo(-45f, 0f)
        lineTo(-12f, 16f) // belly apex
        lineTo(28f, 6f)
        lineTo(45f, -2f)
        lineTo(0f, 2f)
        close()
    }
    val darkRatio = 0.82f
    val darkerFishCol = Color(
        red = fish.color.red * darkRatio,
        green = fish.color.green * darkRatio,
        blue = fish.color.blue * darkRatio,
        alpha = 1f
    )
    drawPath(bellyPath, color = darkerFishCol)

    // Tail Fin Polygon (wags on spring ticks)
    val tailFin = Path().apply {
        moveTo(45f, -2f)
        lineTo(62f, -16f + tailPhase)
        lineTo(54f, -2f + tailPhase * 0.4f)
        lineTo(62f, 12f + tailPhase)
        close()
    }
    drawPath(tailFin, color = fish.color.copy(alpha = 0.75f))

    // Big shining eyes
    drawCircle(
        color = Color.White,
        radius = 3.5f,
        center = Offset(-30f, -3f)
    )
    drawCircle(
        color = Color.Black,
        radius = 1.8f,
        center = Offset(-31f, -3f)
    )

    // Pectoral Fin facet
    val pecFin = Path().apply {
        moveTo(-16f, 3f)
        lineTo(-6f, 12f + tailPhase * 0.3f)
        lineTo(-4f, 5f)
        close()
    }
    drawPath(pecFin, color = Color.White.copy(alpha = 0.6f))
}

/**
 * Draws an isometric/perspective flattened polygonal ripple ring representing low-poly water waves.
 */
private fun DrawScope.drawLowPolyRipple(
    center: Offset,
    radius: Float,
    color: Color,
    strokeWidth: Float,
    segments: Int = 8
) {
    val path = Path()
    for (i in 0 until segments) {
        val angle = i * 2 * Math.PI / segments
        val x = center.x + radius * cos(angle).toFloat()
        // Perspective vertical flattening (0.4f ratio) for water surfaces
        val y = center.y + (radius * 0.4f) * sin(angle).toFloat()
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth)
    )
}

// Japanese Localization for Fish Species data entries
val FishSpecies.nameJa: String
    get() = when (id) {
        "gold_crucian" -> "黄金フナ"
        "rainbow_trout" -> "ニジマス"
        "sweetfish" -> "アユ"
        "mandarin_fish" -> "コウ라이케ツギョ" // コウライケツギョ
        "cherry_salmon" -> "ヤマメ"
        "catfish" -> "マナマズ"
        "shiri" -> "シュリ"
        "koi" -> "錦鯉"
        "gobs" -> "カムルチー"
        "moonlight_catfish" -> "月光ナマズ"
        "sunset_butterfly" -> "チョウチョウウオ"
        "star_whale" -> "星空クジラ"
        else -> nameEn
    }

val FishSpecies.rarityJa: String
    get() = when (rarityEn) {
        "Common", "일반" -> "一般"
        "Rare", "희귀" -> "レア"
        "Legendary", "전설" -> "伝説"
        "Mythic", "신화" -> "神話"
        else -> rarityEn
    }

val FishSpecies.optimalTimeJa: String
    get() = when (optimalTimeEn) {
        "Anytime" -> "いつでも"
        "Day" -> "昼"
        "Sunset" -> "夕方"
        "Night" -> "夜"
        else -> optimalTimeEn
    }

val FishSpecies.descriptionJa: String
    get() = when (id) {
        "gold_crucian" -> "もっとも一般的で親しみやすい淡水魚。温かみのある黄金色の鱗が特徴で、穏やかな川辺のどこでも出会えます。波に揺られて静かに佇む姿は、見る人の心を落ち着かせてくれます。"
        "rainbow_trout" -> "体の側面に鮮やかで美しい虹色の帯を持つ魚。澄んだ冷たい急流を泳ぎ抜く生命力に溢れ、見る人の目を引き付けます。"
        "sweetfish" -> "澄みきったきれいな渓流にひっそりと生息する銀色の小型魚。岩に付いた苔だけを食べて育ち、体から爽やかなスイカのような香りがする、自然のエッセンスを宿した魚です。"
        "mandarin_fish" -> "美しい虎柄の鱗を持つ、川の隠れた支配者。暗い岩の隙間に身を潜め、静かに水の流れに耳を澄ませる穏やかな習性を持っています。"
        "cherry_salmon" -> "緑豊かな森의冷たく澄んだ川に住み、背中に赤い落ち葉のような美しい模様を刻んだ美しい魚。出会うだけで爽やかな風を感じさせてくれます。"
        "catfish" -> "長くて丸い髭をのんびりと揺らしながら、川の底의砂にゆったりと身を預ける、静かな夜を愛する川の古い友人です。"
        "shiri" -> "川を輝きで飾る美しく可憐な魚。太陽の光を浴びると、透明なヒレの端にオレンジやパープル、スカイブルーなど七色の虹が控えめに砕け散ります。"
        "koi" -> "上品で気品あふれる純白のベースに、深い朱色の美しい模様を誇る魚。優雅な泳ぎは、見る人の心に大きな安らぎをもたらしてくれます。"
        "gobs" -> "強い生命力を持ちながらも、深い泥の底で長い静寂を守る存在。静かに内面をつむぐように、穏やかな水面下を静かに見守っています。"
        "moonlight_catfish" -> "夜の魂のように淡いラベンダーブルーの体。銀貨のような輝く模様を持つ神話的な存在。暗闇の中で満月の光を浴びながら静かに水面を泳ぎます。"
        "sunset_butterfly" -> "夕日が沈む短い瞬間、水面が赤い絨毯に変わる時、柳の木の下にふわりと浮かび、オレンジ色のヒレを羽のように羽ばたかせます。"
        "star_whale" -> "背中に生命を帯びた宇宙の星雲と冬の星座を美しく描き出した超自然的なクジラ。真夜中の湖面から一度だけ高々と跳び上がり、忘れられない満天の星の噴水を放ちます。"
        else -> descriptionEn
    }

fun FishSpecies.localizedName(lang: AppLanguage): String {
    return when (lang) {
        AppLanguage.JA -> nameJa
        AppLanguage.EN -> nameEn
        AppLanguage.KO -> name
    }
}

fun FishSpecies.localizedRarity(lang: AppLanguage): String {
    return when (lang) {
        AppLanguage.JA -> rarityJa
        AppLanguage.EN -> rarityEn
        AppLanguage.KO -> rarity
    }
}

fun FishSpecies.localizedOptimalTime(lang: AppLanguage): String {
    return when (lang) {
        AppLanguage.JA -> optimalTimeJa
        AppLanguage.EN -> optimalTimeEn
        AppLanguage.KO -> optimalTime
    }
}

fun FishSpecies.localizedDescription(lang: AppLanguage): String {
    return when (lang) {
        AppLanguage.JA -> descriptionJa
        AppLanguage.EN -> descriptionEn
        AppLanguage.KO -> description
    }
}

fun FishSpecies.localizedOptimalWeather(lang: AppLanguage): String {
    return when (id) {
        "gold_crucian", "shiri", "rainbow_trout", "koi" -> when (lang) {
            AppLanguage.KO -> "맑음 ☀️"
            AppLanguage.JA -> "晴れ ☀️"
            AppLanguage.EN -> "Sunny ☀️"
        }
        "mandarin_fish", "gobs", "cherry_salmon", "sunset_butterfly" -> when (lang) {
            AppLanguage.KO -> "안개 🌫️"
            AppLanguage.JA -> "霧・曇り 🌫️"
            AppLanguage.EN -> "Cloudy/Mist 🌫️"
        }
        "catfish", "sweetfish", "moonlight_catfish", "star_whale" -> when (lang) {
            AppLanguage.KO -> "비 🌧️"
            AppLanguage.JA -> "雨 🌧️"
            AppLanguage.EN -> "Rainy 🌧️"
        }
        else -> when (lang) {
            AppLanguage.KO -> "맑음 ☀️"
            AppLanguage.JA -> "晴れ ☀️"
            AppLanguage.EN -> "Sunny ☀️"
        }
    }
}

@Composable
fun AquariumDrawerContent(
    viewModel: FishingViewModel,
    caughtList: List<CaughtFishEntity>,
    language: AppLanguage,
    animationTicks: Long,
    onClose: () -> Unit,
    onSelectFish: (FishSpecies) -> Unit,
    onReset: () -> Unit
) {
    val isEnglish = language == AppLanguage.EN
    var selectedTab by remember { mutableStateOf(0) } // 0: Aquarium, 1: Collection Book

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .testTag("aquarium_drawer"),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (language) {
                            AppLanguage.KO -> "나의 힐링 수족관 & 수집 기록"
                            AppLanguage.JA -> "マイアクアリウム & 収集記録"
                            else -> "My Healing Aquarium & Log Book"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    
                    val totalCatches = caughtList.size
                    val uniqueSpCount = caughtList.map { it.speciesId }.distinct().size
                    Text(
                        text = when (language) {
                            AppLanguage.KO -> "총 포획 수: ${totalCatches}마리 (${uniqueSpCount}종)"
                            AppLanguage.JA -> "総収穫: ${totalCatches}匹 (${uniqueSpCount}種)"
                            else -> "Total Caught: $totalCatches fish ($uniqueSpCount species)"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Release button
                    IconButton(
                        onClick = onReset,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = if (isEnglish) "Release All (Reset)" else "방생하기(초기화)",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Close button
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isEnglish) "Close" else "닫기",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab bar: Aquarium vs Collection Book
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf(
                    when (language) {
                        AppLanguage.KO -> "🐠 미니 수족관"
                        AppLanguage.JA -> "🐠 ミニ水族館"
                        else -> "🐠 Mini Aquarium"
                    },
                    when (language) {
                        AppLanguage.KO -> "📜 낚시 일지"
                        AppLanguage.JA -> "📜 釣り日誌"
                        else -> "📜 Fishing Journal"
                    }
                )

                tabs.forEachIndexed { index, label ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            )
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp)
                            .testTag("aquarium_tab_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Content
            if (selectedTab == 0) {
                // Aquarium Tab!
                AquariumVisualizer(
                    caughtList = caughtList,
                    language = language,
                    animationTicks = animationTicks,
                    onSelectFish = onSelectFish
                )
            } else {
                // Collection Book Tab!
                CollectionBookList(
                    viewModel = viewModel,
                    caughtList = caughtList,
                    language = language,
                    onSelectFish = onSelectFish
                )
            }
        }
    }
}

@Composable
fun AquariumVisualizer(
    caughtList: List<CaughtFishEntity>,
    language: AppLanguage,
    animationTicks: Long,
    onSelectFish: (FishSpecies) -> Unit
) {
    val isEnglish = language == AppLanguage.EN
    val uniqueCaughtSpecies = remember(caughtList) {
        caughtList.map { it.speciesId }.distinct().mapNotNull { FishSpecies.find(it) }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Banner
        if (caughtList.isNotEmpty()) {
            val heaviestEntity = caughtList.maxByOrNull { it.weight }
            val longestEntity = caughtList.maxByOrNull { it.length }
            val heaviestSpecies = heaviestEntity?.let { FishSpecies.find(it.speciesId) }
            val longestSpecies = longestEntity?.let { FishSpecies.find(it.speciesId) }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isEnglish) "🏆 Heaviest Caught" else "🏆 최대 무게 기록",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (heaviestEntity != null && heaviestSpecies != null) {
                            Text(
                                text = "${heaviestSpecies.localizedName(language)} (${String.format("%.2f", heaviestEntity.weight)}kg)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(text = "-", fontSize = 12.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isEnglish) "📏 Longest Caught" else "📏 최대 길이 기록",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (longestEntity != null && longestSpecies != null) {
                            Text(
                                text = "${longestSpecies.localizedName(language)} (${String.format("%.1f", longestEntity.length)}cm)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(text = "-", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Virtual Glass Tank Box
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFE0F7FA), // Soft crystal aquamarine
                            Color(0xFF80DEEA), // Soft pastel blue
                            Color(0xFF00ACC1).copy(alpha = 0.85f) // Ocean depths
                        )
                    )
                )
                .border(2.dp, Color(0xFFB2EBF2), RoundedCornerShape(24.dp))
        ) {
            val tankWidthDp = maxWidth

            // 1. Seaweed & Bubbles under-painting Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw bubbles
                for (i in 0..7) {
                    val bubbleSpeed = 0.04f + (i * 0.005f)
                    // Float up using animationTicks
                    val rawY = h - ((animationTicks * bubbleSpeed * 8f + i * 80f) % (h + 40f))
                    val bubbleY = if (rawY < -20f) h + 20f else rawY
                    val bubbleX = w * (0.08f + i * 0.12f + sin(animationTicks * 0.012f + i * 1.5f).toFloat() * 0.04f)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.45f),
                        radius = 4f + (i * 1.5f),
                        center = Offset(bubbleX, bubbleY),
                        style = Stroke(width = 1f)
                    )
                }

                // Draw Seaweeds
                val weedsCount = 5
                for (i in 0 until weedsCount) {
                    val weedX = w * (0.1f + i * 0.2f)
                    val weedHeight = h * (0.3f + (i % 3) * 0.1f)
                    val weedPath = Path()
                    weedPath.moveTo(weedX, h)

                    // Cubic curve with sine sway
                    val sway = sin(animationTicks * 0.002f + i * 1.2f).toFloat() * (20f + i * 5f)
                    weedPath.cubicTo(
                        weedX - 10f, h - weedHeight * 0.33f,
                        weedX + 10f + sway * 0.5f, h - weedHeight * 0.66f,
                        weedX + sway, h - weedHeight
                    )

                    drawPath(
                        path = weedPath,
                        color = Color(0xFF4DB6AC).copy(alpha = 0.65f),
                        style = Stroke(width = 8f + (i % 3).toFloat(), cap = StrokeCap.Round)
                    )
                }
            }

            // 2. Swimming fish layers (animated transparent boxes)
            if (uniqueCaughtSpecies.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "🫧🛶🐠", fontSize = 28.sp)
                        Text(
                            text = if (isEnglish) "Go catch fish to fill your aquarium!" else "물고기를 잡으면 여기에 나타나 헤엄칩니다!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                // Show up to 6 unique swimming fish species inside the glass tank
                val activeList = uniqueCaughtSpecies.take(6)
                activeList.forEachIndexed { index, fish ->
                    val swimSpeed = 0.7f + (index % 3) * 0.4f
                    val directionLToR = (index % 2 == 0)
                    val yOffsetDp = 20.dp + ((index * 30) % 110).dp + sin(animationTicks * 0.002f + index).toFloat().times(6).dp
                    
                    val cycleWidth = tankWidthDp.value + 160f
                    val rawProgress = (animationTicks * 0.05f * swimSpeed + index * 120f) % cycleWidth
                    val xOffsetDp = if (directionLToR) {
                        (-80f + rawProgress).dp
                    } else {
                        (tankWidthDp.value + 80f - rawProgress).dp
                    }

                    // Touch/clickable fish to show full detail popup
                    Box(
                        modifier = Modifier
                            .offset(x = xOffsetDp, y = yOffsetDp)
                            .size(80.dp, 50.dp)
                            .graphicsLayer {
                                scaleX = if (directionLToR) {
                                    scaleX // wait, just use 1f
                                    1f
                                } else {
                                    -1f
                                }
                            }
                            .clickable { onSelectFish(fish) }
                            .testTag("aquarium_fish_${fish.id}")
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawLowPolyCapturedFish(fish, animationTicks)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CollectionBookList(
    viewModel: FishingViewModel,
    caughtList: List<CaughtFishEntity>,
    language: AppLanguage,
    onSelectFish: (FishSpecies) -> Unit
) {
    val isEnglish = language == AppLanguage.EN
    val sortedCatches = remember(caughtList) {
        caughtList.sortedByDescending { it.caughtTime }
    }
    
    var currentSubTab by remember { mutableStateOf(0) } // 0: 백과도감, 1: 시간별 기록일지
    
    val colMilestone3Claimed by viewModel.colMilestone3Claimed.collectAsState()
    val colMilestone6Claimed by viewModel.colMilestone6Claimed.collectAsState()
    val colMilestone9Claimed by viewModel.colMilestone9Claimed.collectAsState()
    val colMilestone12Claimed by viewModel.colMilestone12Claimed.collectAsState()
    
    val uniqueCount = remember(caughtList) {
        caughtList.map { it.speciesId }.distinct().size
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // --- MULTIPLE TARGET COLLECTION GOALS ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
            ),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEnglish) "🎯 Collection Goals" else "🎯 특정 수집 목표 현황",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "$uniqueCount / 12 " + (if (isEnglish) "Species" else "종 발견"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                LinearProgressIndicator(
                    progress = (uniqueCount / 12f).coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        MilestoneItem(
                            title = if (isEnglish) "Novice" else "입문 소집가",
                            targetCount = 3,
                            currentCount = uniqueCount,
                            isClaimed = colMilestone3Claimed,
                            rewardsText = "🪙200\n🪱 x2",
                            onClaim = { viewModel.claimCollectionMilestone(3) },
                            isEnglish = isEnglish
                        )
                    }
                    item {
                        MilestoneItem(
                            title = if (isEnglish) "Scholar" else "노련한 학자",
                            targetCount = 6,
                            currentCount = uniqueCount,
                            isClaimed = colMilestone6Claimed,
                            rewardsText = "🪙500\n🦐 x2",
                            onClaim = { viewModel.claimCollectionMilestone(6) },
                            isEnglish = isEnglish
                        )
                    }
                    item {
                        MilestoneItem(
                            title = if (isEnglish) "Ecologist" else "생태 탐구자",
                            targetCount = 9,
                            currentCount = uniqueCount,
                            isClaimed = colMilestone9Claimed,
                            rewardsText = "🪙1k\n💫 x2",
                            onClaim = { viewModel.claimCollectionMilestone(9) },
                            isEnglish = isEnglish
                        )
                    }
                    item {
                        MilestoneItem(
                            title = if (isEnglish) "Master" else "대완성 마스터",
                            targetCount = 12,
                            currentCount = uniqueCount,
                            isClaimed = colMilestone12Claimed,
                            rewardsText = "🪙2.5k\n💫 x5",
                            onClaim = { viewModel.claimCollectionMilestone(12) },
                            isEnglish = isEnglish
                        )
                    }
                }
            }
        }

        // Beautiful Sub-tab Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                .padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val isTab0 = currentSubTab == 0
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isTab0) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                    .clickable { currentSubTab = 0 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isEnglish) "📖 All Species Book" else "📖 전체 어종 백과",
                    fontSize = 11.sp,
                    fontWeight = if (isTab0) FontWeight.Bold else FontWeight.Medium,
                    color = if (isTab0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            val isTab1 = currentSubTab == 1
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isTab1) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                    .clickable { currentSubTab = 1 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isEnglish) "Chrono Log" else "시간별 기록일지",
                    fontSize = 11.sp,
                    fontWeight = if (isTab1) FontWeight.Bold else FontWeight.Medium,
                    color = if (isTab1) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (currentSubTab == 0) {
            // ALL 12 SPECIES LIST
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(FishSpecies.list) { fish ->
                    val speciesCatches = caughtList.filter { it.speciesId == fish.id }
                    val isDiscovered = speciesCatches.isNotEmpty()
                    val totalCaughtCount = speciesCatches.size
                    
                    if (isDiscovered) {
                        val maxLen = speciesCatches.maxOf { it.length }
                        val maxWt = speciesCatches.maxOf { it.weight }
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectFish(fish) }
                                .testTag("species_book_item_${fish.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = fish.color.copy(alpha = 0.12f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, fish.color.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(Color.White.copy(alpha = 0.7f), CircleShape)
                                        .border(1.dp, fish.color, CircleShape)
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawLowPolyCapturedFish(fish, 0L)
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = fish.localizedName(language),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        val rarityColor = when (fish.rarity) {
                                            "일반" -> Color(0xFFA5E6D8)
                                            "희귀" -> Color(0xFFA6C5E3)
                                            "전설" -> Color(0xFFE5C0F5)
                                            "신화" -> Color(0xFFFFB2A0)
                                            else -> MaterialTheme.colorScheme.secondaryContainer
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(rarityColor)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = fish.localizedRarity(language),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.DarkGray
                                            )
                                        }
                                    }

                                    Text(
                                        text = (if (isEnglish) "Caught Count: " else "누적 포획 횟수: ") + "${totalCaughtCount}마리",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "📏 Max ${String.format("%.1f", maxLen)} cm",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = "⚖️ Max ${String.format("%.2f", maxWt)} kg",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                                
                                Text(text = "🔍", fontSize = 14.sp, modifier = Modifier.padding(end = 4.dp))
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectFish(fish) }
                                .testTag("species_book_item_locked_${fish.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(Color.Black.copy(alpha = 0.06f), CircleShape)
                                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "❓", fontSize = 18.sp, color = Color.Gray)
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "🔒 " + (if (isEnglish) "Undiscovered" else "미발견 어종"),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        )
                                        
                                        val rarityColor = when (fish.rarity) {
                                            "일반" -> Color(0xFFA5E6D8).copy(alpha = 0.5f)
                                            "희귀" -> Color(0xFFA6C5E3).copy(alpha = 0.5f)
                                            "전설" -> Color(0xFFE5C0F5).copy(alpha = 0.5f)
                                            "신화" -> Color(0xFFFFB2A0).copy(alpha = 0.5f)
                                            else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(rarityColor)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = fish.localizedRarity(language),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    Text(
                                        text = (if (isEnglish) "Optimal Time: " else "선호 시간대: ") + fish.localizedOptimalTime(language),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    
                                    val suggestedBait = when (fish.rarity) {
                                        "일반" -> if (isEnglish) "Basic Bait" else "기본 미끼"
                                        "희귀" -> if (isEnglish) "Earthworm Bait" else "지렁이 미끼"
                                        "전설" -> if (isEnglish) "Krill Shrimp Bait" else "크릴새우 미끼"
                                        else -> if (isEnglish) "Golden Bait" else "황금 미끼"
                                    }
                                    Text(
                                        text = (if (isEnglish) "Suggested Bait: " else "권장 미끼: ") + suggestedBait,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                                
                                Text(text = "💡Hint", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), modifier = Modifier.padding(end = 4.dp))
                            }
                        }
                    }
                }
            }
        } else {
            // CHRONOLOGICAL HISTORIC DIARY LOG
            if (sortedCatches.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "📜", fontSize = 32.sp)
                        Text(
                            text = if (isEnglish) "No historic biological traces logged yet." else "기록된 첫 생태 흔적이 아직 없습니다.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(sortedCatches) { item ->
                        val fish = remember(item.speciesId) { FishSpecies.find(item.speciesId) }
                        if (fish != null) {
                            val formattedTime = remember(item.caughtTime, language) {
                                val date = java.util.Date(item.caughtTime)
                                when (language) {
                                    AppLanguage.KO -> java.text.SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", java.util.Locale.KOREAN).format(date)
                                    AppLanguage.JA -> java.text.SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", java.util.Locale.JAPANESE).format(date)
                                    else -> java.text.SimpleDateFormat("MMM dd, yyyy - h:mm:ss a", java.util.Locale.ENGLISH).format(date)
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectFish(fish) }
                                    .testTag("collection_item_${item.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = fish.color.copy(alpha = 0.12f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, fish.color.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .background(Color.White.copy(alpha = 0.7f), CircleShape)
                                            .border(1.dp, fish.color, CircleShape)
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            drawLowPolyCapturedFish(fish, 0L)
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = fish.localizedName(language),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            val rarityColor = when (fish.rarity) {
                                                "일반" -> Color(0xFFA5E6D8)
                                                "희귀" -> Color(0xFFA6C5E3)
                                                "전설" -> Color(0xFFE5C0F5)
                                                "신화" -> Color(0xFFFFB2A0)
                                                else -> MaterialTheme.colorScheme.secondaryContainer
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(rarityColor)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = fish.localizedRarity(language),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.DarkGray
                                                )
                                            }
                                        }

                                        Text(
                                            text = formattedTime,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                        
                                        Spacer(modifier = Modifier.height(2.dp))

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "📏 ${String.format("%.1f", item.length)} cm",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                            )
                                            Text(
                                                text = "⚖️ ${String.format("%.2f", item.weight)} kg",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                            )
                                            
                                            val timeIcon = when (item.timeOfDay.lowercase()) {
                                                "day" -> "☀️"
                                                "sunset" -> "🌅"
                                                "night" -> "🌙"
                                                else -> "☀️"
                                            }
                                            Text(
                                                text = "$timeIcon ${item.timeOfDay.uppercase()}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MilestoneItem(
    title: String,
    targetCount: Int,
    currentCount: Int,
    isClaimed: Boolean,
    rewardsText: String,
    onClaim: () -> Unit,
    isEnglish: Boolean
) {
    val isAchieved = currentCount >= targetCount
    
    Card(
        modifier = Modifier
            .width(108.dp)
            .testTag("collection_milestone_$targetCount"),
        colors = CardDefaults.cardColors(
            containerColor = if (isClaimed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                             else if (isAchieved) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                             else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isClaimed) Color.Transparent
                    else if (isAchieved) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "$currentCount / $targetCount",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isAchieved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            
            Text(
                text = rewardsText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 11.sp,
                textAlign = TextAlign.Center
            )
            
            if (isClaimed) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Gray.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isEnglish) "Claimed" else "완료됨",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            } else if (isAchieved) {
                Button(
                    onClick = onClaim,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                ) {
                    Text(
                        text = if (isEnglish) "Claim" else "받기",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isEnglish) "Locked" else "잠김",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
fun UpgradeDrawerContent(
    coins: Int,
    rodLevel: Int,
    upgradeCost: Int,
    language: AppLanguage,
    onClose: () -> Unit,
    onUpgrade: () -> Unit
) {
    val isEnglish = language == AppLanguage.EN
    val canUpgrade = coins >= upgradeCost

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.70f)
            .testTag("upgrade_drawer"),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Drag handle / Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when (language) {
                            AppLanguage.KO -> "🎣 장비 강화소"
                            AppLanguage.JA -> "🎣 道具鍛冶場"
                            else -> "🎣 Gear Fortification"
                        },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = when (language) {
                            AppLanguage.KO -> "장비를 강화해 더 특별한 입질과 혜택을 받으세요."
                            AppLanguage.JA -> "道具を鍛えて、特別なチャンスを掴みましょう。"
                            else -> "Fortify your gear to reach legendary fish sizes."
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main stats & comparisons display card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Current gear level
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (language) {
                                AppLanguage.KO -> "현재 낚싯대"
                                AppLanguage.JA -> "現在の釣竿"
                                else -> "Current Gear"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = when (rodLevel) {
                                    1 -> if (isEnglish) "Bamboo Rod" else "대나무 낚싯대"
                                    2 -> if (isEnglish) "Birch Wood Rod" else "자작나무 낚싯대"
                                    3 -> if (isEnglish) "Oak Iron Rod" else "참나무 철제 낚싯대"
                                    4 -> if (isEnglish) "Carbon Composite" else "카본 합성 낚싯대"
                                    else -> if (isEnglish) "Mystic Prism Rod" else "신비의 무지개 낚싯대"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Lv.$rodLevel",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    // Divider
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)))

                    // List of bonuses
                    Text(
                        text = when (language) {
                            AppLanguage.KO -> "🎯 강화 완료 시 제공되는 영구 버프"
                            AppLanguage.JA -> "🎯 強化ボーナス"
                            else -> "🎯 Fortification Benefits"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    // Effect 1: Wait rate
                    val currentWaitPct = (rodLevel - 1) * 350
                    val nextWaitPct = rodLevel * 350
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isEnglish) "⏱️ Faster Nibbles" else "⏱️ 입질 반응 속도",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = if(rodLevel == 1) "0ms" else "-${currentWaitPct}ms", fontSize = 12.sp, color = Color.Gray)
                            Text(text = "➜", fontSize = 11.sp, color = Color.Gray)
                            Text(text = "-${nextWaitPct}ms", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        }
                    }

                    // Effect 2: Perfect window
                    val currentWindow = (rodLevel - 1) * 12
                    val nextWindow = rodLevel * 12
                    Row(
                         modifier = Modifier.fillMaxWidth(),
                         horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isEnglish) "🎯 Rhythm Perfect Width" else "🎯 리듬 판정 두께",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = if(rodLevel == 1) "Standard" else "+${currentWindow}%", fontSize = 12.sp, color = Color.Gray)
                            Text(text = "➜", fontSize = 11.sp, color = Color.Gray)
                            Text(text = "+${nextWindow}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        }
                    }

                    // Effect 3: Catch Gold passive
                    val currentGoldPct = (rodLevel - 1) * 15
                    val nextGoldPct = rodLevel * 15
                    Row(
                         modifier = Modifier.fillMaxWidth(),
                         horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isEnglish) "🪙 Catch Gold Bonus" else "🪙 물고기 포획 골드 보너스",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = if(rodLevel == 1) "+0%" else "+${currentGoldPct}%", fontSize = 12.sp, color = Color.Gray)
                            Text(text = "➜", fontSize = 11.sp, color = Color.Gray)
                            Text(text = "+${nextGoldPct}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Wallet gold balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEnglish) "Your Balance:" else "나의 골드:",
                    fontSize = 14.sp,
                )
                Text(
                    text = "🪙 $coins G",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD700)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Upgrade Button
            Button(
                onClick = onUpgrade,
                enabled = canUpgrade,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("upgrade_execute_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canUpgrade) MaterialTheme.colorScheme.primary else Color.LightGray
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (canUpgrade) (if (isEnglish) "Fortify Gear" else "장비 강화하기") 
                               else if (isEnglish) "Insufficient Funds" else "소지 골드 부족",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (canUpgrade) {
                        Text(
                            text = "($upgradeCost G)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Light,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuestDrawerContent(
    viewModel: FishingViewModel,
    onClose: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val isEnglish = language == AppLanguage.EN
    val isKo = language == AppLanguage.KO

    // Quests progresses
    val questCatchCount by viewModel.dailyQuestCatchCount.collectAsState()
    val questRareCount by viewModel.dailyQuestRareCount.collectAsState()
    val questGoldEarned by viewModel.dailyQuestGoldEarned.collectAsState()

    val questCatchClaimed by viewModel.dailyQuestCatchClaimed.collectAsState()
    val questRareClaimed by viewModel.dailyQuestRareClaimed.collectAsState()
    val questGoldClaimed by viewModel.dailyQuestGoldClaimed.collectAsState()

    // Achievements
    val totalFishCaught by viewModel.totalFishCaught.collectAsState()
    val totalFishSold by viewModel.totalFishSold.collectAsState()
    val coins by viewModel.coins.collectAsState()
    val rodLevel by viewModel.rodLevel.collectAsState()
    val hasCaughtLegendaryOrMythic by viewModel.hasCaughtLegendaryOrMythic.collectAsState()

    val achCatch10Claimed by viewModel.achCatch10Claimed.collectAsState()
    val achMythicClaimed by viewModel.achMythicClaimed.collectAsState()
    val achRod3Claimed by viewModel.achRod3Claimed.collectAsState()
    val achSell20Claimed by viewModel.achSell20Claimed.collectAsState()
    val achCoins2000Claimed by viewModel.achCoins2000Claimed.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Daily Quests, 1: Achievements

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable { onClose() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .clickable(enabled = false) {},
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "🏆", fontSize = 22.sp)
                        Text(
                            text = if (isKo) "퀘스트 & 누적 업적" else if (language == AppLanguage.JA) "クエストと業績" else "Quests & Achievements",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
                    ) {
                        Text(text = "✕", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Tab Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf(
                        if (isKo) "일일 퀘스트" else "Daily Quests",
                        if (isKo) "평생 업적" else "Achievements"
                    )
                    tabs.forEachIndexed { index, title ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (activeTab == index) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    activeTab = index
                                    viewModel.triggerSoundChime(350f + index * 100f, 0.08f)
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (activeTab == index) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable lists
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (activeTab == 0) {
                        // Daily Quests
                        item {
                            QuestItem(
                                title = if (isKo) "유유자적 물놀이" else "Easy Angle",
                                desc = if (isKo) "물고기 3마리 낚기" else "Catch 3 fish",
                                progress = questCatchCount,
                                maxProgress = 3,
                                rewardText = if (isKo) "🪙 50 + 🐛 지렁이 2" else "🪙 50 + 🐛 Worm 2",
                                isClaimed = questCatchClaimed,
                                onClaim = { viewModel.claimDailyQuest("catch") }
                            )
                        }
                        item {
                            QuestItem(
                                title = if (isKo) "희귀종 발견!" else "Rare Encounter",
                                desc = if (isKo) "희귀 등급 이상 물고기 1마리 낚기" else "Catch 1 rare or better fish",
                                progress = questRareCount,
                                maxProgress = 1,
                                rewardText = if (isKo) "🪙 80 + 🦐 새우 1" else "🪙 80 + 🦐 Shrimp 1",
                                isClaimed = questRareClaimed,
                                onClaim = { viewModel.claimDailyQuest("rare") }
                            )
                        }
                        item {
                            QuestItem(
                                title = if (isKo) "만선과 상업" else "Great Profit",
                                desc = if (isKo) "물고기를 판매하여 누적 120 G 획득하기" else "Earn 120 coins from selling fish today",
                                progress = questGoldEarned,
                                maxProgress = 120,
                                rewardText = if (isKo) "🪙 100 + 🌟 황금 떡밥 1" else "🪙 100 + 🌟 Golden Bait 1",
                                isClaimed = questGoldClaimed,
                                onClaim = { viewModel.claimDailyQuest("gold") }
                            )
                        }
                    } else {
                        // Permanent Achievements
                        item {
                            QuestItem(
                                title = if (isKo) "초보 강태공" else "Novice Angler",
                                desc = if (isKo) "누적 10마리 물고기 낚기" else "Catch total of 10 fish",
                                progress = totalFishCaught,
                                maxProgress = 10,
                                rewardText = "🪙 100",
                                isClaimed = achCatch10Claimed,
                                onClaim = { viewModel.claimAchievement("catch_10") }
                            )
                        }
                        item {
                            QuestItem(
                                title = if (isKo) "전설을 마주하다" else "Faced Legendaries",
                                desc = if (isKo) "전설 혹은 신화 등급의 무지개색 어종 잡기" else "Catch a Legend or Mythic fish",
                                progress = if (hasCaughtLegendaryOrMythic) 1 else 0,
                                maxProgress = 1,
                                rewardText = "🪙 250",
                                isClaimed = achMythicClaimed,
                                onClaim = { viewModel.claimAchievement("mythic") }
                            )
                        }
                        item {
                            QuestItem(
                                title = if (isKo) "장비의 가치" else "Finer Rods",
                                desc = if (isKo) "낚싯대 레벨 3 달성" else "Upgrade fishing rod to Lv. 3",
                                progress = rodLevel,
                                maxProgress = 3,
                                rewardText = "🪙 400",
                                isClaimed = achRod3Claimed,
                                onClaim = { viewModel.claimAchievement("rod_3") }
                            )
                        }
                        item {
                            QuestItem(
                                title = if (isKo) "도매상의 대부" else "Wholesaler Apprentice",
                                desc = if (isKo) "누적 20마리 물고기 판매" else "Sell a total of 20 fish",
                                progress = totalFishSold,
                                maxProgress = 20,
                                rewardText = "🪙 500",
                                isClaimed = achSell20Claimed,
                                onClaim = { viewModel.claimAchievement("sell_20") }
                            )
                        }
                        item {
                            QuestItem(
                                title = if (isKo) "낚시 재벌" else "Millionaire Dream",
                                desc = if (isKo) "보유 골드 2000 G 돌파하기" else "Hold over 2,000 gold coins",
                                progress = coins,
                                maxProgress = 2000,
                                rewardText = if (isKo) "🪙 1000 + 🎁 떡밥 세트" else "🪙 1000 + 🎁 Bait Expansion",
                                isClaimed = achCoins2000Claimed,
                                onClaim = { viewModel.claimAchievement("coins_2000") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuestItem(
    title: String,
    desc: String,
    progress: Int,
    maxProgress: Int,
    rewardText: String,
    isClaimed: Boolean,
    onClaim: () -> Unit
) {
    val isComplete = progress >= maxProgress

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isComplete && !isClaimed) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isComplete && !isClaimed) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE9C46A), shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(text = "COMPLETE", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Progress Bar
                val fraction = (progress.toFloat() / maxProgress).coerceIn(0f, 1f)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearProgressIndicator(
                        progress = fraction,
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp),
                        strokeCap = StrokeCap.Round,
                        color = if (isComplete) MaterialTheme.colorScheme.primary else Color(0xFFE76F51),
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )
                    Text(
                        text = "$progress/$maxProgress",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "🎁 $rewardText",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Claim Reward Button
            Button(
                onClick = onClaim,
                enabled = isComplete && !isClaimed,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isComplete && !isClaimed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = if (isClaimed) MaterialTheme.colorScheme.surface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.defaultMinSize(minWidth = 72.dp)
            ) {
                Text(
                    text = if (isClaimed) "완료" else "받기",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isClaimed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else if (isComplete) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SpotSelectDrawerContent(
    viewModel: FishingViewModel,
    onClose: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val isKo = language == AppLanguage.KO
    val isEnglish = language == AppLanguage.EN
    
    val currentSpot by viewModel.currentSpot.collectAsState()
    val checkLevel by viewModel.fishingLevel.collectAsState()
    
    val spots = FishingSpot.values().toList()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable { onClose() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.65f)
                .clickable(enabled = false) {},
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "🏞️", fontSize = 22.sp)
                        Text(
                            text = if (isKo) "낚시터 야외 변경" else if (language == AppLanguage.JA) "釣り場を変更" else "Select Fishing Spot",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
                    ) {
                        Text(text = "✕", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isKo) "낚시터를 변경하면 새로운 배경 일러스트와 전용 자연 순환 음향, 떡밥 최적 어항이 적용됩니다." 
                           else "Changing spot changes the landscape background artwork, nature ambient echoes, and species distributions.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = spots) { spot ->
                        val isLocked = checkLevel < spot.minLevel
                        val isSelected = currentSpot.id == spot.id

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isLocked) {
                                    viewModel.selectSpot(spot)
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Left Emoji Illustration
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(
                                            if (isLocked) Color.DarkGray else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = if (isLocked) "🔒" else spot.emoji, fontSize = 28.sp)
                                }

                                // Center Metadata
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = if (isKo) spot.nameKo else if (language == AppLanguage.JA) spot.nameJa else spot.nameEn,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (isLocked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isLocked) {
                                            Text(
                                                text = "Lv.${spot.minLevel}+",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Red
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isKo) spot.descriptionKo else if (language == AppLanguage.JA) spot.descriptionJa else spot.descriptionEn,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        color = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Right status dot
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GpgRankingDrawerContent(
    viewModel: FishingViewModel,
    onClose: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val isKo = language == AppLanguage.KO
    val isEnglish = language == AppLanguage.EN
    
    val isSignedIn by viewModel.isGpgSignedIn.collectAsState()
    val checkLevel by viewModel.fishingLevel.collectAsState()
    val totalCaught by viewModel.totalFishCaught.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable { onClose() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .clickable(enabled = false) {},
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "🎮", fontSize = 22.sp)
                        Text(
                            text = if (isKo) "Google Play 게임 랭킹" else "Google Play Games Ranking",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
                    ) {
                        Text(text = "✕", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sign in controller card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSignedIn) Color(0xFF1B4332) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "🛡️", fontSize = 18.sp)
                            Column {
                                Text(
                                    text = if (isSignedIn) "Play Games 연동 중" else "Play Games 비활성화됨",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSignedIn) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (isSignedIn) "실시간 자동 리더보드가 동기화됩니다." else "연동하여 업적과 다른 우수한 어부들의 랭킹을 겨뤄보세요.",
                                    fontSize = 10.sp,
                                    color = if (isSignedIn) Color.White.copy(alpha = 0.82f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Button(
                            onClick = { viewModel.toggleGpgSignIn() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSignedIn) Color(0xFFD8F3DC) else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isSignedIn) "로그아웃" else "연동하기",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSignedIn) Color(0xFF1B4332) else MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!isSignedIn) {
                    // Gray overlay placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "🔒", fontSize = 36.sp)
                            Text(
                                text = if (isKo) "순위표 연동이 필요합니다." else "Please link Google Play Games first.",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Simulated global leaderboard
                    Text(
                        text = if (isKo) "🏆 실시간 아시아 어부 랭킹 (레벨)" else "🏆 Asia Angler Leaderboard (Level)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val rankings = remember(checkLevel, totalCaught) {
                        listOf(
                            Triple("#1", "AeroAngler (Lv.12)", "2,400 Caught"),
                            Triple("#2", "SeoulGosu (Lv.9)", "1,250 Caught"),
                            Triple("#3", "낚시왕장만수 (Lv.7)", "430 Caught"),
                            Triple("#4", "You (Lv.$checkLevel)", "$totalCaught Caught"),
                            Triple("#5", "RiverBreeze (Lv.2)", "22 Caught"),
                            Triple("#6", "OceanWhisper (Lv.1)", "10 Caught")
                        ).sortedByDescending { 
                            // Determine numerical level from string parsing
                            val part = it.second.substringAfter("Lv.").substringBefore(")")
                            part.toIntOrNull() ?: 0
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(items = rankings) { rawIndex, item ->
                            val isPlayer = item.second.startsWith("You")
                            val normalizedRank = "#${rawIndex + 1}"
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isPlayer) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    width = if (isPlayer) 1.5.dp else 0.dp,
                                    color = if (isPlayer) MaterialTheme.colorScheme.primary else Color.Transparent
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            text = normalizedRank,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp,
                                            color = when (normalizedRank) {
                                                "#1" -> Color(0xFFD4AF37) // Gold
                                                "#2" -> Color(0xFFC0C0C0) // Silver
                                                "#3" -> Color(0xFFCD7F32) // Bronze
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                        Text(
                                            text = item.second,
                                            fontWeight = if (isPlayer) FontWeight.ExtraBold else FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = item.third,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FishShopDrawerContent(
    caughtList: List<CaughtFishEntity>,
    language: AppLanguage,
    onClose: () -> Unit,
    onSellFish: (CaughtFishEntity) -> Unit,
    onSellAll: () -> Unit,
    viewModel: FishingViewModel
) {
    val isEnglish = language == AppLanguage.EN
    val totalEstimatedValue = caughtList.sumOf { viewModel.getFishValue(it) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.75f)
            .testTag("fish_shop_drawer"),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Drag handle / Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (language) {
                            AppLanguage.KO -> "🏪 물고기 상점"
                            AppLanguage.JA -> "🏪 川の魚市場"
                            else -> "🏪 Nature Fish Market"
                        },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = when (language) {
                            AppLanguage.KO -> "원하는 물고기를 판매하여 골드를 장만해 장비를 강화해보세요."
                            AppLanguage.JA -> "捕獲した魚を売ってゴールドを得て、道具を強化しましょう。"
                            else -> "Sell your dynamic catches for gold to fund superior gear."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = if (isEnglish) "Close" else "닫기",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body Area (List of fish to sell)
            Box(modifier = Modifier.weight(1f)) {
                if (caughtList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "🎣", fontSize = 48.sp)
                            Text(
                                text = when (language) {
                                    AppLanguage.KO -> "상점에 팔 수 있는 물고기가 없습니다."
                                    AppLanguage.JA -> "まだ販売できる魚를 捕獲していません。"
                                    else -> "No fish available in your repository to trade."
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = when (language) {
                                    AppLanguage.KO -> "강가로 돌아가 먼저 물고기를 낚아보세요!"
                                    AppLanguage.JA -> "川辺に戻って、まず魚を釣り上げましょう！"
                                    else -> "Return to the rivers to hook some fresh catches!"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(caughtList) { item ->
                            val fish = remember(item.speciesId) { FishSpecies.find(item.speciesId) }
                            if (fish != null) {
                                val fishValue = viewModel.getFishValue(item)
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = fish.color.copy(alpha = 0.08f)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, fish.color.copy(alpha = 0.25f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Miniature fish visual
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .background(Color.White.copy(alpha = 0.7f), CircleShape)
                                                .border(1.dp, fish.color, CircleShape)
                                                .padding(4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Canvas(modifier = Modifier.fillMaxSize()) {
                                                drawLowPolyCapturedFish(fish, 0L)
                                            }
                                        }

                                        // Info Column
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = fish.localizedName(language),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = String.format("%.1f cm", item.length),
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                                Text(
                                                    text = String.format("%.2f kg", item.weight),
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                            }
                                        }

                                        // Worth Label
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "🪙",
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "$fishValue G",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFFD4AF37)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(2.dp))

                                        // Sell specific button
                                        Button(
                                            onClick = { onSellFish(item) },
                                            modifier = Modifier.height(36.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                            ),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                                        ) {
                                            Text(
                                                text = when (language) {
                                                    AppLanguage.KO -> "판매"
                                                    AppLanguage.JA -> "売却"
                                                    else -> "Sell"
                                                },
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer row - showing total balance
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEnglish) "Gold Balance:" else "나의 보유 골드:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "🪙 ${viewModel.coins.value} G",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD700)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer primary action - Sell All button
            Button(
                onClick = onSellAll,
                enabled = caughtList.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("sell_all_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (caughtList.isNotEmpty()) Color(0xFF4CAF50) else Color.LightGray
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.KO -> "전체 물고기 일괄 판매"
                            AppLanguage.JA -> "すべての魚をまとめて売却"
                            else -> "Sell All Captured Fish"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (caughtList.isNotEmpty()) {
                        Text(
                            text = "(🪙 +$totalEstimatedValue G)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Light,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CelebrationOverlay(
    viewModel: FishingViewModel,
    language: AppLanguage
) {
    val currentCelebration by viewModel.currentCelebration.collectAsState()
    if (currentCelebration != null) {
        val celebration = currentCelebration!!
        val isEnglish = language == AppLanguage.EN
        
        // Particles system state
        val sparkles = remember { mutableStateListOf<CelebrationSparkle>() }
        
        // Animation value for spring card entrance
        val scale = remember { androidx.compose.animation.core.Animatable(0.5f) }
        val alphaVal = remember { androidx.compose.animation.core.Animatable(0f) }
        
        // Breathing aura pulse
        val infiniteTransition = rememberInfiniteTransition(label = "pulse_celebration")
        val auraPulse by infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "aura"
        )
        
        val random = remember { java.util.Random() }

        LaunchedEffect(celebration.id) {
            // Initialize particles
            sparkles.clear()
            repeat(45) {
                sparkles.add(
                    CelebrationSparkle(
                        x = random.nextFloat(),
                        y = random.nextFloat() * 0.8f + 0.1f,
                        speedX = (random.nextFloat() - 0.5f) * 0.003f,
                        speedY = -0.002f - random.nextFloat() * 0.003f,
                        color = when (random.nextInt(4)) {
                            0 -> Color(0xFFA5E6D8) // Pastel Mint
                            1 -> Color(0xFFFFB2A0) // Pastel Coral
                            2 -> Color(0xFFFFF6B8) // Pastel Yellow
                            else -> Color(0xFFE5C0F5) // Pastel Violet
                        },
                        size = 6f + random.nextFloat() * 14f,
                        maxLife = 1.8f + random.nextFloat() * 2.5f,
                        life = random.nextFloat() * 1.5f
                    )
                )
            }
            
            // Elegantly scale card inside
            launch {
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            launch {
                alphaVal.animateTo(1f, animationSpec = tween(500))
            }
            
            // Smooth particle update cycle
            while (isActive) {
                for (i in sparkles.indices.reversed()) {
                    val s = sparkles[i]
                    s.y += s.speedY
                    s.x += s.speedX
                    s.life -= 0.016f
                    if (s.life <= 0f || s.y < -0.05f) {
                        sparkles[i] = CelebrationSparkle(
                            x = random.nextFloat(),
                            y = 1.05f,
                            speedX = (random.nextFloat() - 0.5f) * 0.003f,
                            speedY = -0.002f - random.nextFloat() * 0.003f,
                            color = when (random.nextInt(4)) {
                                0 -> Color(0xFFA5E6D8)
                                1 -> Color(0xFFFFB2A0)
                                2 -> Color(0xFFFFF6B8)
                                else -> Color(0xFFE5C0F5)
                            },
                            size = 6f + random.nextFloat() * 14f,
                            maxLife = 2.0f + random.nextFloat() * 2f,
                            life = 2.0f + random.nextFloat() * 2f
                        )
                    }
                }
                delay(16)
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f * alphaVal.value))
                .pointerInput(celebration.id) {
                    detectTapGestures { viewModel.dismissCelebration() }
                }
                .zIndex(999f), // Draw above all UI modals
            contentAlignment = Alignment.Center
        ) {
            // Background Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerOffset = Offset(size.width / 2f, size.height / 2f)
                
                // 1. Soft pulsing radial halo aura
                val currentAuraRadius = 320.dp.toPx() * auraPulse
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE5C0F5).copy(alpha = 0.20f * alphaVal.value),
                            Color(0xFFFFF6B8).copy(alpha = 0.15f * alphaVal.value),
                            Color.Transparent
                        ),
                        center = centerOffset,
                        radius = currentAuraRadius
                    ),
                    radius = currentAuraRadius,
                    center = centerOffset
                )
                
                // 2. Spark particles
                sparkles.forEach { s ->
                    val alpha = (s.life / s.maxLife).coerceIn(0f, 1f) * alphaVal.value
                    val cx = s.x * size.width
                    val cy = s.y * size.height
                    
                    // 4-pointed star
                    val path = Path().apply {
                        moveTo(cx, cy - s.size)
                        quadraticTo(cx, cy, cx + s.size, cy)
                        quadraticTo(cx, cy, cx, cy + s.size)
                        quadraticTo(cx, cy, cx - s.size, cy)
                        quadraticTo(cx, cy, cx, cy - s.size)
                        close()
                    }
                    drawPath(
                        path = path,
                        color = s.color.copy(alpha = alpha)
                    )
                }
            }
            
            // Celebration Card Container
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(20.dp)
                    .scale(scale.value)
                    .shadow(40.dp, RoundedCornerShape(32.dp))
                    .testTag("celebration_card_${celebration.id}"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                ),
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE5C0F5), // Pastel Violet
                            Color(0xFFFFB2A0), // Pastel Coral
                            Color(0xFFA5E6D8)  // Pastel Mint
                        )
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val labelColorCycle by infiniteTransition.animateColor(
                        initialValue = Color(0xFFE5C0F5),
                        targetValue = Color(0xFFFFB2A0),
                        animationSpec = infiniteRepeatable(
                            animation = tween(1800, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "colorCycle"
                    )
                    
                    // Goal Category Tag
                    val categoryTag = when (celebration.type) {
                        "DAILY_QUEST" -> if (isEnglish) "DAILY QUEST COMPLETED" else "일일 미션 완료"
                        "COLLECTION" -> if (isEnglish) "COLLECTION GOAL ACHIEVED" else "특정 수집 목표 달성"
                        else -> if (isEnglish) "ACHIEVEMENT UNLOCKED" else "업적 달성"
                    }
                    
                    Text(
                        text = categoryTag,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = labelColorCycle,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier
                            .background(labelColorCycle.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Large Badge
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFFF6B8).copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = celebration.badgeEmoji,
                            fontSize = 62.sp,
                            modifier = Modifier.scale(scale.value)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Title
                    Text(
                        text = if (isEnglish) celebration.titleEn else celebration.titleKo,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Description
                    Text(
                        text = if (isEnglish) celebration.descriptionEn else celebration.descriptionKo,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Rewards Section Header (Horizontal custom line Box)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = if (isEnglish) "🎁 RECEIVED REWARDS" else "🎁 획득 보상",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Rewards List
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        celebration.rewards.forEach { reward ->
                            Row(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val rewardIcon = when (reward.type) {
                                    "coins" -> "🪙"
                                    "xp" -> "⭐"
                                    "bait_worm" -> "🪱"
                                    "bait_shrimp" -> "🦐"
                                    "bait_golden" -> "💫"
                                    else -> "🎁"
                                }
                                Text(text = rewardIcon, fontSize = 14.sp)
                                Text(
                                    text = "+${reward.count} " + (if (isEnglish) reward.nameEn.substringAfter(" ") else reward.nameKo.substringAfter(" ")),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Confirmation Action Button
                    Button(
                        onClick = { viewModel.dismissCelebration() },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(48.dp)
                            .testTag("celebration_confirm_button")
                    ) {
                        Text(
                            text = if (isEnglish) "Thank you" else "감사합니다",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FishingTutorialDialog(
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val isEnglish = language == AppLanguage.EN
    var currentSlide by remember { mutableStateOf(0) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(enabled = false) {}
            .zIndex(150f),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .padding(16.dp)
                .testTag("fishing_tutorial_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.KO -> "🎣 힐링 낚시 가이드 (Tutorial)"
                            AppLanguage.JA -> "🎣 癒しの釣りガイド"
                            else -> "🎣 Healing Fishing Guide"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Animated slide contents
                AnimatedContent(
                    targetState = currentSlide,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { width -> width } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> -width } + fadeOut()
                        } else {
                            slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> width } + fadeOut()
                        }.using(SizeTransform(clip = false))
                    },
                    label = "tutorial_slide"
                ) { slide ->
                    when (slide) {
                        0 -> {
                            TutorialSlideContent(
                                emoji = "🌊",
                                title = when (language) {
                                    AppLanguage.KO -> "1. 강물에 캐스팅하기"
                                    AppLanguage.JA -> "1. 川へのキャスティング"
                                    else -> "1. Casting Your Line"
                                },
                                desc = when (language) {
                                    AppLanguage.KO -> "잔잔하게 흐르는 아름다운 강줄기 어디든 원하는 곳을 가볍게 터치해 보세요. 그곳을 향해 찌가 날아가 착수 소리를 내며 조용히 가라앉습니다. 맑은 물결의 출렁임과 함께 물고기가 은신처로부터 다가올 때까지 평화롭게 귀를 기울여 줍니다."
                                    AppLanguage.JA -> "穏やかに流れる美しい川の任意の場所を軽くタップしてみてください。そこへ向かってウキが飛び、着水音を立てて静かに沈みます。澄んだ波の揺らぎと共に、魚が隠れ家から近づいてくるのを穏やかに待ちましょう。"
                                    else -> "Gently tap anywhere on the flowing river to cast your fishing line. The bobber flies and splashes softly into the water. Turn up your sound to enjoy the peaceful ripples while waiting for a hungry fish to bite."
                                }
                            )
                        }
                        1 -> {
                            TutorialSlideContent(
                                emoji = "🟢",
                                title = when (language) {
                                    AppLanguage.KO -> "2. 찌맞춤과 릴링 컨트롤"
                                    AppLanguage.JA -> "2. アタリとリールのコントロール"
                                    else -> "2. Hooking & Reel Tension"
                                },
                                desc = when (language) {
                                    AppLanguage.KO -> "찌 근처에 동그란 파동이 퍼지고 마침내 'SPLASH! 💦' 메시지와 진동이 발생하면, 화면 하단의 커다란 릴(Reel) 버튼을 빠르게 터치해 낚아채세요! 릴을 꾸욱 누르면 텐션 바가 오르고 놓으면 내립니다. 초록색 안전 영역 안에 바를 고정하면 물고기를 낚아 올릴 수 있습니다!"
                                    AppLanguage.JA -> "ウキの周りに波紋が広がり、ついに「SPLASH! 💦」が画面に現れた瞬間、画面下部のリール（Reel）ボタンを素早く押して釣り上げましょう! 押し続けるとテンションが上がり、離すと下がります。安全な緑色のゾーンに維持してください!"
                                    else -> "When ripples expand and a dramatic 'SPLASH! 💦' bubble appears, react quickly! Press and hold the big Reel button to increase tension, and release it to drop tension. Keep the bar inside the green safe zone to fully reel the fish in!"
                                }
                            )
                        }
                        2 -> {
                            TutorialSlideContent(
                                emoji = "🪱",
                                title = when (language) {
                                    AppLanguage.KO -> "3. 시간대와 다양한 특수 미끼"
                                    AppLanguage.JA -> "3. 時間帯と特別な餌"
                                    else -> "3. Spawn times & Premium Bait"
                                },
                                desc = when (language) {
                                    AppLanguage.KO -> "상점이나 일일 퀘스트로 얻은 지렁이(Worm), 크릴새우(Shrimp), 황금 미끼(Golden)를 장착하면 일반 미끼보다 수십 배 희귀한 전설과 신화급 어종이 유혹됩니다! 또한 낮, 노을, 밤 등 게임 속 시간대의 생태 흐름에 맞추면 도감 속 비밀 어종을 노릴 수 있습니다."
                                    AppLanguage.JA -> "ショップやクエストで獲得したミミズ(Worm)、クリル(Shrimp)、黄金の餌(Golden)ကို 장비하면 伝説야 신화의 レア한 魚가 낚이기 쉬워집니다! 昼、夕暮れ、夜などの時間帯によっても異なる神秘的なお魚が出現します。"
                                    else -> "Equip special baits—Worm (🪱), Shrimp (🦐), or Golden Bait (💫)—purchased from the Shop or earned from Quests to attract rare, legendary, and mythic fish! Different species awake during daytime, sunsets, or under the moonlit starry nights."
                                }
                            )
                        }
                        else -> {
                            TutorialSlideContent(
                                emoji = "🏆",
                                title = when (language) {
                                    AppLanguage.KO -> "4. 미니 수족관과 장비 업그레이드"
                                    AppLanguage.JA -> "4. ミニ水族館と釣り竿の強化"
                                    else -> "4. Aquarium & Professional Rods"
                                },
                                desc = when (language) {
                                    AppLanguage.KO -> "낚은 희귀 어종들은 🐠 미니 수족관에 채워 넣어 그들의 우아한 지느러미들의 춤을 감상하세요. 나머지 어종들은 백과도감에서 '방생(Release)'하여 대량의 코인과 경험치를 벌어보세요. 벌어들인 돈으로 더 강력하고 세련된 낚싯대를 조율할 수 있습니다!"
                                    AppLanguage.JA -> "釣り上げた高貴なお魚は 🐠 ミニ水族館で優雅に泳がせることができます。余ったお魚は放流(Release)して、大量のコインや経験値を集めましょう! 集めたコインで、さらに感度が高く美しいデザイン의 釣り竿に強化できます。"
                                    else -> "Place majestic sea creatures inside your 🐠 Mini Aquarium to watch them glide. Release other catches from the Journal to earn gold coins and stats/XP, allowing you to synthesize and buy super responsive carbon fishing rods!"
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Indicators and Control Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Button
                    TextButton(
                        onClick = { if (currentSlide > 0) currentSlide-- },
                        enabled = currentSlide > 0
                    ) {
                        Text(
                            text = if (isEnglish) "Prev" else "이전",
                            fontWeight = FontWeight.Bold,
                            color = if (currentSlide > 0) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f)
                        )
                    }
                    
                    // Slide Indicators
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(4) { idx ->
                            val isSelected = currentSlide == idx
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 10.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                    )
                            )
                        }
                    }
                    
                    // Next / Close Button
                    Button(
                        onClick = {
                            if (currentSlide < 3) {
                                currentSlide++
                            } else {
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = if (currentSlide < 3) {
                                if (isEnglish) "Next" else "다음"
                            } else {
                                if (isEnglish) "Start Fishing!" else "낚시하러 가기!"
                            },
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TutorialSlideContent(
    emoji: String,
    title: String,
    desc: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Splendid Emoji Box
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 32.sp)
        }
        
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = desc,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

