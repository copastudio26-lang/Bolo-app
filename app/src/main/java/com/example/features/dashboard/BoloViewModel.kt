package com.example.features.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.executor.ExecutorEngine
import com.example.core.executor.SystemDeviceState
import com.example.core.voice.VoiceEngine
import com.example.core.voice.VoiceIntent
import com.example.data.local.BoloDatabase
import com.example.data.local.CommandEntity
import com.example.data.local.RoutineEntity
import com.example.data.repository.BoloRepository
import org.json.JSONArray
import kotlinx.coroutines.Delay
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ProcessingState {
    IDLE, RECORDING, PARSING, CONFIRMATION_PENDING, EXECUTING, COMPLETED, ERROR
}

class BoloViewModel(
    application: Application,
    private val repository: BoloRepository,
    val voiceEngine: VoiceEngine,
    val executorEngine: ExecutorEngine
) : AndroidViewModel(application) {

    private val tag = "BoloViewModel"

    // --- State Streams ---
    val recentCommands: StateFlow<List<CommandEntity>> = repository.recentCommands
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRoutines: StateFlow<List<RoutineEntity>> = repository.allRoutines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deviceState: StateFlow<SystemDeviceState> = executorEngine.deviceState

    // --- UI States ---
    private val _processingState = MutableStateFlow(ProcessingState.IDLE)
    val processingState = _processingState.asStateFlow()

    private val _currentCommandText = MutableStateFlow("")
    val currentCommandText = _currentCommandText.asStateFlow()

    private val _feedbackMessage = MutableStateFlow("")
    val feedbackMessage = _feedbackMessage.asStateFlow()

    private val _activeIntent = MutableStateFlow<VoiceIntent?>(null)
    val activeIntent = _activeIntent.asStateFlow()

    // --- Biometric Auth Overlay Trigger ---
    private val _showBiometricPrompt = MutableStateFlow(false)
    val showBiometricPrompt = _showBiometricPrompt.asStateFlow()

    // --- Routine step-by-step chain ---
    private val _routineSteps = MutableStateFlow<List<String>>(emptyList())
    val routineSteps = _routineSteps.asStateFlow()

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex = _currentStepIndex.asStateFlow()

    private val _activeRunningRoutine = MutableStateFlow<String?>(null)
    val activeRunningRoutine = _activeRunningRoutine.asStateFlow()

    // --- User Personalization ---
    private val _userName = MutableStateFlow("Sir")
    val userName = _userName.asStateFlow()

    private val _isForceOfflineMode = MutableStateFlow(false)
    val isForceOfflineMode = _isForceOfflineMode.asStateFlow()

    // --- Privacy Dashboard Stats ---
    private val _biometricEnabled = MutableStateFlow(true)
    val biometricEnabled = _biometricEnabled.asStateFlow()

    private val _voiceprintVerified = MutableStateFlow(true)
    val voiceprintVerified = _voiceprintVerified.asStateFlow()

    private val _onDeviceOnly = MutableStateFlow(true)
    val onDeviceOnly = _onDeviceOnly.asStateFlow()

    // --- Routine Editing State ---
    private val _editingRoutine = MutableStateFlow<RoutineEntity?>(null)
    val editingRoutine = _editingRoutine.asStateFlow()

    // --- Active coroutine jobs to prevent hanging and ghost executions ---
    private var activeParsingJob: kotlinx.coroutines.Job? = null
    private var activeAutomationJob: kotlinx.coroutines.Job? = null
    private var routineJob: kotlinx.coroutines.Job? = null

    fun setForceOfflineMode(enabled: Boolean) {
        _isForceOfflineMode.value = enabled
    }

    fun toggleBiometric() {
        _biometricEnabled.value = !_biometricEnabled.value
    }

    fun toggleVoiceprint() {
        _voiceprintVerified.value = !_voiceprintVerified.value
    }

    fun toggleOnDeviceOnly() {
        _onDeviceOnly.value = !_onDeviceOnly.value
    }

    fun setEditingRoutine(routine: RoutineEntity?) {
        _editingRoutine.value = routine
    }

    /**
     * Start speech recording simulation
     */
    fun startListening() {
        _processingState.value = ProcessingState.RECORDING
        _currentCommandText.value = ""
        _feedbackMessage.value = "Aapki aawaz sun raha hoon..."
        _activeIntent.value = null
    }

    /**
     * Complete listing and submit to parser
     */
    fun stopListeningAndProcess(spokenText: String) {
        if (spokenText.isBlank()) {
            _processingState.value = ProcessingState.IDLE
            return
        }

        activeParsingJob?.cancel()
        activeParsingJob = viewModelScope.launch {
            _currentCommandText.value = spokenText
            _processingState.value = ProcessingState.PARSING
            _feedbackMessage.value = "Intent samajhne ki koshish ho rahi hai..."

            val parsedIntent = voiceEngine.parseCommand(spokenText, forceOffline = _isForceOfflineMode.value)
            _activeIntent.value = parsedIntent

            // Check security level
            val level = getSecurityLevel(parsedIntent)
            if (level == 3 && _biometricEnabled.value) {
                // Requires Level 3 Biometric Authentication
                _processingState.value = ProcessingState.CONFIRMATION_PENDING
                _feedbackMessage.value = "Security Alert: Biometric authorization required!"
                _showBiometricPrompt.value = true
            } else if (level == 2) {
                // Yellow Level: Messaging/Posting requires direct manual confirmation
                _processingState.value = ProcessingState.CONFIRMATION_PENDING
                _feedbackMessage.value = "Action is ready. Please confirm to proceed."
            } else {
                // Green Level or Biometric off: Execute immediately
                executeParsedIntent(parsedIntent)
            }
        }
    }

    /**
     * User confirms command manually or passes biometric
     */
    fun confirmCommand() {
        val intent = _activeIntent.value ?: return
        _showBiometricPrompt.value = false
        executeParsedIntent(intent)
    }

    /**
     * User cancels command
     */
    fun cancelCommand() {
        activeParsingJob?.cancel()
        activeParsingJob = null
        activeAutomationJob?.cancel()
        activeAutomationJob = null
        routineJob?.cancel()
        routineJob = null
        _routineSteps.value = emptyList()
        _activeRunningRoutine.value = null
        _showBiometricPrompt.value = false
        _processingState.value = ProcessingState.IDLE
        _activeIntent.value = null
    }

    /**
     * Evaluates Security Level for whitelisting (Tier-1 Biometric Requirement)
     */
    private fun getSecurityLevel(intent: VoiceIntent): Int {
        val lowerText = intent.successMessage.lowercase() + " " + intent.action.lowercase()
        return when {
            // LEVEL 3 (Red) - Money, payments, security settings changes
            lowerText.contains("pay") || lowerText.contains("money") || lowerText.contains("bhejo") && (lowerText.contains("rupaye") || lowerText.contains("rs") || lowerText.contains("₹") || lowerText.contains("security") || lowerText.contains("setting")) -> 3
            // LEVEL 2 (Yellow) - Sending messages, posting to social apps
            intent.action == "WHATSAPP" || intent.action == "MESSAGE" || intent.action == "INSTAGRAM" -> 2
            // LEVEL 1 (Green) - System status, basic volume, calling contacts
            else -> 1
        }
    }

    /**
     * Executes the validated command
     */
    private fun executeParsedIntent(intent: VoiceIntent) {
        activeAutomationJob?.cancel()
        activeAutomationJob = viewModelScope.launch {
            _processingState.value = ProcessingState.EXECUTING
            _feedbackMessage.value = "Automation running: " + intent.successMessage

            // Give a small delay to simulate processing steps
            delay(800)

            executorEngine.executeIntent(intent, onStartRoutineChain = { steps ->
                startRoutineChaining(intent.entity ?: "Routine", steps)
            })

            // Store in Room DB
            repository.insertCommand(
                CommandEntity(
                    commandText = _currentCommandText.value,
                    category = intent.category,
                    isSuccess = true,
                    outputMessage = intent.successMessage
                )
            )

            // Finish
            if (_routineSteps.value.isEmpty()) {
                _processingState.value = ProcessingState.COMPLETED
                _feedbackMessage.value = intent.successMessage
                delay(3000)
                if (_processingState.value == ProcessingState.COMPLETED) {
                    _processingState.value = ProcessingState.IDLE
                }
            }
        }
    }

    /**
     * Step-by-step Routine triggers chain (the "Riwaz" Engine)
     */
    private fun startRoutineChaining(routineName: String, steps: List<String>) {
        if (steps.isEmpty()) return
        _activeRunningRoutine.value = routineName
        _routineSteps.value = steps
        _currentStepIndex.value = 0

        routineJob?.cancel()
        routineJob = viewModelScope.launch {
            for (i in steps.indices) {
                _currentStepIndex.value = i
                _processingState.value = ProcessingState.EXECUTING
                _feedbackMessage.value = "Executing Step ${i + 1} of ${steps.size}: ${steps[i]}"

                // Delay to allow user to visually inspect each automation step
                delay(1800)

                // Match step text against local rules to execute
                val stepIntent = voiceEngine.parseCommand(steps[i], forceOffline = true)
                executorEngine.executeIntent(stepIntent)

                // Save command step to database logs
                repository.insertCommand(
                    CommandEntity(
                        commandText = steps[i],
                        category = "AUTOMATION",
                        isSuccess = true,
                        outputMessage = "[Routine: $routineName] " + stepIntent.successMessage
                    )
                )
            }

            // Finished routine chain
            _processingState.value = ProcessingState.COMPLETED
            _feedbackMessage.value = "Routine '$routineName' successfully completed! All steps executed offline."
            delay(3500)
            _routineSteps.value = emptyList()
            _activeRunningRoutine.value = null
            _processingState.value = ProcessingState.IDLE
        }
    }

    // --- Routine CRUD ops ---
    fun saveRoutine(name: String, triggerType: String, triggerValue: String, actionsList: List<String>) {
        viewModelScope.launch {
            val json = JSONArray()
            actionsList.forEach { json.put(it) }

            val routine = _editingRoutine.value
            if (routine != null) {
                repository.updateRoutine(
                    routine.copy(
                        name = name,
                        triggerType = triggerType,
                        triggerValue = triggerValue,
                        actionsJson = json.toString()
                    )
                )
            } else {
                repository.insertRoutine(
                    RoutineEntity(
                        name = name,
                        triggerType = triggerType,
                        triggerValue = triggerValue,
                        actionsJson = json.toString(),
                        isEnabled = true
                    )
                )
            }
            _editingRoutine.value = null
        }
    }

    fun toggleRoutineEnabled(routine: RoutineEntity) {
        viewModelScope.launch {
            repository.updateRoutine(routine.copy(isEnabled = !routine.isEnabled))
        }
    }

    fun deleteRoutine(routine: RoutineEntity) {
        viewModelScope.launch {
            repository.deleteRoutineById(routine.id)
            _editingRoutine.value = null
        }
    }

    fun clearAllCommandHistory() {
        viewModelScope.launch {
            repository.clearAllCommands()
        }
    }

    fun dismissOverlay() {
        executorEngine.dismissOverlay()
    }
}

class BoloViewModelFactory(
    private val application: Application,
    private val repository: BoloRepository,
    private val voiceEngine: VoiceEngine,
    private val executorEngine: ExecutorEngine
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BoloViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BoloViewModel(application, repository, voiceEngine, executorEngine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
