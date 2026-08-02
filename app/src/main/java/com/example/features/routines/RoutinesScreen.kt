package com.example.features.routines

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.RoutineEntity
import com.example.features.dashboard.BoloViewModel
import com.example.ui.theme.*
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesScreen(
    viewModel: BoloViewModel,
    modifier: Modifier = Modifier
) {
    val routines by viewModel.allRoutines.collectAsStateWithLifecycle()
    val editingRoutine by viewModel.editingRoutine.collectAsStateWithLifecycle()

    var showCreator by remember { mutableStateOf(false) }

    // Creator State Parameters
    var name by remember { mutableStateOf("") }
    var triggerType by remember { mutableStateOf("TIME") } // TIME, LOCATION, EVENT, VOICE
    var triggerValue by remember { mutableStateOf("") }
    var currentActionText by remember { mutableStateOf("") }
    val actionsList = remember { mutableStateListOf<String>() }

    // Sync editing state when editingRoutine changes
    LaunchedEffect(editingRoutine) {
        if (editingRoutine != null) {
            val r = editingRoutine!!
            name = r.name
            triggerType = r.triggerType
            triggerValue = r.triggerValue
            actionsList.clear()
            try {
                val array = JSONArray(r.actionsJson)
                for (i in 0 until array.length()) {
                    actionsList.add(array.getString(i))
                }
            } catch (e: Exception) {
                // fallback
            }
            showCreator = true
        } else {
            if (!showCreator) {
                name = ""
                triggerType = "TIME"
                triggerValue = ""
                actionsList.clear()
            }
        }
    }

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
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 16.dp, bottom = 12.dp)
            ) {
                Text(
                    text = "Routine Manager (\"Riwaz\")",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = SoftCyan
                )
                Text(
                    text = "Combine triggers and automated actions offline",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSlate
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (showCreator) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DeepCoal),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SunsetAmber.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .testTag("routine_creator_panel")
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
                                        text = if (editingRoutine != null) "Edit Routine" else "Create New Routine",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SunsetAmber
                                    )
                                    IconButton(onClick = {
                                        showCreator = false
                                        viewModel.setEditingRoutine(null)
                                    }) {
                                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = SoftCyan)
                                    }
                                }

                                Divider(color = DarkSteel)

                                // Name Input
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Routine Name", color = MutedSlate) },
                                    modifier = Modifier.fillMaxWidth().testTag("routine_name_input"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = SoftCyan,
                                        unfocusedTextColor = SoftCyan,
                                        focusedBorderColor = SunsetAmber,
                                        unfocusedBorderColor = DarkSteel
                                    ),
                                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                                )

                                // Trigger type selector
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Select Trigger Type",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MutedSlate,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val types = listOf("TIME", "LOCATION", "EVENT", "VOICE")
                                        types.forEach { t ->
                                            val selected = triggerType == t
                                            Surface(
                                                onClick = {
                                                    triggerType = t
                                                    if (triggerValue.isEmpty()) {
                                                        triggerValue = when (t) {
                                                            "TIME" -> "08:30 AM"
                                                            "LOCATION" -> "Office"
                                                            "EVENT" -> "Headphones Plugged"
                                                            "VOICE" -> "Hey Bolo, Ghar aa gaya"
                                                            else -> ""
                                                        }
                                                    }
                                                },
                                                color = if (selected) SunsetAmber else DarkSteel,
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, if (selected) SunsetAmber else DarkSteel),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = t,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (selected) MidnightSlate else SoftCyan,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(vertical = 10.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Trigger Value input
                                OutlinedTextField(
                                    value = triggerValue,
                                    onValueChange = { triggerValue = it },
                                    label = {
                                        Text(
                                            text = when (triggerType) {
                                                "TIME" -> "Time (e.g. 07:00 AM)"
                                                "LOCATION" -> "Location name (e.g. Office, Home)"
                                                "EVENT" -> "Sensor Event (e.g. Headphones, Power)"
                                                else -> "Voice phrase trigger"
                                            },
                                            color = MutedSlate
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("routine_trigger_input"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = SoftCyan,
                                        unfocusedTextColor = SoftCyan,
                                        focusedBorderColor = SunsetAmber,
                                        unfocusedBorderColor = DarkSteel
                                    )
                                )

                                Divider(color = DarkSteel)

                                // Actions step builder
                                Text(
                                    text = "Automated Commands (Executed sequentially)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SoftCyan,
                                    fontWeight = FontWeight.Bold
                                )

                                // Action builder item list
                                if (actionsList.isEmpty()) {
                                    Text(
                                        text = "Abhi koi action steps nahi hain. Niche add karein.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedSlate,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        actionsList.forEachIndexed { index, act ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(DarkSteel)
                                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${index + 1}. $act",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = SoftCyan
                                                )
                                                IconButton(
                                                    onClick = { actionsList.removeAt(index) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Filled.Delete, contentDescription = "Delete step", tint = DangerRed, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }

                                // Step adder field
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = currentActionText,
                                        onValueChange = { currentActionText = it },
                                        placeholder = { Text("e.g. WiFi band karo", color = MutedSlate) },
                                        modifier = Modifier.weight(1f).testTag("action_step_input"),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = SoftCyan,
                                            unfocusedTextColor = SoftCyan,
                                            focusedBorderColor = SunsetAmber,
                                            unfocusedBorderColor = DarkSteel
                                        )
                                    )

                                    Button(
                                        onClick = {
                                            if (currentActionText.isNotBlank()) {
                                                actionsList.add(currentActionText.trim())
                                                currentActionText = ""
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SunsetAmber),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(56.dp)
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "Add Step", tint = MidnightSlate)
                                    }
                                }

                                Divider(color = DarkSteel)

                                // Save & Cancel button row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (editingRoutine != null) {
                                        Button(
                                            onClick = {
                                                viewModel.deleteRoutine(editingRoutine!!)
                                                showCreator = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Delete", color = Color.White)
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            if (name.isNotBlank() && actionsList.isNotEmpty()) {
                                                viewModel.saveRoutine(
                                                    name = name,
                                                    triggerType = triggerType,
                                                    triggerValue = triggerValue,
                                                    actionsList = actionsList.toList()
                                                )
                                                showCreator = false
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ActiveMint),
                                        modifier = Modifier.weight(1.5f),
                                        enabled = name.isNotBlank() && actionsList.isNotEmpty()
                                    ) {
                                        Text("Save Routine", color = MidnightSlate, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                if (routines.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = SunsetAmber)
                            Text("Routines data sync...", color = MutedSlate)
                        }
                    }
                } else {
                    items(routines) { routine ->
                        RoutineItemRow(
                            routine = routine,
                            onToggle = { viewModel.toggleRoutineEnabled(routine) },
                            onEdit = { viewModel.setEditingRoutine(routine) }
                        )
                    }
                }
            }
        }

        // Floating Action Button to create a routine
        if (!showCreator) {
            FloatingActionButton(
                onClick = {
                    viewModel.setEditingRoutine(null)
                    name = ""
                    triggerType = "TIME"
                    triggerValue = "08:30 AM"
                    actionsList.clear()
                    showCreator = true
                },
                containerColor = SunsetAmber,
                contentColor = MidnightSlate,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 100.dp)
                    .testTag("create_routine_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Routine")
            }
        }
    }
}

@Composable
fun RoutineItemRow(
    routine: RoutineEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit
) {
    val actions = remember(routine.actionsJson) {
        try {
            val list = mutableListOf<String>()
            val array = JSONArray(routine.actionsJson)
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
            list
        } catch (e: Exception) {
            emptyList<String>()
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DeepCoal),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (routine.isEnabled) DarkSteel else DarkSteel.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SunsetAmber.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (routine.triggerType) {
                                "TIME" -> Icons.Filled.AccessTime
                                "LOCATION" -> Icons.Filled.Place
                                "EVENT" -> Icons.Filled.Power
                                else -> Icons.Filled.RecordVoiceOver
                            },
                            contentDescription = routine.triggerType,
                            tint = SunsetAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = routine.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (routine.isEnabled) SoftCyan else MutedSlate
                        )
                        Text(
                            text = "${routine.triggerType}: ${routine.triggerValue}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedSlate
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = MutedSlate, modifier = Modifier.size(20.dp))
                    }

                    Switch(
                        checked = routine.isEnabled,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ActiveMint,
                            checkedTrackColor = ActiveMint.copy(alpha = 0.3f),
                            uncheckedThumbColor = MutedSlate,
                            uncheckedTrackColor = DarkSteel
                        ),
                        modifier = Modifier.scale(0.85f)
                    )
                }
            }

            Divider(color = DarkSteel)

            // Step tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Steps:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedSlate,
                    fontWeight = FontWeight.Bold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(actions) { action ->
                        Surface(
                            color = DarkSteel,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = action,
                                style = MaterialTheme.typography.labelSmall,
                                color = SoftCyan,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
