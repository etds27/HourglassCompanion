package com.etds.hourglass.util

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant

/// A reusable timer that tracks elapsed time and allows starting, pausing, and canceling.
/// It can be extended to define specific timer behavior.
///
/// @param scope The [CoroutineScope] in which the timer operates.
/// @param resolution The interval in milliseconds between timer updates.
/// @param startingValue The initial elapsed time in milliseconds.
/// @param callbackResolution The interval in milliseconds between callback updates.
open class Timer(
    private val scope: CoroutineScope,
    private val resolution: Long = 10L,
    private var startTime: Long = 0L,
    private var callbackResolution: Long = resolution,
) {
    private val mutableTimeFlow = MutableStateFlow(0L)

    /// A [StateFlow] that emits the elapsed time in milliseconds.
    val timeFlow: StateFlow<Long> = mutableTimeFlow

    private val mutablePauseFlow = MutableStateFlow(true)
    val pauseFlow: StateFlow<Boolean> = mutablePauseFlow

    private var elapsedTime = startTime
    private var timerJob: Job? = null
    private var lastCompletionHandler: (() -> Unit)? = null

    private var mutableHasStarted: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var hasStarted: StateFlow<Boolean> = mutableHasStarted

    private var lastCallbackTime: Instant = Instant.now()

    /// Track all pause and resume events
    protected var timerEvents: MutableList<Instant> = mutableListOf()

    /// Starts or resumes the timer. If an `onComplete` callback is provided,
    /// it will be invoked when the timer completes.
    /// @param onComplete A callback function executed when the timer finishes.
    fun start(
        onComplete: (() -> Unit)? = null,
        onTimerUpdate: ((Long) -> Unit)? = null
    ) {
        lastCompletionHandler = onComplete ?: lastCompletionHandler
        if (timerJob != null) return // Prevent duplicate jobs
        startTime = System.currentTimeMillis() - elapsedTime

        mutableHasStarted.value = true
        mutablePauseFlow.value = false

        timerEvents.add(Instant.now())

        timerJob = scope.launch {
            while (isActive) {
                mutableTimeFlow.value = System.currentTimeMillis() - startTime
                val now = Instant.now()
                if ((now.minusMillis(callbackResolution).isAfter(lastCallbackTime))) {
                    onTimerUpdate?.let { callback ->
                        Log.d(TAG, "Invoking timer update callback")
                        callback.invoke(mutableTimeFlow.value)
                        lastCallbackTime = now
                    }
                }

                if (isTimerFinished()) {
                    cancel()
                    lastCompletionHandler?.invoke()
                    lastCompletionHandler = null // Reset after execution
                }
                delay(resolution) // Update every resolution interval
            }
        }
    }

    /// Determines whether the timer has finished. Should be overridden in subclasses.
    /// @return `false` by default, allowing the timer to run indefinitely.
    protected open fun isTimerFinished(): Boolean = false

    /// Pauses the timer while preserving elapsed time.
    fun pause() {
        if (timerJob == null) return
        Log.d(TAG, "Pausing timer")
        timerJob?.cancel()
        timerJob = null
        elapsedTime = timeFlow.value
        mutablePauseFlow.value = true
        timerEvents.add(Instant.now())
    }

    /// Cancels the timer and resets elapsed time.
    fun cancel() {
        timerJob?.cancel()
        timerJob = null
        elapsedTime = 0L
        mutableTimeFlow.value = 0L
        lastCompletionHandler = null
    }

    companion object {
        const val TAG = "Timer"
    }
}

/// A countdown timer that runs for a fixed duration and stops when time is up.
/// @param scope The [CoroutineScope] in which the countdown runs.
/// @param duration The countdown duration in milliseconds.
/// @param resolution The interval in milliseconds between timer updates.
/// @param startTime The initial elapsed time in milliseconds. This is the time from value 0
///        If you have a duration of 100 and pass a start time of 75, you will then countdown 25
///        If you want to specify that you have 75 out of 100 seconds left, use remainingTime factory
class CountDownTimer(
    scope: CoroutineScope,
    private val duration: Long,
    resolution: Long = 10L,
    startTime: Long = 0L,
    callbackResolution: Long = resolution
) : Timer(
    scope,
    resolution = resolution,
    startTime = startTime,
    callbackResolution = callbackResolution
) {

    /// A [StateFlow] that emits the remaining time in milliseconds.
    val remainingTimeFlow: StateFlow<Long> = timeFlow
        .map { duration - it }
        .stateIn(scope, SharingStarted.Lazily, duration)

    /// Determines if the countdown has reached zero.
    /// @return `true` if the elapsed time meets or exceeds the duration.
    override fun isTimerFinished(): Boolean = timeFlow.value >= duration

    /// Starts or resumes the timer. If an `onComplete` callback is provided,
    /// it will be invoked when the timer completes.
    /// @param onComplete A callback function executed when the timer finishes.
    /// @param onTimerUpdate A callback function executed on each timer update.
    /// @param onCountDownTimerUpdate A callback function executed on each timer update.
    fun start(
        onComplete: (() -> Unit)? = null,
        onTimerUpdate: ((Long) -> Unit)? = null,
        onCountDownTimerUpdate: ((Long) -> Unit)? = null
    ) {
        super.start(
            onComplete = onComplete,
            onTimerUpdate = { currentDuration ->
                onTimerUpdate?.invoke(currentDuration)
                onCountDownTimerUpdate?.invoke(duration - currentDuration)
            }
        )
    }

    companion object {
        fun fromRemainingTime(
            scope: CoroutineScope,
            duration: Long,
            remainingTime: Long,
            resolution: Long = 10L,
            callbackResolution: Long = resolution
        ): CountDownTimer {
            return CountDownTimer(
                scope = scope,
                duration = duration,
                startTime = duration - remainingTime,
                resolution = resolution,
                callbackResolution = callbackResolution
            )
        }

        fun fromStartingTime(
            scope: CoroutineScope,
            duration: Long,
            startingTime: Long,
            resolution: Long = 10L,
            callbackResolution: Long = resolution
        ): CountDownTimer {
            return CountDownTimer(
                scope = scope,
                duration = duration,
                startTime = startingTime,
                resolution = resolution,
                callbackResolution = callbackResolution
            )
        }
    }
}
