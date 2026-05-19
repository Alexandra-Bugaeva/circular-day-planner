package com.example.circledayplanner.data.local

import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.circledayplanner.data.model.EventEntity
import com.example.circledayplanner.data.model.TodoTaskEntity

@Database(
    entities = [EventEntity::class, TodoTaskEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun todoTaskDao(): TodoTaskDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS events_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        startDate TEXT NOT NULL,
                        startTime TEXT NOT NULL,
                        endDate TEXT NOT NULL,
                        endTime TEXT NOT NULL,
                        color INTEGER NOT NULL,
                        location TEXT NOT NULL,
                        notes TEXT NOT NULL,
                        isCompleted INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO events_new (id, title, startDate, startTime, endDate, endTime, color, location, notes, isCompleted)
                    SELECT id, title, date, startTime, date, endTime, color, location, notes, isCompleted FROM events
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE events")
                db.execSQL("ALTER TABLE events_new RENAME TO events")
            }
        }
    }
}
