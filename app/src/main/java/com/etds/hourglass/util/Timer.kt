package com.etds.hourglass.util

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/// A reusable timer that tracks elapsed time and allows starting, pausing, and canceling.
/// It can be extended to define specific timer behavior.
///
/// @param scope The [CoroutineScope] in which the timer operates.
open class Timer(
    private val scope: CoroutineScope,
    private val resolution: Long = 10L
    ) {
    private val mutableTimeFlow = MutableStateFlow(0L)

    /// A [StateFlow] that emits the elapsed time in milliseconds.
    val timeFlow: StateFlow<Long> = mutableTimeFlow

    private val mutablePauseFlow = MutableStateFlow(true)
    val pauseFlow: StateFlow<Boolean> = mutablePauseFlow

    private var startTime = 0L
    private var elapsedTime = 0L
    private var timerJob: Job? = null
    private var lastCompletionHandler: (() -> Unit)? = null
    private var _hasStarted: Boolean = false

    /// Starts or resumes the timer. If an `onComplete` callback is provided,
    /// it will be invoked when the timer completes.
    /// @param onComplete A callback function executed when the timer finishes.
    fun start(onComplete: (() -> Unit)? = null) {
        lastCompletionHandler = onComplete ?: lastCompletionHandler
        if (timerJob != null) return // Prevent duplicate jobs
        startTime = System.currentTimeMillis() - elapsedTime

        _hasStarted = true
        mutablePauseFlow.value = false

        timerJob = scope.launch {
            while (isActive) {
                mutableTimeFlow.value = System.currentTimeMillis() - startTime

                if (isTimerFinished()) {
                    cancel()
                    lastCompletionHandler?.invoke()
                    lastCompletionHandler = null // Reset after execution
                }
                delay(resolution) // Update every second
            }
        }
    }

    /// Determines whether the timer has finished. Should be overridden in subclasses.
    /// @return `false` by default, allowing the timer to run indefinitely.
    protected open fun isTimerFinished(): Boolean = false

    /// Pauses the timer while preserving elapsed time.
    fun pause() {
        timerJob?.cancel()
        timerJob = null
        elapsedTime = timeFlow.value
        mutablePauseFlow.value = true
    }

    /// Cancels the timer and resets elapsed time.
    fun cancel() {
        timerJob?.cancel()
        timerJob = null
        elapsedTime = 0L
        mutableTimeFlow.value = 0L
        lastCompletionHandler = null
    }
}

/// A countdown timer that runs for a fixed duration and stops when time is up.
/// @param scope The [CoroutineScope] in which the countdown runs.
/// @param duration The countdown duration in milliseconds.
class CountDownTimer(scope: CoroutineScope, private val duration: Long) : Timer(scope) {
    /// A [StateFlow] that emits the remaining time in milliseconds.
    val remainingTimeFlow: StateFlow<Long> = timeFlow
        .map { duration - it }
        .stateIn(scope, SharingStarted.Lazily, duration)
    /// Determines if the countdown has reached zero.
    /// @return `true` if the elapsed time meets or exceeds the duration.
    override fun isTimerFinished(): Boolean = timeFlow.value >= duration
}
