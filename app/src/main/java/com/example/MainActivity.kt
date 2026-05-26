package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import kotlin.math.abs
import com.example.data.model.AttentionProfileEntity
import com.example.data.model.DistractionTimelineEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AttentionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    private val attentionViewModel: AttentionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_scaffold"),
                    containerColor = CyberBlack
                ) { innerPadding ->
                    MainScreenContent(
                        viewModel = attentionViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreenContent(
    viewModel: AttentionViewModel,
    modifier: Modifier = Modifier
) {
    val stability by viewModel.attentionStability.collectAsStateWithLifecycle()
    val drift by viewModel.curiosityDrift.collectAsStateWithLifecycle()
    val impulseRisk by viewModel.impulseRisk.collectAsStateWithLifecycle()
    val fragmentation by viewModel.mentalFragmentation.collectAsStateWithLifecycle()
    val secondsToCrash by viewModel.secondsToCrash.collectAsStateWithLifecycle()
    val predictedCrashTime by viewModel.predictedCrashTime.collectAsStateWithLifecycle()
    val isInterventionActive by viewModel.isInterventionActive.collectAsStateWithLifecycle()
    val interventionType by viewModel.activeInterventionType.collectAsStateWithLifecycle()

    val typingVariance by viewModel.typingVariance.collectAsStateWithLifecycle()
    val scrollDeviation by viewModel.scrollSpeedDev.collectAsStateWithLifecycle()
    val reactionTime by viewModel.reactionTimeMs.collectAsStateWithLifecycle()
    val appSwitchHz by viewModel.appSwitchSpeedHz.collectAsStateWithLifecycle()
    val lastTrigger by viewModel.lastTelemetryTrigger.collectAsStateWithLifecycle()

    val timelineEvents by viewModel.allEvents.collectAsStateWithLifecycle()
    val profileStats by viewModel.profileFlow.collectAsStateWithLifecycle()

    val isNotificationActive by viewModel.isNotificationActive.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
    ) {
        // Core Dashboard View
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            item {
                HeaderComponent()
            }

            // Real-time Neural Stability Ring
            item {
                AttentionStabilityDeck(
                    stability = stability,
                    predictedCrashTime = predictedCrashTime,
                    secondsToCrash = secondsToCrash,
                    onCrashClick = { viewModel.forceFocusCrash() }
                )
            }

            // Interactive Trigger Status Grid
            item {
                StatusMetricsGrid(
                    drift = drift,
                    impulseRisk = impulseRisk,
                    fragmentation = fragmentation,
                )
            }

            // Dynamic Sensor Active Log Output
            item {
                TelemetryOutputLog(
                    lastTrigger = lastTrigger,
                    typingVariance = typingVariance,
                    scrollDeviation = scrollDeviation,
                    reactionTime = reactionTime,
                    appSwitchHz = appSwitchHz
                )
            }

            // ASSESSMENT LABS (THE INTERACTIVE EXPERIENCES)
            item {
                SectionTitle(text = "Attention Assessment Labs", icon = Icons.Default.Science)
            }

            item {
                AssessmentLabsArena(
                    viewModel = viewModel,
                    isNotificationActive = isNotificationActive
                )
            }

            // Intervention Prefs selector
            item {
                InterventionPreferencesRow(
                    selectedMode = interventionType,
                    onModeSelected = { viewModel.setInterventionMode(it) }
                )
            }

            // ATTENTION TIMELINE EVENTS HISTORICAL LOG
            item {
                SectionTitle(text = "Subconscious Collapse Log", icon = Icons.Default.Timeline)
            }

            if (timelineEvents.isEmpty()) {
                item {
                    EmptyHistoryLog()
                }
            } else {
                items(timelineEvents.take(10)) { event ->
                    TimelineEventRow(event = event)
                }
            }

            // Stats Aggregator and Control Deck
            item {
                StatsAndControlDeck(
                    profile = profileStats ?: AttentionProfileEntity(),
                    onClearHistory = { viewModel.clearHistory() }
                )
            }
        }

        // Simulating floating distraction overlay (Notification Trap)
        AnimatedVisibility(
            visible = isNotificationActive,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 12.dp, end = 12.dp)
        ) {
            NotificationBaitCard(
                onClick = { viewModel.onNotificationClicked() }
            )
        }

        // Active Intervention Screen (Pattern Interrupt)
        AnimatedVisibility(
            visible = isInterventionActive,
            enter = fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.9f),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            FocusInterventionOverlay(
                interventionType = interventionType,
                onSuccess = { viewModel.onInterventionBeaten() },
                onBypass = { viewModel.onInterventionFailed() }
            )
        }
    }
}

@Composable
fun HeaderComponent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "SECOND BRAIN",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedSlateText,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
                Text(
                    text = "Lag Predictor",
                    style = MaterialTheme.typography.headlineLarge,
                    color = LightSlateText,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                )
            }
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(DarkGreySurface)
                    .border(1.dp, BrightCyanAccent.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "Neural Hub Icon",
                    tint = BrightCyanAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Studying subconscious micro-telemetry to predict and defeat attention collapse before it locks in.",
            style = MaterialTheme.typography.bodySmall,
            color = MutedSlateText
        )
    }
}

@Composable
fun AttentionStabilityDeck(
    stability: Int,
    predictedCrashTime: String,
    secondsToCrash: Int,
    onCrashClick: () -> Unit
) {
    val dynamicColor = when {
        stability > 75 -> SoftGreenStable
        stability > 45 -> ElectricAmberWarning
        else -> AlertRedCollapse
    }

    val animatedStability by animateFloatAsState(
        targetValue = stability.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "Stability"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stability_deck"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGreySurface),
        border = BorderStroke(1.dp, dynamicColor.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(170.dp)
            ) {
                // Background track
                Canvas(modifier = Modifier.size(150.dp)) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.04f),
                        style = Stroke(width = 12.dp.toPx())
                    )
                }

                // Dynamic Progress Arc
                Canvas(modifier = Modifier.size(150.dp)) {
                    drawArc(
                        color = dynamicColor,
                        startAngle = -90f,
                        sweepAngle = (animatedStability / 100f) * 360f,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${stability}%",
                        style = MaterialTheme.typography.displayMedium,
                        color = LightSlateText,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "STABILITY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedSlateText,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Prediction Alert Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberBlack)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (stability > 50) Icons.Default.Bolt else Icons.Default.Warning,
                        contentDescription = "Alert Symbol",
                        tint = dynamicColor,
                        modifier = Modifier
                            .size(20.dp)
                            .testTag("stability_status_icon")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (stability > 50) "Attention steady" else "Attention Collapse Imminent",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = LightSlateText
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (stability > 40) "Crash est: $predictedCrashTime" else "Collapse in ~${secondsToCrash}s",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (stability > 40) LightSlateText else AlertRedCollapse,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Crash Simulation bypass testing lever
            Button(
                onClick = onCrashClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AlertRedCollapse.copy(alpha = 0.15f),
                    contentColor = AlertRedCollapse
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("force_crash_btn"),
                border = BorderStroke(1.dp, AlertRedCollapse.copy(alpha = 0.3f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HourglassEmpty, contentDescription = "Test Crash", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Force Imminence Intervention Test",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StatusMetricsGrid(
    drift: Float,
    impulseRisk: String,
    fragmentation: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left Column: Curiosity Drift & Fragmentation Wave
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Curiosity Drift State Display
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkGreySurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Curiosity Drift",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedSlateText
                        )
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Trend up icon",
                            tint = ElectricAmberWarning,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = if (drift > 0.6f) "RISING SHARPLY" else "STABLE NOISE",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (drift > 0.6f) ElectricAmberWarning else SoftGreenStable
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { drift },
                        color = if (drift > 0.6f) ElectricAmberWarning else BrightCyanAccent,
                        trackColor = Color.White.copy(alpha = 0.05f),
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Fragmentation wave simulator component
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkGreySurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Fragmentation Profile",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedSlateText
                    )
                    Text(
                        text = "$fragmentation STATUS",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (fragmentation) {
                            "Low" -> SoftGreenStable
                            "Medium" -> ElectricAmberWarning
                            else -> AlertRedCollapse
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    // Sine Wave Draw demonstrating dispersion
                    FragmentationWaveAnimation(fragmentationLevel = fragmentation)
                }
            }
        }

        // Right Column: Impulse Risk
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkGreySurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .weight(1f)
                .height(176.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Impulse Grab Risk",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedSlateText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = impulseRisk.uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (impulseRisk) {
                            "Low" -> SoftGreenStable
                            "Moderate" -> ElectricAmberWarning
                            else -> AlertRedCollapse
                        }
                    )
                }

                // Interactive Radar Sweep Representation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberBlack),
                    contentAlignment = Alignment.Center
                ) {
                    RadarSweepAnimation(
                        color = when (impulseRisk) {
                            "Low" -> SoftGreenStable
                            "Moderate" -> ElectricAmberWarning
                            else -> AlertRedCollapse
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TelemetryOutputLog(
    lastTrigger: String,
    typingVariance: Long,
    scrollDeviation: Float,
    reactionTime: Long,
    appSwitchHz: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGreySurface),
        border = BorderStroke(1.dp, BrightCyanAccent.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "LIVE BEHAVIORAL TELEMETRY",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrightCyanAccent,
                    letterSpacing = 1.sp
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(SoftGreenStable)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Parameters output lines
            TelemetryParamLine(label = "Typing Hesitance Drift:", value = "${typingVariance}ms", unit = " (target < 100)")
            TelemetryParamLine(label = "Scrolling Fluctuation Div:", value = "${scrollDeviation.toInt()}px/s¹", unit = " (target < 80)")
            TelemetryParamLine(label = "Notification Response Lag:", value = "${reactionTime}ms", unit = " (target > 800)")
            TelemetryParamLine(label = "Simulated Switching Load:", value = "${String.format("%.2f", appSwitchHz)}Hz", unit = " (target < 0.40)")

            Spacer(modifier = Modifier.height(12.dp))

            // Trigger Reason
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberBlack)
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VolumeMute,
                        contentDescription = "Log icon",
                        tint = BrightCyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LOG: $lastTrigger",
                        style = MaterialTheme.typography.bodySmall,
                        color = LightSlateText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun TelemetryParamLine(label: String, value: String, unit: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MutedSlateText)
        Row {
            Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = LightSlateText, fontFamily = FontFamily.Monospace)
            Text(text = unit, style = MaterialTheme.typography.bodySmall, color = MutedSlateText, fontSize = 10.sp)
        }
    }
}

@Composable
fun AssessmentLabsArena(
    viewModel: AttentionViewModel,
    isNotificationActive: Boolean
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Typing Hesitation", "Doomscroll Loop", "Switch Speed", "Response Trap")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkGreySurface)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
    ) {
        // Scrollable Tab Row for Assessment labs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = BrightCyanAccent,
            edgePadding = 8.dp,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = BrightCyanAccent
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        Divider(color = Color.White.copy(alpha = 0.04f))

        // Tab Content Display Workspace
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(130.dp),
            contentAlignment = Alignment.Center
        ) {
            when (selectedTab) {
                0 -> TypingLabSection(onKeyPress = { viewModel.onInteractiveKeyPressed() })
                1 -> ScrollingLabSection(onScroll = { viewModel.onInteractiveScrolled(it) })
                2 -> SwitchingLabSection(onSwitchClick = { viewModel.simulateAppSwitch() })
                3 -> ResponseTrapSection(
                    isNotificationActive = isNotificationActive,
                    onTriggerPing = { viewModel.triggerSimulatedNotification() }
                )
            }
        }
    }
}

@Composable
fun TypingLabSection(onKeyPress: () -> Unit) {
    var textInput by remember { mutableStateOf("") }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Type neural passphrase to test hesitation rate:",
            style = MaterialTheme.typography.bodySmall,
            color = MutedSlateText,
            textAlign = TextAlign.Center
        )
        Text(
            text = "\"FOCUS DEFEATS DECAY\"",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = BrightCyanAccent,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        OutlinedTextField(
            value = textInput,
            onValueChange = {
                textInput = it
                onKeyPress()
            },
            placeholder = { Text("Begin typing passphrase...", fontSize = 12.sp, color = MutedSlateText) },
            textStyle = MaterialTheme.typography.bodySmall.copy(color = LightSlateText, fontFamily = FontFamily.Monospace),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrightCyanAccent,
                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                focusedContainerColor = CyberBlack,
                unfocusedContainerColor = CyberBlack
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("typing_field")
        )
    }
}

@Composable
fun ScrollingLabSection(onScroll: (Float) -> Unit) {
    val scrollState = rememberScrollState()

    // Annoying doomscroll bait items
    val clickBaits = listOf(
        "🔥 SENSATIONAL: This scientific tip will 3x your lifespan instantly!",
        "⚠️ BE CAREFUL: You are wasting 6 hours on your phone right now...",
        "💎 HIDDEN TRICK: Rich people do not want you to know this simple visual cheat!",
        "👀 CLICK NOW: 24 photos that will make you rethink your entire existence!",
        "🎮 RED ALERT: The addictive game taking over the internet in seconds!",
        "🚀 MIND BLOWN: How quantum mechanics cured sleep deficits globally!"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Doomscroll to feed algorithm (Fast / erratic swipes lower Stability):",
            style = MaterialTheme.typography.bodySmall,
            color = MutedSlateText,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CyberBlack)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onScroll(dragAmount.y)
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(8.dp)
            ) {
                clickBaits.forEachIndexed { index, bait ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkGreySurface)
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.TouchApp, contentDescription = "Scroll Icon", tint = ElectricAmberWarning, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = bait, style = MaterialTheme.typography.bodySmall, color = LightSlateText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
fun SwitchingLabSection(onSwitchClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Rapidly cycle screen apps. Rapid switching models micro-fragmentation.",
            style = MaterialTheme.typography.bodySmall,
            color = MutedSlateText,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = onSwitchClick,
            colors = ButtonDefaults.buttonColors(containerColor = BrightCyanAccent),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("app_switch_btn")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Switch", tint = CyberBlack, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("TAP TO INSTANT APP-SWITCH", style = MaterialTheme.typography.bodySmall, color = CyberBlack, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun ResponseTrapSection(
    isNotificationActive: Boolean,
    onTriggerPing: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Self-Trigger a focus-bait ping. Can you resist clicking it instantly?",
            style = MaterialTheme.typography.bodySmall,
            color = MutedSlateText,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = onTriggerPing,
            colors = ButtonDefaults.buttonColors(containerColor = ElectricAmberWarning.copy(alpha = 0.2f), contentColor = ElectricAmberWarning),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("trigger_bait_btn"),
            enabled = !isNotificationActive,
            border = BorderStroke(1.dp, ElectricAmberWarning.copy(alpha = 0.3f))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.NotificationsActive, contentDescription = "Ping Icon", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isNotificationActive) "ACTIVE INTERRUPT SENT..." else "TRIGGER DISTRACTION BAIT PING",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun InterventionPreferencesRow(
    selectedMode: String,
    onModeSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "SELECT PATTERN INTERRUPT MECHANICAL TYPE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = BrightCyanAccent,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val modes = listOf(
                Triple("tactile_shifter", "Breath Shifter", Icons.Default.HighlightOff),
                Triple("neural_mesh", "Trace Geometry", Icons.Default.Psychology),
                Triple("icon_scramble", "Value Calibrator", Icons.Default.Speed)
            )

            modes.forEach { (mode, title, icon) ->
                val active = selectedMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (active) BrightCyanAccent.copy(alpha = 0.12f) else DarkGreySurface)
                        .border(1.dp, if (active) BrightCyanAccent else Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                        .clickable { onModeSelected(mode) }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = icon, contentDescription = title, tint = if (active) BrightCyanAccent else MutedSlateText, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 9.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            color = if (active) LightSlateText else MutedSlateText,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineEventRow(event: DistractionTimelineEntity) {
    val dateStr = remember(event.timestamp) {
        val format = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        format.format(Date(event.timestamp))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkGreySurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        border = BorderStroke(1.dp, if (event.isInterventionSuccessful) BrightCyanAccent.copy(alpha = 0.05f) else AlertRedCollapse.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (event.isInterventionSuccessful) BrightCyanAccent else AlertRedCollapse)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Collapse Interrupted", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = LightSlateText)
                }
                Text(text = dateStr, style = MaterialTheme.typography.bodySmall, color = MutedSlateText, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(6.dp))

            // Details
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(text = "Trigger System Source", style = MaterialTheme.typography.labelSmall, color = MutedSlateText)
                    Text(text = event.triggerReason, style = MaterialTheme.typography.bodySmall, color = LightSlateText, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Defeat Speed", style = MaterialTheme.typography.labelSmall, color = MutedSlateText)
                    Text(text = "~${event.recoveryDurationSeconds}s elapsed", style = MaterialTheme.typography.bodySmall, color = LightSlateText, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Reason: ${event.distractionSource}",
                style = MaterialTheme.typography.bodySmall,
                color = if (event.isInterventionSuccessful) SoftGreenStable else AlertRedCollapse,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun EmptyHistoryLog() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkGreySurface),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Timeline, contentDescription = "History blank", tint = MutedSlateText, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Collapse timeline pristine. Feed simulated events to populate logs.", style = MaterialTheme.typography.bodySmall, color = MutedSlateText)
        }
    }
}

@Composable
fun StatsAndControlDeck(
    profile: AttentionProfileEntity,
    onClearHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkGreySurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "NEURAL SYMMETRY METRICS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = BrightCyanAccent,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatMetricBlock(label = "Collapse Defeats", value = "${profile.totalDefeated}")
                    StatMetricBlock(label = "Model Fights", value = "${profile.totalConfronted}")
                    StatMetricBlock(label = "Active Streak", value = "${profile.stabilityStreak}")
                    StatMetricBlock(label = "Max Beat Streak", value = "${profile.maxStreak}")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onClearHistory,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f), contentColor = MutedSlateText),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("clear_history_btn")
        ) {
            Text("Clear Session Stability History", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatMetricBlock(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = LightSlateText, fontFamily = FontFamily.Monospace)
        Text(text = label, style = MaterialTheme.typography.bodySmall, fontSize = 9.sp, color = MutedSlateText)
    }
}

@Composable
fun SectionTitle(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = text, tint = BrightCyanAccent, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = BrightCyanAccent,
            letterSpacing = 1.sp
        )
    }
}

// BITS OR FLOATING PINGS (THE BAIT NOTIFICATIONS)
@Composable
fun NotificationBaitCard(
    onClick: () -> Unit
) {
    val triggers = listOf(
        "🔥 Someone commented on your video: \"Bro, I can't believe you did...\"",
        "💬 WhatsApp: \"Can you come over right now? Urgent...\"",
        "👀 Instagram: \"@user liked your post. See what they said...\"",
        "⚡ Crypto-Trade: \"Your token crashed 94% in the last 4 seconds!\""
    )
    val randomText = remember { triggers.random() }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkGreySurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, AlertRedCollapse),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("bait_notification")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AlertRedCollapse.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = "Alert", tint = AlertRedCollapse, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "MICRO-INTERRUPTION DETECTED",
                        style = MaterialTheme.typography.labelSmall,
                        color = AlertRedCollapse,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = randomText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = LightSlateText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "TAP TO DEFEAT",
                style = MaterialTheme.typography.labelSmall,
                color = BrightCyanAccent,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp
            )
        }
    }
}

// INTERRUPT OVERLAY SYSTEM (THE INTERVENTIONS)
@Composable
fun FocusInterventionOverlay(
    interventionType: String,
    onSuccess: () -> Unit,
    onBypass: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack.copy(alpha = 0.95f))
            .clickable { /* Block taps */ }
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Aesthetic Grid lines background decoration
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridStep = 40.dp.toPx()
            val gridColor = Color.White.copy(alpha = 0.015f)
            for (x in 0..size.width.toInt() step gridStep.toInt()) {
                drawLine(gridColor, Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height))
            }
            for (y in 0..size.height.toInt() step gridStep.toInt()) {
                drawLine(gridColor, Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()))
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Neon Warning Header
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AlertRedCollapse.copy(alpha = 0.15f))
                    .border(1.dp, AlertRedCollapse, CircleShape)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = "Alarm Icon", tint = AlertRedCollapse, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PATTERN INTERRUPT ACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AlertRedCollapse,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Hacking sub-conscious muscle memory bypass system...",
                style = MaterialTheme.typography.bodySmall,
                color = MutedSlateText,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Specific Mechanical challenge board
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkGreySurface)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                when (interventionType) {
                    "tactile_shifter" -> TactileShifterChallenge(onSuccess = onSuccess)
                    "neural_mesh" -> NeuralMeshChallenge(onSuccess = onSuccess)
                    "icon_scramble" -> ValueCalibratorChallenge(onSuccess = onSuccess)
                    else -> TactileShifterChallenge(onSuccess = onSuccess)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Option to bypass (fail streak, but exits)
            TextButton(
                onClick = onBypass,
                colors = ButtonDefaults.textButtonColors(contentColor = AlertRedCollapse.copy(alpha = 0.6f))
            ) {
                Text(
                    text = "Bypass Intervention (Fail attention stability check)",
                    style = MaterialTheme.typography.bodySmall,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                )
            }
        }
    }
}

// CHALLENGE TYPE 1: Tactile circle holds
@Composable
fun TactileShifterChallenge(onSuccess: () -> Unit) {
    var progress by remember { mutableStateOf(0.0f) }
    var isPressing by remember { mutableStateOf(false) }

    LaunchedEffect(isPressing) {
        if (isPressing) {
            while (progress < 1.0f) {
                delay(40)
                progress += 0.015f
            }
            onSuccess()
        } else {
            while (progress > 0.0f) {
                delay(30)
                progress = (progress - 0.05f).coerceAtLeast(0.0f)
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "HOLD SHIFT BULB",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = LightSlateText
        )
        Text(
            text = "Hold for 3 slow rhythmic breaths until calibration bar lights cyan.",
            style = MaterialTheme.typography.bodySmall,
            color = MutedSlateText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            if (isPressing) BrightCyanAccent.copy(alpha = 0.4f) else ElectricAmberWarning.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
                .border(
                    2.dp,
                    if (isPressing) BrightCyanAccent else ElectricAmberWarning,
                    CircleShape
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { isPressing = true },
                        onDragEnd = { isPressing = false },
                        onDragCancel = { isPressing = false },
                        onDrag = { _, _ -> }
                    )
                }
                .testTag("tactile_grip_node"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.HighlightOff,
                contentDescription = "Hold target",
                tint = if (isPressing) BrightCyanAccent else ElectricAmberWarning,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        LinearProgressIndicator(
            progress = { progress },
            color = BrightCyanAccent,
            trackColor = Color.White.copy(alpha = 0.04f),
            strokeCap = StrokeCap.Round,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}

// CHALLENGE TYPE 2: Geometry Node tracer
@Composable
fun NeuralMeshChallenge(onSuccess: () -> Unit) {
    val nodes = listOf(
        Offset(50f, 50f),
        Offset(250f, 50f),
        Offset(150f, 150f),
        Offset(250f, 250f),
        Offset(50f, 250f)
    )

    var currentTargetIndex by remember { mutableStateOf(0) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Trace Neural Geometry Mesh",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = LightSlateText
        )
        Text(
            text = "Tap highlighted digital grid coords in sequence to ground focus.",
            style = MaterialTheme.typography.bodySmall,
            color = MutedSlateText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(300.dp)
                .background(CyberBlack, RoundedCornerShape(10.dp))
                .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(10.dp))
                .testTag("neural_mesh_pad")
        ) {
            // Draw background tracking geometric lines
            Canvas(modifier = Modifier.fillMaxSize()) {
                for (i in 0 until nodes.size - 1) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = nodes[i],
                        end = nodes[i + 1]
                    )
                }
            }

            // Clickable coordinate triggers
            nodes.forEachIndexed { index, node ->
                val isTarget = currentTargetIndex == index
                val isCompleted = index < currentTargetIndex

                Box(
                    modifier = Modifier
                        .offset(x = node.x.dp - 20.dp, y = node.y.dp - 20.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isTarget -> BrightCyanAccent.copy(alpha = 0.15f)
                                isCompleted -> SoftGreenStable.copy(alpha = 0.15f)
                                else -> Color.White.copy(alpha = 0.02f)
                            }
                        )
                        .border(
                            1.dp,
                            when {
                                isTarget -> BrightCyanAccent
                                isCompleted -> SoftGreenStable
                                else -> Color.White.copy(alpha = 0.1f)
                            },
                            CircleShape
                        )
                        .clickable {
                            if (isTarget) {
                                if (index == nodes.size - 1) {
                                    onSuccess()
                                } else {
                                    currentTargetIndex++
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isTarget) BrightCyanAccent else if (isCompleted) SoftGreenStable else MutedSlateText,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// CHALLENGE TYPE 3: Calibrator slider value
@Composable
fun ValueCalibratorChallenge(onSuccess: () -> Unit) {
    var sliderValue1 by remember { mutableStateOf(20f) }
    var sliderValue2 by remember { mutableStateOf(80f) }

    val targetVal1 = 50f
    val targetVal2 = 50f

    val isCalibrated1 = abs(sliderValue1 - targetVal1) < 4f
    val isCalibrated2 = abs(sliderValue2 - targetVal2) < 4f

    LaunchedEffect(isCalibrated1, isCalibrated2) {
        if (isCalibrated1 && isCalibrated2) {
            delay(400)
            onSuccess()
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Align Impulse Calibrators",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = LightSlateText
        )
        Text(
            text = "Drag both focus sliders to exactly 50 to re-center stability.",
            style = MaterialTheme.typography.bodySmall,
            color = MutedSlateText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Slider 1
            Column {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "RHYTHMIC SCAN CALIBRATOR", style = MaterialTheme.typography.labelSmall, color = MutedSlateText)
                    Text(
                        text = "${sliderValue1.toInt()}%",
                        color = if (isCalibrated1) BrightCyanAccent else ElectricAmberWarning,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Slider(
                    value = sliderValue1,
                    onValueChange = { sliderValue1 = it },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = if (isCalibrated1) BrightCyanAccent else ElectricAmberWarning,
                        activeTrackColor = if (isCalibrated1) BrightCyanAccent else ElectricAmberWarning
                    )
                )
            }

            // Slider 2
            Column {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "ACCELERATION SENSOR OFFSET", style = MaterialTheme.typography.labelSmall, color = MutedSlateText)
                    Text(
                        text = "${sliderValue2.toInt()}%",
                        color = if (isCalibrated2) BrightCyanAccent else ElectricAmberWarning,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Slider(
                    value = sliderValue2,
                    onValueChange = { sliderValue2 = it },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = if (isCalibrated2) BrightCyanAccent else ElectricAmberWarning,
                        activeTrackColor = if (isCalibrated2) BrightCyanAccent else ElectricAmberWarning
                    )
                )
            }
        }
    }
}


// DYNAMIC ANIMATED CANVAS GRAPHICS
@Composable
fun FragmentationWaveAnimation(fragmentationLevel: String) {
    val amplitude by animateFloatAsState(
        targetValue = when (fragmentationLevel) {
            "Low" -> 10f
            "Medium" -> 25f
            else -> 48f
        },
        animationSpec = tween(500),
        label = "amplitude"
    )

    val frequency by animateFloatAsState(
        targetValue = when (fragmentationLevel) {
            "Low" -> 0.05f
            "Medium" -> 0.15f
            else -> 0.35f
        },
        animationSpec = tween(500),
        label = "frequency"
    )

    // Run continuous wave time factor
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val timeOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(CyberBlack)
    ) {
        val width = size.width
        val height = size.height
        val midY = height / 2

        val path = androidx.compose.ui.graphics.Path()
        path.moveTo(0f, midY)

        for (x in 0..width.toInt() step 4) {
            val y = midY + amplitude * sin(x * frequency + timeOffset)
            path.lineTo(x.toFloat(), y)
        }

        drawPath(
            path = path,
            color = when (fragmentationLevel) {
                "Low" -> SoftGreenStable
                "Medium" -> ElectricAmberWarning
                else -> AlertRedCollapse
            },
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun RadarSweepAnimation(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    Canvas(modifier = Modifier.size(60.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 2

        // Outer rim
        drawCircle(
            color = color.copy(alpha = 0.2f),
            radius = radius,
            style = Stroke(width = 1.dp.toPx())
        )

        // Radial radar needle
        drawArc(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.4f), Color.Transparent),
                center = center,
                radius = radius
            ),
            startAngle = sweepAngle,
            sweepAngle = -45f,
            useCenter = true
        )
    }
}
