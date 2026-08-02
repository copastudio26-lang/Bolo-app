package com.example.features.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.executor.SystemDeviceState
import com.example.data.local.CommandEntity
import com.example.data.local.RoutineEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: BoloViewModel,
    onNavigateToRoutines: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recentCmds by viewModel.recentCommands.collectAsStateWithLifecycle()
    val routines by viewModel.allRoutines.collectAsStateWithLifecycle()
    val deviceState by viewModel.deviceState.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val offlineMode by viewModel.isForceOfflineMode.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MidnightSlate, DeepCoal)
                )
            )
    ) {
        // Subtle background decoration to avoid flat design
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-80).dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(SunsetAmber.copy(alpha = 0.08f), Color.Transparent)
                        ),
                        radius = size.width
                    )
                }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Section
            item {
                HeaderComponent(
                    userName = userName,
                    offlineMode = offlineMode,
                    deviceState = deviceState,
                    onToggleOffline = { viewModel.setForceOfflineMode(!offlineMode) }
                )
            }

            // Big Listening Trigger Panel
            item {
                ActionVoiceCard(
                    onSpeakClick = { viewModel.startListening() },
                    offlineMode = offlineMode
                )
            }

            // Live Device State Toggles Card
            item {
                LiveStateCard(deviceState = deviceState, viewModel = viewModel)
            }

            // Quick Trigger Routines
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ Quick Routines (\"Riwaz\")",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftCyan
                        )
                        Text(
                            text = "See All",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = SunsetAmber,
                            modifier = Modifier
                                .clickable { onNavigateToRoutines() }
                                .padding(4.dp)
                        )
                    }

                    if (routines.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DeepCoal),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().border(1.dp, DarkSteel, RoundedCornerShape(16.dp))
                        ) {
                            Text(
                                text = "Routines load ho rahi hain...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedSlate,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(end = 20.dp)
                        ) {
                            items(routines.take(4)) { routine ->
                                QuickRoutineItem(
                                    routine = routine,
                                    onTrigger = {
                                        // Submit voice-equivalent text
                                        viewModel.stopListeningAndProcess("Routine: ${routine.name} chalao")
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Recent Commands History (the "Audit Trail")
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📋 Command Logs (\"Audit Trail\")",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftCyan
                        )
                        if (recentCmds.isNotEmpty()) {
                            Text(
                                text = "Clear Logs",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = DangerRed,
                                modifier = Modifier
                                    .clickable { viewModel.clearAllCommandHistory() }
                                    .padding(4.dp)
                            )
                        }
                    }

                    if (recentCmds.isEmpty()) {
                        EmptyStateLogs()
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            recentCmds.forEach { command ->
                                CommandLogItem(command = command)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderComponent(
    userName: String,
    offlineMode: Boolean,
    deviceState: SystemDeviceState,
    onToggleOffline: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hello, $userName 👋",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = SoftCyan,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Bolophone: \"Bolte Hi Ho Jayega\"",
                style = MaterialTheme.typography.bodyMedium,
                color = SunsetAmber,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Offline Network Toggle Button
        IconButton(
            onClick = onToggleOffline,
            modifier = Modifier
                .clip(CircleShape)
                .background(if (offlineMode) DangerRed.copy(alpha = 0.2f) else ActiveMint.copy(alpha = 0.15f))
                .border(1.dp, if (offlineMode) DangerRed else ActiveMint, CircleShape)
                .testTag("network_toggle")
        ) {
            Icon(
                imageVector = if (offlineMode) Icons.Filled.CloudOff else Icons.Filled.CloudQueue,
                contentDescription = "Toggle Network Connection",
                tint = if (offlineMode) DangerRed else ActiveMint
            )
        }
    }
}

@Composable
fun ActionVoiceCard(
    onSpeakClick: () -> Unit,
    offlineMode: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = DeepCoal),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkSteel, RoundedCornerShape(24.dp))
            .shadow(16.dp, ambientColor = Color.Black, spotColor = Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Tap & Speak Hindi / Hinglish / English",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = SoftCyan,
                textAlign = TextAlign.Center
            )

            // Pulsing Mic Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(110.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(SunsetAmber, SunsetAmber.copy(alpha = 0.3f))
                        )
                    )
                    .clickable { onSpeakClick() }
                    .testTag("listening_trigger")
            ) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = "Speak Now Button",
                    tint = MidnightSlate,
                    modifier = Modifier.size(46.dp)
                )
            }

            // Dynamic tags showing current mode
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SuggestionChip(
                    onClick = {},
                    label = { Text("Local Whisper.cpp", fontWeight = FontWeight.Bold) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        labelColor = ActiveMint,
                        containerColor = ActiveMint.copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(1.dp, ActiveMint.copy(alpha = 0.4f))
                )
                SuggestionChip(
                    onClick = {},
                    label = { Text(if (offlineMode) "100% Offline" else "Online Gemini", fontWeight = FontWeight.Bold) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        labelColor = if (offlineMode) DangerRed else SunsetAmber,
                        containerColor = (if (offlineMode) DangerRed else SunsetAmber).copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(1.dp, (if (offlineMode) DangerRed else SunsetAmber).copy(alpha = 0.4f))
                )
            }
        }
    }
}

@Composable
fun LiveStateCard(
    deviceState: SystemDeviceState,
    viewModel: BoloViewModel
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeepCoal),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkSteel, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚙️ Active Device States",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SoftCyan
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ActiveMint.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, ActiveMint.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "SAFE SECURE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ActiveMint,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Divider(color = DarkSteel)

            // Grid of toggles
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // WiFi & Silent Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StateToggleItem(
                        icon = if (deviceState.wifiOn) Icons.Filled.Wifi else Icons.Filled.WifiOff,
                        label = "WiFi Status",
                        value = if (deviceState.wifiOn) "CHALU (ON)" else "BAND (OFF)",
                        active = deviceState.wifiOn,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.stopListeningAndProcess("WiFi toggle karo")
                        }
                    )

                    StateToggleItem(
                        icon = if (deviceState.silentModeOn) Icons.Filled.VolumeMute else Icons.Filled.VolumeUp,
                        label = "Silent Mode",
                        value = if (deviceState.silentModeOn) "SILENT ON" else "GENERAL MODE",
                        active = deviceState.silentModeOn,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.stopListeningAndProcess("Silent mode toggle karo")
                        }
                    )
                }

                // Volume & Brightness Percent Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StateToggleItem(
                        icon = Icons.Filled.BrightnessHigh,
                        label = "Brightness",
                        value = "${deviceState.brightnessPercent}%",
                        active = true,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.stopListeningAndProcess("Brightness 50% set karo")
                        }
                    )

                    StateToggleItem(
                        icon = Icons.Filled.VolumeUp,
                        label = "Media Volume",
                        value = "${deviceState.mediaVolumePercent}%",
                        active = true,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.stopListeningAndProcess("Volume badhao")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StateToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (active) DarkSteel else DeepCoal,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (active) ActiveMint.copy(alpha = 0.5f) else DarkSteel),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (active) ActiveMint.copy(alpha = 0.2f) else DarkSteel.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (active) ActiveMint else MutedSlate,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedSlate
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (active) SoftCyan else MutedSlate
                )
            }
        }
    }
}

@Composable
fun QuickRoutineItem(
    routine: RoutineEntity,
    onTrigger: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeepCoal),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(180.dp)
            .border(1.dp, if (routine.isEnabled) SunsetAmber.copy(alpha = 0.4f) else DarkSteel, RoundedCornerShape(16.dp))
            .clickable { onTrigger() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (routine.triggerType) {
                        "TIME" -> Icons.Outlined.AccessTime
                        "LOCATION" -> Icons.Outlined.Place
                        "EVENT" -> Icons.Outlined.Devices
                        else -> Icons.Outlined.VoiceOverOff
                    },
                    contentDescription = "Trigger Type",
                    tint = SunsetAmber,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = routine.triggerType,
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedSlate,
                    fontWeight = FontWeight.Bold
                )
            }

            Column {
                Text(
                    text = routine.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = SoftCyan,
                    maxLines = 1
                )
                Text(
                    text = routine.triggerValue,
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedSlate,
                    maxLines = 1
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SunsetAmber.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Run",
                    tint = SunsetAmber,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "RUN NOW",
                    style = MaterialTheme.typography.labelSmall,
                    color = SunsetAmber,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun CommandLogItem(command: CommandEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeepCoal.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkSteel, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when (command.category) {
                            "SYSTEM" -> ActiveMint.copy(alpha = 0.2f)
                            "COMMUNICATION" -> SunsetAmber.copy(alpha = 0.2f)
                            "APPS" -> SoftCyan.copy(alpha = 0.15f)
                            "AUTOMATION" -> SunsetAmber.copy(alpha = 0.2f)
                            else -> DarkSteel
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (command.category) {
                        "SYSTEM" -> Icons.Filled.SettingsInputHdmi
                        "COMMUNICATION" -> Icons.Filled.ChatBubbleOutline
                        "APPS" -> Icons.Filled.Apps
                        "MEDIA" -> Icons.Filled.MusicNote
                        "NAVIGATION" -> Icons.Filled.Navigation
                        "AUTOMATION" -> Icons.Filled.FlashOn
                        else -> Icons.Filled.Hearing
                    },
                    contentDescription = command.category,
                    tint = when (command.category) {
                        "SYSTEM" -> ActiveMint
                        "COMMUNICATION" -> SunsetAmber
                        "APPS" -> SoftCyan
                        "AUTOMATION" -> SunsetAmber
                        else -> MutedSlate
                    },
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "\"${command.commandText}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftCyan
                    )

                    val timeString = remember(command.timestamp) {
                        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                        sdf.format(Date(command.timestamp))
                    }
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedSlate
                    )
                }

                Text(
                    text = command.outputMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (command.isSuccess) ActiveMint else DangerRed
                )
            }
        }
    }
}

@Composable
fun EmptyStateLogs() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.History,
            contentDescription = "No Logs yet",
            tint = MutedSlate,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "No commands executed yet.\nTry saying \"Hey Bolo, weather kaisa hai?\" or click the Mic above!",
            style = MaterialTheme.typography.bodyMedium,
            color = MutedSlate,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}
