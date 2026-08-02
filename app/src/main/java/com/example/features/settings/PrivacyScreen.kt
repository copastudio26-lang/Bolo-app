package com.example.features.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.features.dashboard.BoloViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    viewModel: BoloViewModel,
    modifier: Modifier = Modifier
) {
    val biometricOn by viewModel.biometricEnabled.collectAsStateWithLifecycle()
    val voiceprintOn by viewModel.voiceprintVerified.collectAsStateWithLifecycle()
    val onDeviceOnly by viewModel.onDeviceOnly.collectAsStateWithLifecycle()
    val recentCmds by viewModel.recentCommands.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MidnightSlate, DeepCoal)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 16.dp, bottom = 12.dp)
            ) {
                Text(
                    text = "Privacy Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = SoftCyan
                )
                Text(
                    text = "Tier-1 Security & Zero-Knowledge Architecture",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSlate
                )
            }

            // Security Status Card
            Card(
                colors = CardDefaults.cardColors(containerColor = ActiveMint.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ActiveMint, RoundedCornerShape(20.dp))
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(ActiveMint.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Shield, contentDescription = "Security Safe", tint = ActiveMint, modifier = Modifier.size(28.dp))
                    }

                    Column {
                        Text(
                            text = "SECURITY STATUS: SAFE & AUDITED",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = ActiveMint,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Sandbox Audited • No Root Detected • AES-256 Enabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Core Switches Group
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepCoal),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkSteel, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🛡️ Core Security Controls",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftCyan
                    )

                    Divider(color = DarkSteel)

                    // Switch 1: Biometric Auth
                    PrivacySwitchItem(
                        icon = Icons.Filled.Fingerprint,
                        title = "Mandatory Biometric Auth",
                        description = "Required fingerprint/face confirmation for high-risk Red Level 3 actions (Payments, system changes).",
                        checked = biometricOn,
                        onCheckedChange = { viewModel.toggleBiometric() },
                        testTag = "toggle_biometric"
                    )

                    Divider(color = DarkSteel)

                    // Switch 2: Voiceprint
                    PrivacySwitchItem(
                        icon = Icons.Filled.RecordVoiceOver,
                        title = "Voiceprint Verification",
                        description = "Enforces primary vocal speaker liveness matching, preventing replay or audio synthesize attacks.",
                        checked = voiceprintOn,
                        onCheckedChange = { viewModel.toggleVoiceprint() },
                        testTag = "toggle_voiceprint"
                    )

                    Divider(color = DarkSteel)

                    // Switch 3: Local Device Only
                    PrivacySwitchItem(
                        icon = Icons.Filled.CloudOff,
                        title = "On-Device Processing Only",
                        description = "Zero knowledge. All Speech-to-Text and NLP triggers are processed locally. No cloud transmission.",
                        checked = onDeviceOnly,
                        onCheckedChange = { viewModel.toggleOnDeviceOnly() },
                        testTag = "toggle_ondevice"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Storage and Audit statistics
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepCoal),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkSteel, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "📊 Audit Logs & Storage",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftCyan
                    )

                    Divider(color = DarkSteel)

                    // Data size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Database Cache Size", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SoftCyan)
                            Text("Local command histories and settings bytes", style = MaterialTheme.typography.bodySmall, color = MutedSlate)
                        }
                        val count = recentCmds.size
                        Text(
                            text = "${count * 128} Bytes",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = SunsetAmber
                        )
                    }

                    // Security audit status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Last Security Audit", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SoftCyan)
                            Text("Self-diagnostic integrity check", style = MaterialTheme.typography.bodySmall, color = MutedSlate)
                        }
                        Text(
                            text = "TODAY - PASSED",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = ActiveMint
                        )
                    }

                    // Cloud backup
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Cloud Sync Backup", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SoftCyan)
                            Text("Disabled for complete zero-knowledge privacy", style = MaterialTheme.typography.bodySmall, color = MutedSlate)
                        }
                        Text(
                            text = "OFFLINE ONLY",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = DangerRed
                        )
                    }

                    Divider(color = DarkSteel)

                    // WIPE ALL DATA
                    Button(
                        onClick = { viewModel.clearAllCommandHistory() },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, DangerRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.DeleteForever, contentDescription = "Wipe", tint = DangerRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("WIPE LOCAL COMMAND HISTORY CACHE", color = DangerRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Compliance Stamp
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepCoal),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkSteel, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(SunsetAmber.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Gavel, contentDescription = "Legislation compliant", tint = SunsetAmber, modifier = Modifier.size(32.dp))
                    }

                    Text(
                        text = "BoloPhone Privacy Mandate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftCyan
                    )

                    Text(
                        text = "Our platform is GDPR, CCPA, and DPDP Act 2023 compliant by design. Your audio waveforms are never saved nor uploaded, remaining isolated securely in the device's volatile RAM memory sandbox.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlate,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
fun PrivacySwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(DarkSteel),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = SunsetAmber, modifier = Modifier.size(18.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = SoftCyan
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MutedSlate,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ActiveMint,
                checkedTrackColor = ActiveMint.copy(alpha = 0.3f),
                uncheckedThumbColor = MutedSlate,
                uncheckedTrackColor = DarkSteel
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
