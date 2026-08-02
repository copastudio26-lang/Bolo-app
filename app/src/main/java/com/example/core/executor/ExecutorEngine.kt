package com.example.core.executor

import android.content.Context
import com.example.core.voice.VoiceIntent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SystemDeviceState(
    val wifiOn: Boolean = true,
    val silentModeOn: Boolean = false,
    val brightnessPercent: Int = 70,
    val mediaVolumePercent: Int = 50,
    val batteryPercent: Int = 84,
    val availableStorageGb: Int = 42,
    val isCalling: Boolean = false,
    val callingContact: String? = null,
    val isCameraActive: Boolean = false,
    val isYoutubeActive: Boolean = false,
    val isInstagramActive: Boolean = false,
    val activeAppOverlayName: String? = null,
    val isSpeechActive: Boolean = false,
    val dndModeOn: Boolean = false,
    val whatsappMessagePendingFor: String? = null,
    val currentNewsHeadline: String? = null
)

class ExecutorEngine(private val context: Context) {

    private val _deviceState = MutableStateFlow(SystemDeviceState())
    val deviceState: StateFlow<SystemDeviceState> = _deviceState.asStateFlow()

    fun executeIntent(intent: VoiceIntent, onStartRoutineChain: (List<String>) -> Unit = {}) {
        _deviceState.update { current ->
            when (intent.action) {
                "WIFI" -> {
                    val target = intent.value == "ON" || (intent.value == null && !current.wifiOn)
                    current.copy(
                        wifiOn = target,
                        activeAppOverlayName = null
                    )
                }
                "SILENT" -> {
                    val target = intent.value == "ON" || (intent.value == null && !current.silentModeOn)
                    current.copy(
                        silentModeOn = target,
                        activeAppOverlayName = null
                    )
                }
                "BRIGHTNESS" -> {
                    val percent = intent.value?.toIntOrNull() ?: current.brightnessPercent
                    current.copy(
                        brightnessPercent = percent.coerceIn(0, 100),
                        activeAppOverlayName = null
                    )
                }
                "VOLUME_UP" -> {
                    val newVolume = (current.mediaVolumePercent + 15).coerceAtMost(100)
                    current.copy(
                        mediaVolumePercent = newVolume,
                        activeAppOverlayName = null
                    )
                }
                "VOLUME_DOWN" -> {
                    val newVolume = (current.mediaVolumePercent - 15).coerceAtLeast(0)
                    current.copy(
                        mediaVolumePercent = newVolume,
                        activeAppOverlayName = null
                    )
                }
                "CALL" -> {
                    current.copy(
                        isCalling = true,
                        callingContact = intent.entity ?: "Papa",
                        activeAppOverlayName = "Call"
                    )
                }
                "WHATSAPP" -> {
                    current.copy(
                        whatsappMessagePendingFor = intent.entity ?: "Priya",
                        activeAppOverlayName = "WhatsApp"
                    )
                }
                "YOUTUBE" -> {
                    current.copy(
                        isYoutubeActive = true,
                        activeAppOverlayName = "YouTube Music"
                    )
                }
                "INSTAGRAM" -> {
                    current.copy(
                        isInstagramActive = true,
                        activeAppOverlayName = "Instagram Reels"
                    )
                }
                "CAMERA" -> {
                    current.copy(
                        isCameraActive = true,
                        activeAppOverlayName = "Camera"
                    )
                }
                "TIME" -> {
                    current.copy(activeAppOverlayName = "Time Info")
                }
                "WEATHER" -> {
                    current.copy(activeAppOverlayName = "Weather Details")
                }
                "BACK", "HOME" -> {
                    current.copy(
                        isCalling = false,
                        callingContact = null,
                        isCameraActive = false,
                        isYoutubeActive = false,
                        isInstagramActive = false,
                        whatsappMessagePendingFor = null,
                        activeAppOverlayName = null
                    )
                }
                "ROUTINE" -> {
                    // Trigger routine chain back in viewModel
                    val routineName = intent.entity ?: ""
                    val actions = when (routineName.lowercase()) {
                        "good morning", "subah" -> listOf("Alarm band karo", "Weather batao", "Calendar summary", "News sunao")
                        "driving mode", "driving" -> listOf("Do Not Disturb on", "Maps navigation start", "WhatsApp auto-reply on", "Music chalao")
                        "office mode", "office" -> listOf("Silent mode", "WiFi band karo", "Bluetooth chalu karo")
                        else -> listOf("Brightness 50%", "Weather batao")
                    }
                    onStartRoutineChain(actions)
                    current.copy(
                        dndModeOn = if (routineName.lowercase().contains("driving")) true else current.dndModeOn,
                        silentModeOn = if (routineName.lowercase().contains("office")) true else current.silentModeOn,
                        wifiOn = if (routineName.lowercase().contains("office")) false else current.wifiOn
                    )
                }
                "BATTERY" -> {
                    current.copy(activeAppOverlayName = "Battery Stats")
                }
                "STORAGE" -> {
                    current.copy(activeAppOverlayName = "Storage Clean")
                }
                else -> {
                    current
                }
            }
        }
    }

    fun dismissOverlay() {
        _deviceState.update { it.copy(activeAppOverlayName = null, isCalling = false, callingContact = null) }
    }
}
