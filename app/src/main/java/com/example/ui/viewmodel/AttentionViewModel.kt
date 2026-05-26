package com.example.ui.viewmodel

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AttentionDatabase
import com.example.data.model.AttentionProfileEntity
import com.example.data.model.DistractionTimelineEntity
import com.example.data.repository.AttentionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random

class AttentionViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AttentionDatabase.getDatabase(application)
    private val repository = AttentionRepository(database.attentionDao())

    // Database exposure
    val allEvents: StateFlow<List<DistractionTimelineEntity>> = repository.allEvents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val profileFlow: StateFlow<AttentionProfileEntity?> = repository.profileFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Live Simulated Engine State
    private val _attentionStability = MutableStateFlow(81)
    val attentionStability = _attentionStability.asStateFlow()

    private val _curiosityDrift = MutableStateFlow(0.38f) // 0.0f to 1.0f
    val curiosityDrift = _curiosityDrift.asStateFlow()

    private val _impulseRisk = MutableStateFlow("Moderate") // Low, Moderate, High
    val impulseRisk = _impulseRisk.asStateFlow()

    private val _mentalFragmentation = MutableStateFlow("Low") // Low, Medium, High
    val mentalFragmentation = _mentalFragmentation.asStateFlow()

    private val _secondsToCrash = MutableStateFlow(40)
    val secondsToCrash = _secondsToCrash.asStateFlow()

    private val _predictedCrashTime = MutableStateFlow("")
    val predictedCrashTime = _predictedCrashTime.asStateFlow()

    private val _isInterventionActive = MutableStateFlow(false)
    val isInterventionActive = _isInterventionActive.asStateFlow()

    private val _activeInterventionType = MutableStateFlow("tactile_shifter") // "tactile_shifter", "neural_mesh", "icon_scramble"
    val activeInterventionType = _activeInterventionType.asStateFlow()

    // Telemetry display meters
    private val _typingVariance = MutableStateFlow(85L) // millisecond variation
    val typingVariance = _typingVariance.asStateFlow()

    private val _scrollSpeedDev = MutableStateFlow(120f) // standard deviation of scroll
    val scrollSpeedDev = _scrollSpeedDev.asStateFlow()

    private val _reactionTimeMs = MutableStateFlow(420L)
    val reactionTimeMs = _reactionTimeMs.asStateFlow()

    private val _appSwitchSpeedHz = MutableStateFlow(0.12f)
    val appSwitchSpeedHz = _appSwitchSpeedHz.asStateFlow()

    private val _lastTelemetryTrigger = MutableStateFlow("Ambient baseline")
    val lastTelemetryTrigger = _lastTelemetryTrigger.asStateFlow()

    // Interactive helper variables
    private var lastKeystoreTime = 0L
    private var typingIntervals = mutableListOf<Long>()

    private var lastScrollTime = 0L
    private var scrollDeltas = mutableListOf<Float>()

    private var appSwitchClicks = 0
    private var lastSwitchClickTime = 0L

    private var notificationPingTime = 0L
    private val _isNotificationActive = MutableStateFlow(false)
    val isNotificationActive = _isNotificationActive.asStateFlow()

    private var stabilityTimerJob: Job? = null

    init {
        // Initialize profile stats
        viewModelScope.launch {
            val profile = repository.getOrCreateProfile()
            _activeInterventionType.update { profile.selectedInterventionMode }
        }
        recalculatePredictedCrashTime()
        startStabilityFluctuations()
    }

    private fun recalculatePredictedCrashTime() {
        val format = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        val crashMs = System.currentTimeMillis() + (_secondsToCrash.value * 1000L)
        _predictedCrashTime.update { format.format(Date(crashMs)) }
    }

    // Passive decay simulation to make the app live and breathing
    private fun startStabilityFluctuations() {
        stabilityTimerJob?.cancel()
        stabilityTimerJob = viewModelScope.launch {
            while (true) {
                delay(2500)
                if (_isInterventionActive.value) continue

                // Slow natural drift over time
                if (Random.nextFloat() > 0.65f) {
                    val delta = Random.nextInt(-2, 2)
                    adjustStability(delta)
                }

                // If stability gets critically low, trigger intervention
                if (_attentionStability.value < 40 && !_isInterventionActive.value) {
                    triggerFocusIntervention("Systemic overload simulated")
                }
            }
        }
    }

    private fun adjustStability(delta: Int) {
        val current = _attentionStability.value
        val next = (current + delta).coerceIn(10, 100)
        _attentionStability.update { next }

        // Metrics derived from stability level
        _curiosityDrift.update { (1.0f - (next / 100f) + Random.nextFloat() * 0.1f).coerceIn(0.0f, 1.0f) }
        _impulseRisk.update {
            when {
                next > 75 -> "Low"
                next > 50 -> "Moderate"
                else -> "High"
            }
        }
        _mentalFragmentation.update {
            when {
                next > 70 -> "Low"
                next > 45 -> "Medium"
                else -> "High"
            }
        }

        // Adjust crash prediction countdown based on stability rate
        val rawSecs = (next * 0.6f + 10).toInt()
        _secondsToCrash.update { rawSecs }
        recalculatePredictedCrashTime()
    }

    // TELEMETRY SIMULATION ACTION: Interactive Keyboard Hesitation
    fun onInteractiveKeyPressed() {
        if (_isInterventionActive.value) return
        val now = SystemClock.elapsedRealtime()
        if (lastKeystoreTime > 0) {
            val delta = now - lastKeystoreTime
            typingIntervals.add(delta)
            if (typingIntervals.size > 8) {
                typingIntervals.removeAt(0)
            }
            // If typing deviation is highly variable, attention is hesitating / wandering
            val avg = typingIntervals.average()
            val variance = typingIntervals.map { abs(it - avg) }.average().toLong()
            _typingVariance.update { variance }

            if (variance > 140) { // high typing hesitation
                _lastTelemetryTrigger.update { "Uneven typing rhythm detected (hestitancy: ${variance}ms)" }
                adjustStability(-3)
            } else {
                _lastTelemetryTrigger.update { "Stable, fluent keystroke typing rhythm" }
                adjustStability(1)
            }
        }
        lastKeystoreTime = now
    }

    // TELEMETRY SIMULATION ACTION: Scrolling Rhythm Fluctuations
    fun onInteractiveScrolled(dragAmount: Float) {
        if (_isInterventionActive.value) return
        val now = SystemClock.elapsedRealtime()
        val absDrag = abs(dragAmount)
        scrollDeltas.add(absDrag)
        if (scrollDeltas.size > 8) {
            scrollDeltas.removeAt(0)
        }

        val speedDeviation = scrollDeltas.average().toFloat()
        _scrollSpeedDev.update { speedDeviation }

        // Frantic over-scrolling or flick swipes
        if (absDrag > 120f) {
            _lastTelemetryTrigger.update { "Chaotic scroll-acceleration burst (${absDrag.toInt()}px/s¹)" }
            adjustStability(-4)
        } else if (now - lastScrollTime < 150) {
            _lastTelemetryTrigger.update { "Frantic scrolling velocity spike" }
            adjustStability(-2)
        } else {
            _lastTelemetryTrigger.update { "Rhythmic scanning speed stabilized" }
            adjustStability(1)
        }
        lastScrollTime = now
    }

    // TELEMETRY SIMULATION ACTION: App Switching Burst Simulator
    fun simulateAppSwitch() {
        if (_isInterventionActive.value) return
        val now = SystemClock.elapsedRealtime()
        appSwitchClicks++
        if (lastSwitchClickTime > 0) {
            val timeBetweenSwitches = now - lastSwitchClickTime
            val frequency = 1000f / timeBetweenSwitches
            _appSwitchSpeedHz.update { frequency }

            if (frequency > 0.8f) { // Rapid hyperactive jumping
                _lastTelemetryTrigger.update { "Hyper-switching burst detected at ${String.format("%.2f", frequency)}Hz" }
                adjustStability(-12)
            } else {
                _lastTelemetryTrigger.update { "Conscious app transit logged" }
                adjustStability(-2)
            }
        }
        lastSwitchClickTime = now
    }

    // TELEMETRY SIMULATION ACTION: Distraction Popups pings
    fun triggerSimulatedNotification() {
        if (_isInterventionActive.value) return
        viewModelScope.launch {
            _isNotificationActive.update { true }
            notificationPingTime = SystemClock.elapsedRealtime()
            // Automatic timeout if user ignores it (which is GOOD for focus!)
            delay(5000)
            if (_isNotificationActive.value) {
                _isNotificationActive.update { false }
                _lastTelemetryTrigger.update { "Resisted micro-interruption (Excellent)" }
                adjustStability(8) // Reward for resisting distraction!
            }
        }
    }

    fun onNotificationClicked() {
        if (!_isNotificationActive.value) return
        _isNotificationActive.update { false }
        val now = SystemClock.elapsedRealtime()
        val elapsed = now - notificationPingTime
        _reactionTimeMs.update { elapsed }

        if (elapsed < 800) { // extremely fast reactive focus grab (impulsive reflex)
            _lastTelemetryTrigger.update { "Impulsive micro-reaction to bait ping ($elapsed ms)" }
            adjustStability(-15)
        } else { // slow conscious tap
            _lastTelemetryTrigger.update { "Deliberate response lag ($elapsed ms)" }
            adjustStability(-5)
        }
    }

    // Force a focus crash state
    fun forceFocusCrash() {
        triggerFocusIntervention("Manual neural instability test overrides")
    }

    // Trigger pattern interrupt overlay
    private fun triggerFocusIntervention(reason: String) {
        viewModelScope.launch {
            _attentionStability.update { 22 } // crash point level
            _isInterventionActive.update { true }
            _lastTelemetryTrigger.update { "Attention collapse imminent! Injecting pattern interrupt." }

            // Increment confronted profile stats
            val profile = repository.getOrCreateProfile()
            val updated = profile.copy(totalConfronted = profile.totalConfronted + 1, stabilityStreak = 0)
            repository.saveProfile(updated)
        }
    }

    // SELECT INTERVENTION MODE
    fun setInterventionMode(mode: String) {
        viewModelScope.launch {
            _activeInterventionType.update { mode }
            val profile = repository.getOrCreateProfile()
            repository.saveProfile(profile.copy(selectedInterventionMode = mode))
        }
    }

    // COMPLETED INTERVENTION (PATERN INTERRUPT DEFEATED MEETS USER)
    fun onInterventionBeaten() {
        viewModelScope.launch {
            _isInterventionActive.update { false }
            _attentionStability.update { 85 } // complete stability restore

            val profile = repository.getOrCreateProfile()
            val nextStreak = profile.stabilityStreak + 1
            val maxStr = maxOf(nextStreak, profile.maxStreak)
            val updated = profile.copy(
                stabilityStreak = nextStreak,
                maxStreak = maxStr,
                totalDefeated = profile.totalDefeated + 1
            )
            repository.saveProfile(updated)

            // Log details in our Timeline Database
            val triggerSources = listOf(
                "Frantic Scrolling Loops",
                "Hyperactive Notification Reflexes",
                "Typing Rhythm Collapse",
                "Erratic App switching jumps"
            )
            val distractionCauses = listOf(
                "Subconscious Instagram reflex trigger",
                "Short-form curiosity trap",
                "Low battery sensory exhaustion (below 25%)",
                "Information micro-burst flooding"
            )

            val event = DistractionTimelineEntity(
                timestamp = System.currentTimeMillis(),
                focusStability = 22,
                predictedCollapseTime = _predictedCrashTime.value,
                triggerReason = triggerSources.random(),
                recoveryDurationSeconds = Random.nextInt(28, 55),
                distractionSource = distractionCauses.random(),
                isInterventionSuccessful = true
            )
            repository.insertEvent(event)
            _lastTelemetryTrigger.update { "Intervention successful. Neural symmetry restored!" }
        }
    }

    fun onInterventionFailed() {
        viewModelScope.launch {
            _isInterventionActive.update { false }
            _attentionStability.update { 45 } // sluggish return

            val profile = repository.getOrCreateProfile()
            val updated = profile.copy(stabilityStreak = 0)
            repository.saveProfile(updated)

            val event = DistractionTimelineEntity(
                timestamp = System.currentTimeMillis(),
                focusStability = 22,
                predictedCollapseTime = _predictedCrashTime.value,
                triggerReason = "Intervention timeout / bypass",
                recoveryDurationSeconds = 120,
                distractionSource = "Doomscroll spiral fully locked",
                isInterventionSuccessful = false
            )
            repository.insertEvent(event)
            _lastTelemetryTrigger.update { "Intervention bypassed. Distraction threshold exceeded." }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearTimeline()
            val profile = repository.getOrCreateProfile()
            repository.saveProfile(profile.copy(stabilityStreak = 0, totalConfronted = 0, totalDefeated = 0))
        }
    }
}
