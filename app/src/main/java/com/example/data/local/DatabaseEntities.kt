package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "command_history")
data class CommandEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val commandText: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSuccess: Boolean,
    val outputMessage: String
)

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val triggerType: String, // TIME, LOCATION, EVENT, VOICE
    val triggerValue: String, // e.g. "07:00", "Office", "Headphones Connected"
    val actionsJson: String, // e.g. '["Alarm band karo", "Weather batao", "Silent Mode"]'
    val isEnabled: Boolean = true
)
