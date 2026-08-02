package com.example.data.repository

import com.example.data.local.BoloDao
import com.example.data.local.CommandEntity
import com.example.data.local.RoutineEntity
import kotlinx.coroutines.flow.Flow

class BoloRepository(private val boloDao: BoloDao) {

    val allCommands: Flow<List<CommandEntity>> = boloDao.getAllCommands()
    val recentCommands: Flow<List<CommandEntity>> = boloDao.getRecentCommands(10)
    val allRoutines: Flow<List<RoutineEntity>> = boloDao.getAllRoutines()

    suspend fun insertCommand(command: CommandEntity) {
        boloDao.insertCommand(command)
    }

    suspend fun clearAllCommands() {
        boloDao.clearAllCommands()
    }

    suspend fun insertRoutine(routine: RoutineEntity) {
        boloDao.insertRoutine(routine)
    }

    suspend fun updateRoutine(routine: RoutineEntity) {
        boloDao.updateRoutine(routine)
    }

    suspend fun deleteRoutineById(id: Int) {
        boloDao.deleteRoutineById(id)
    }

    suspend fun getEnabledRoutines(): List<RoutineEntity> {
        return boloDao.getEnabledRoutines()
    }
}
