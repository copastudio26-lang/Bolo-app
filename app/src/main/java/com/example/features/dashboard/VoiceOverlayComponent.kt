package com.example.features.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.executor.SystemDeviceState
import com.example.ui.theme.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin
import kotlin.math.PI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceOverlayComponent(
    viewModel: BoloViewModel,
    modifier: Modifier = Modifier
) {
    val processingState by viewModel.processingState.collectAsStateWithLifecycle()
    val commandText by viewModel.currentCommandText.collectAsStateWithLifecycle()
    val feedbackMsg by viewModel.feedbackMessage.collectAsStateWithLifecycle()
    val showBiometricPrompt by viewModel.showBiometricPrompt.collectAsStateWithLifecycle()
    val deviceState by viewModel.deviceState.collectAsStateWithLifecycle()

    val routineSteps by viewModel.routineSteps.collectAsStateWithLifecycle()
    val currentStepIndex by viewModel.currentStepIndex.collectAsStateWithLifecycle()
    val activeRoutineName by viewModel.activeRunningRoutine.collectAsStateWithLifecycle()

    var customText by remember { mutableStateOf("") }

    if (processingState == ProcessingState.IDLE) return

    // Full screen overlay Dialog
    Dialog(
        onDismissRequest = { viewModel.cancelCommand() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MidnightSlate.copy(alpha = 0.95f))
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Close Button
            IconButton(
                onClick = { viewModel.cancelCommand() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(DarkSteel)
                    .testTag("close_voice_overlay")
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Close Voice Assistant", tint = SoftCyan)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top: App branding
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = "BOLOPHONE VOICE ENGINE",
                        style = MaterialTheme.typography.labelMedium,
                        color = SunsetAmber,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Suno • Dekho • Karo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedSlate
                    )
                }

                // Middle: Interactive State Visualizers
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    when (processingState) {
                        ProcessingState.RECORDING -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                ListeningWavesVisualizer()
                                Text(
                                    text = "Listening... Bolna shuru karein",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftCyan
                                )
                                Text(
                                    text = "Or type or select a mock command below:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedSlate
                                )

                                // Custom Keyboard Input Textfield
                                OutlinedTextField(
                                    value = customText,
                                    onValueChange = { customText = it },
                                    placeholder = { Text("Type custom voice command...", color = MutedSlate) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .testTag("custom_voice_input"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = SoftCyan,
                                        unfocusedTextColor = SoftCyan,
                                        focusedBorderColor = SunsetAmber,
                                        unfocusedBorderColor = DarkSteel
                                    ),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(onSend = {
                                        if (customText.isNotBlank()) {
                                            viewModel.stopListeningAndProcess(customText)
                                            customText = ""
                                        }
                                    }),
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            if (customText.isNotBlank()) {
                                                viewModel.stopListeningAndProcess(customText)
                                                customText = ""
                                            }
                                        }) {
                                            Icon(Icons.Filled.Send, contentDescription = "Send", tint = SunsetAmber)
                                        }
                                    }
                                )

                                // Pre-baked testing list for easy emulator demo
                                Text(
                                    text = "🎯 Quick Test Commands:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SunsetAmber,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.Start).padding(start = 16.dp)
                                )
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .padding(horizontal = 16.dp)
                                ) {
                                    val demos = listOf(
                                        "Papa ko call karo",
                                        "Priya ko WhatsApp karo",
                                        "WiFi band karo",
                                        "Volume badhao",
                                        "Brightness 80% karo",
                                        "Mausam kaisa hai?",
                                        "Routine: Good Morning chalao",
                                        "Send money ₹1000 to Papa"
                                    )
                                    items(demos) { text ->
                                        Surface(
                                            onClick = { viewModel.stopListeningAndProcess(text) },
                                            color = DeepCoal,
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, DarkSteel),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(text, style = MaterialTheme.typography.bodyMedium, color = SoftCyan)
                                                Icon(Icons.Filled.ArrowForward, contentDescription = "Trigger", tint = SunsetAmber, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        ProcessingState.PARSING -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(color = SunsetAmber)
                                Text(
                                    text = "Analyzing with AI...",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftCyan
                                )
                                Text(
                                    text = "\"$commandText\"",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = SunsetAmber,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }

                        ProcessingState.CONFIRMATION_PENDING -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                if (showBiometricPrompt) {
                                    BiometricScanVisualizer(
                                        onAuthorize = { viewModel.confirmCommand() },
                                        onCancel = { viewModel.cancelCommand() }
                                    )
                                } else {
                                    Icon(
                                        Icons.Filled.Warning,
                                        contentDescription = "Confirmation Required",
                                        tint = SunsetAmber,
                                        modifier = Modifier.size(60.dp)
                                    )
                                    Text(
                                        text = "Confirm Action",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftCyan
                                    )
                                    Text(
                                        text = "Are you sure you want to execute:\n\"$commandText\"?",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MutedSlate,
                                        textAlign = TextAlign.Center
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.cancelCommand() },
                                            colors = ButtonDefaults.buttonColors(containerColor = DarkSteel),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Cancel", color = SoftCyan)
                                        }

                                        Button(
                                            onClick = { viewModel.confirmCommand() },
                                            colors = ButtonDefaults.buttonColors(containerColor = SunsetAmber),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Execute", color = MidnightSlate, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        ProcessingState.EXECUTING -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                if (routineSteps.isNotEmpty()) {
                                    // Chaining multi-step Routine Riwaz Runner!
                                    Text(
                                        text = "⚡ Routine: $activeRoutineName",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = SunsetAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Executing steps offline step-by-step:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedSlate
                                    )

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = DeepCoal),
                                        modifier = Modifier.fillMaxWidth().border(1.dp, DarkSteel, RoundedCornerShape(16.dp))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            routineSteps.forEachIndexed { idx, step ->
                                                val isActive = idx == currentStepIndex
                                                val isDone = idx < currentStepIndex

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Icon(
                                                        imageVector = when {
                                                            isDone -> Icons.Filled.CheckCircle
                                                            isActive -> Icons.Filled.PlayCircleFilled
                                                            else -> Icons.Filled.RadioButtonUnchecked
                                                        },
                                                        contentDescription = null,
                                                        tint = when {
                                                            isDone -> ActiveMint
                                                            isActive -> SunsetAmber
                                                            else -> MutedSlate
                                                        },
                                                        modifier = Modifier.size(20.dp)
                                                    )

                                                    Text(
                                                        text = step,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isActive) SoftCyan else MutedSlate
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    LinearProgressIndicator(
                                        progress = (currentStepIndex + 1).toFloat() / routineSteps.size.toFloat(),
                                        color = SunsetAmber,
                                        trackColor = DarkSteel,
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                                    )
                                } else {
                                    // Single action execution overlay animation
                                    ExecutionOverlayVisualizer(
                                        actionText = commandText,
                                        deviceState = deviceState,
                                        feedback = feedbackMsg,
                                        onDismiss = { viewModel.cancelCommand() }
                                    )
                                }
                            }
                        }

                        ProcessingState.COMPLETED -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Success",
                                    tint = ActiveMint,
                                    modifier = Modifier.size(80.dp)
                                )
                                Text(
                                    text = "Done!",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftCyan
                                )
                                Text(
                                    text = feedbackMsg,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MutedSlate,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )
                            }
                        }

                        else -> {}
                    }
                }

                // Bottom: Spoken prompt text
                if (processingState != ProcessingState.RECORDING && processingState != ProcessingState.PARSING) {
                    Text(
                        text = "\"$commandText\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedSlate,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun ListeningWavesVisualizer() {
    val infiniteTransition = rememberInfiniteTransition(label = "voice_wave")

    // Animate phases for the three overlapping waves to create organic fluid movement
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )

    val phase2 by infiniteTransition.animateFloat(
        initialValue = (2 * PI).toFloat(),
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    val phase3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase3"
    )

    // Animate breathing amplitude scale
    val amplitudeScale by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "amplitude_scale"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(horizontal = 16.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        // Draw 3 distinct overlapping sine waves with Gaussian-like envelopes
        // Wave 1: Active Mint (Background Wave)
        val path1 = Path()
        val amplitude1 = 35f * amplitudeScale
        val cycles1 = 1.8f
        for (x in 0..width.toInt() step 2) {
            val progress = x.toFloat() / width
            val envelope = sin(PI * progress).toFloat()
            val angle = (2 * PI * cycles1 * progress) + phase1
            val y = centerY + envelope * amplitude1 * sin(angle).toFloat()
            if (x == 0) path1.moveTo(x.toFloat(), y) else path1.lineTo(x.toFloat(), y)
        }
        drawPath(
            path = path1,
            color = ActiveMint.copy(alpha = 0.4f),
            style = Stroke(width = 3.dp.toPx())
        )

        // Wave 2: Soft Cyan (Middle Wave)
        val path2 = Path()
        val amplitude2 = 22f * amplitudeScale
        val cycles2 = 2.8f
        for (x in 0..width.toInt() step 2) {
            val progress = x.toFloat() / width
            val envelope = sin(PI * progress).toFloat()
            val angle = (2 * PI * cycles2 * progress) + phase2
            val y = centerY + envelope * amplitude2 * sin(angle).toFloat()
            if (x == 0) path2.moveTo(x.toFloat(), y) else path2.lineTo(x.toFloat(), y)
        }
        drawPath(
            path = path2,
            color = SoftCyan.copy(alpha = 0.5f),
            style = Stroke(width = 2.dp.toPx())
        )

        // Wave 3: Sunset Amber (Primary Foreground Wave)
        val path3 = Path()
        val amplitude3 = 45f * amplitudeScale
        val cycles3 = 1.2f
        for (x in 0..width.toInt() step 2) {
            val progress = x.toFloat() / width
            val envelope = sin(PI * progress).toFloat()
            val angle = (2 * PI * cycles3 * progress) + phase3
            val y = centerY + envelope * amplitude3 * sin(angle).toFloat()
            if (x == 0) path3.moveTo(x.toFloat(), y) else path3.lineTo(x.toFloat(), y)
        }
        drawPath(
            path = path3,
            color = SunsetAmber.copy(alpha = 0.85f),
            style = Stroke(width = 4.dp.toPx())
        )
    }
}

@Composable
fun BiometricScanVisualizer(
    onAuthorize: () -> Unit,
    onCancel: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "biometric")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = DeepCoal),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, DangerRed.copy(alpha = borderAlpha), RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(DangerRed.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Fingerprint, contentDescription = "Shield Security", tint = DangerRed, modifier = Modifier.size(36.dp))
            }

            Text(
                text = "BIOMETRIC AUTH REQUIRED",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = DangerRed,
                letterSpacing = 1.sp
            )

            Text(
                text = "Red Level 3 payments/settings change requests require fingerprint or Face ID confirmation.",
                style = MaterialTheme.typography.bodyMedium,
                color = SoftCyan,
                textAlign = TextAlign.Center
            )

            // Huge sensor touch button
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(DangerRed.copy(alpha = 0.2f))
                    .border(1.dp, DangerRed, CircleShape)
                    .clickable { onAuthorize() }
                    .testTag("biometric_fingerprint_sensor"),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Fingerprint, contentDescription = "Tap Sensor", tint = DangerRed, modifier = Modifier.size(48.dp))
            }

            Text(
                text = "TAP SENSOR TO AUTHORIZE",
                style = MaterialTheme.typography.labelSmall,
                color = MutedSlate,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSteel),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("Decline Request", color = SoftCyan)
            }
        }
    }
}

@Composable
fun ExecutionOverlayVisualizer(
    actionText: String,
    deviceState: SystemDeviceState,
    feedback: String,
    onDismiss: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeepCoal),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkSteel, RoundedCornerShape(24.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Determine icon or simulated app screen overlay!
                val lower = actionText.lowercase()

                when {
                    // Call mockup screen
                    lower.contains("call") || lower.contains("phone") || lower.contains("milao") -> {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(ActiveMint.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Call, contentDescription = "Ringing", tint = ActiveMint, modifier = Modifier.size(40.dp))
                        }
                        Text(
                            text = "Calling: ${deviceState.callingContact ?: "Papa"}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SoftCyan
                        )
                        Text(
                            text = "Simulating Hands-free GSM Call Dialer...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSlate
                        )
                    }

                    // WhatsApp Mockup screen
                    lower.contains("whatsapp") -> {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF25D366).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Chat, contentDescription = "WhatsApp message", tint = Color(0xFF25D366), modifier = Modifier.size(40.dp))
                        }
                        Text(
                            text = "WhatsApp message to Priya",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SoftCyan
                        )
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSteel),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        ) {
                            Text(
                                text = "Draft: \"Main aa raha hoon\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SoftCyan,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Camera finder mockup
                    lower.contains("camera") || lower.contains("photo") -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black)
                                .border(1.dp, MutedSlate, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Camera, contentDescription = "Camera viewfinder", tint = SoftCyan, modifier = Modifier.size(36.dp))
                        }
                        Text(
                            text = "Camera Active [Mock Viewfinder]",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftCyan
                        )
                    }

                    // WiFi Toggling step
                    lower.contains("wifi") -> {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(SunsetAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Settings, contentDescription = "Setting change", tint = SunsetAmber, modifier = Modifier.size(40.dp))
                        }
                        Text(
                            text = if (deviceState.wifiOn) "Toggling WiFi: ON" else "Toggling WiFi: OFF",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftCyan
                        )
                    }

                    // Brightness step
                    lower.contains("brightness") -> {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(ActiveMint.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.BrightnessHigh, contentDescription = "Brightness change", tint = ActiveMint, modifier = Modifier.size(40.dp))
                        }
                        Text(
                            text = "Setting system brightness: ${deviceState.brightnessPercent}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftCyan
                        )
                    }

                    // Money payment step
                    lower.contains("money") || lower.contains("send money") || lower.contains("pay") -> {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(ActiveMint.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.CreditCard, contentDescription = "Secured Payment", tint = ActiveMint, modifier = Modifier.size(40.dp))
                        }
                        Text(
                            text = "Secure UPI Payment Initiated",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SoftCyan
                        )
                        Text(
                            text = "Biometrics Verified • Local Sandboxed Transaction Successful!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ActiveMint,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Default system gear loading
                    else -> {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(SunsetAmber.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Android, contentDescription = "Processing OS Intent", tint = SunsetAmber, modifier = Modifier.size(40.dp))
                        }
                        Text(
                            text = "Executing System Command...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftCyan
                        )
                    }
                }

                Text(
                    text = feedback,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSlate,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                CircularProgressIndicator(
                    strokeWidth = 3.dp,
                    color = SunsetAmber,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
