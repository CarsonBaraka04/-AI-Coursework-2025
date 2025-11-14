package com.example.noteapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.noteapp.data.model.Note
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Room Database class for the Note app
 */
@Database(
    entities = [Note::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NoteDatabase : RoomDatabase() {
    
    abstract fun noteDao(): NoteDao
    
    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null
        
        /**
         * Get database instance (Singleton pattern)
         */
        fun getDatabase(
            context: Context,
            scope: CoroutineScope? = null
        ): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                )
                    .addCallback(NoteDatabaseCallback(scope))
                    .fallbackToDestructiveMigration() // For development only
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Callback to populate database with sample data on creation
         */
        private class NoteDatabaseCallback(
            private val scope: CoroutineScope?
        ) : RoomDatabase.Callback() {
            
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope?.launch(Dispatchers.IO) {
                        populateDatabase(database.noteDao())
                    }
                }
            }
            
            /**
             * Populate database with sample notes
             */
            suspend fun populateDatabase(noteDao: NoteDao) {
                // Clear existing data
                noteDao.deleteAll()
                
                // Add sample notes
                val sampleNotes = listOf(
                    Note(
                        title = "Welcome to Notes!",
                        content = "This is your first note. You can edit or delete it anytime.",
                        priority = Note.Priority.MEDIUM,
                        tags = "welcome,tutorial"
                    ),
                    Note(
                        title = "Grocery Shopping",
                        content = "Milk, Eggs, Bread, Butter, Coffee",
                        priority = Note.Priority.HIGH,
                        tags = "shopping,personal",
                        dueDate = System.currentTimeMillis() + 2 * 24 * 60 * 60 * 1000 // 2 days from now
                    ),
                    Note(
                        title = "Project Meeting",
                        content = "Discuss Q4 roadmap and sprint planning",
                        priority = Note.Priority.URGENT,
                        tags = "work,meeting",
                        dueDate = System.currentTimeMillis() + 24 * 60 * 60 * 1000, // Tomorrow
                        reminderTime = System.currentTimeMillis() + 23 * 60 * 60 * 1000 // 23 hours from now
                    )
                )
                
                noteDao.insertAll(sampleNotes)
            }
        }
    }
}

/**
 * Type converters for Room database
 */
class Converters {
    @androidx.room.TypeConverter
    fun fromPriority(priority: Note.Priority): Int {
        return priority.value
    }
    
    @androidx.room.TypeConverter
    fun toPriority(value: Int): Note.Priority {
        return Note.Priority.values().find { it.value == value } ?: Note.Priority.MEDIUM
    }
}
