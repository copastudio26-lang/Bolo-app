package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [CommandEntity::class, RoutineEntity::class], version = 1, exportSchema = false)
abstract class BoloDatabase : RoomDatabase() {
    abstract fun boloDao(): BoloDao

    companion object {
        @Volatile
        private var INSTANCE: BoloDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): BoloDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BoloDatabase::class.java,
                    "bolophone_database"
                )
                .addCallback(BoloDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class BoloDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.boloDao())
                }
            }
        }

        suspend fun populateDatabase(boloDao: BoloDao) {
            // Pre-populate with default routines from the spec
            boloDao.insertRoutine(
                RoutineEntity(
                    name = "Good Morning",
                    triggerType = "TIME",
                    triggerValue = "07:00 AM",
                    actionsJson = """["Alarm band karo", "Weather batao", "Calendar summary", "News sunao"]""",
                    isEnabled = true
                )
            )
            boloDao.insertRoutine(
                RoutineEntity(
                    name = "Driving Mode",
                    triggerType = "EVENT",
                    triggerValue = "Bluetooth Connected (Car)",
                    actionsJson = """["Do Not Disturb on", "Maps navigation start", "WhatsApp auto-reply on", "Music chalao"]""",
                    isEnabled = true
                )
            )
            boloDao.insertRoutine(
                RoutineEntity(
                    name = "Office Mode",
                    triggerType = "LOCATION",
                    triggerValue = "Office",
                    actionsJson = """["Silent mode", "WiFi band karo", "Bluetooth chalu karo"]""",
                    isEnabled = false
                )
            )
        }
    }
}
