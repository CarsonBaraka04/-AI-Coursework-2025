package com.example.noteapp.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.noteapp.data.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Note entity
 */
@Dao
interface NoteDao {
    
    /**
     * Insert a new note
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note): Long
    
    /**
     * Insert multiple notes
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<Note>)
    
    /**
     * Update an existing note
     */
    @Update
    suspend fun update(note: Note)
    
    /**
     * Delete a note
     */
    @Delete
    suspend fun delete(note: Note)
    
    /**
     * Delete all notes
     */
    @Query("DELETE FROM notes")
    suspend fun deleteAll()
    
    /**
     * Get all notes (LiveData for observation)
     */
    @Query("SELECT * FROM notes WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllNotes(): LiveData<List<Note>>
    
    /**
     * Get all notes as Flow (alternative to LiveData)
     */
    @Query("SELECT * FROM notes WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllNotesFlow(): Flow<List<Note>>
    
    /**
     * Get archived notes
     */
    @Query("SELECT * FROM notes WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedNotes(): LiveData<List<Note>>
    
    /**
     * Get note by ID
     */
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Long): Note?
    
    /**
     * Get notes by priority
     */
    @Query("SELECT * FROM notes WHERE priority = :priority AND isArchived = 0 ORDER BY createdAt DESC")
    fun getNotesByPriority(priority: Note.Priority): LiveData<List<Note>>
    
    /**
     * Search notes by title or content
     */
    @Query("SELECT * FROM notes WHERE (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') AND isArchived = 0")
    fun searchNotes(query: String): LiveData<List<Note>>
    
    /**
     * Get notes with upcoming reminders
     */
    @Query("SELECT * FROM notes WHERE reminderTime > :currentTime AND isArchived = 0 ORDER BY reminderTime ASC")
    fun getNotesWithReminders(currentTime: Long = System.currentTimeMillis()): LiveData<List<Note>>
    
    /**
     * Get overdue notes
     */
    @Query("SELECT * FROM notes WHERE dueDate < :currentTime AND isArchived = 0 ORDER BY dueDate ASC")
    fun getOverdueNotes(currentTime: Long = System.currentTimeMillis()): LiveData<List<Note>>
    
    /**
     * Archive a note
     */
    @Query("UPDATE notes SET isArchived = 1, updatedAt = :timestamp WHERE id = :noteId")
    suspend fun archiveNote(noteId: Long, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Unarchive a note
     */
    @Query("UPDATE notes SET isArchived = 0, updatedAt = :timestamp WHERE id = :noteId")
    suspend fun unarchiveNote(noteId: Long, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Get notes sorted by urgency (smart sorting)
     */
    @Query("""
        SELECT * FROM notes 
        WHERE isArchived = 0 
        ORDER BY 
            CASE 
                WHEN dueDate < :currentTime THEN 1000 + priority
                WHEN dueDate < :oneDayFromNow THEN 500 + priority
                WHEN dueDate < :threeDaysFromNow THEN 250 + priority
                ELSE priority
            END DESC,
            createdAt DESC
    """)
    fun getNotesSortedByUrgency(
        currentTime: Long = System.currentTimeMillis(),
        oneDayFromNow: Long = System.currentTimeMillis() + 24 * 60 * 60 * 1000,
        threeDaysFromNow: Long = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000
    ): LiveData<List<Note>>
    
    /**
     * Get notes by tag
     */
    @Query("SELECT * FROM notes WHERE tags LIKE '%' || :tag || '%' AND isArchived = 0")
    fun getNotesByTag(tag: String): LiveData<List<Note>>
    
    /**
     * Get count of active notes
     */
    @Query("SELECT COUNT(*) FROM notes WHERE isArchived = 0")
    fun getActiveNotesCount(): LiveData<Int>
}
