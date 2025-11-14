package com.example.noteapp.data.repository

import androidx.lifecycle.LiveData
import com.example.noteapp.data.local.NoteDao
import com.example.noteapp.data.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * Repository class that abstracts access to multiple data sources
 * This is the single source of truth for note data
 */
class NoteRepository(private val noteDao: NoteDao) {
    
    // LiveData observations
    val allNotes: LiveData<List<Note>> = noteDao.getAllNotes()
    val archivedNotes: LiveData<List<Note>> = noteDao.getArchivedNotes()
    val activeNotesCount: LiveData<Int> = noteDao.getActiveNotesCount()
    
    /**
     * Insert a new note
     */
    suspend fun insert(note: Note): Long {
        return noteDao.insert(note)
    }
    
    /**
     * Update an existing note
     */
    suspend fun update(note: Note) {
        noteDao.update(note.copy(updatedAt = System.currentTimeMillis()))
    }
    
    /**
     * Delete a note
     */
    suspend fun delete(note: Note) {
        noteDao.delete(note)
    }
    
    /**
     * Get note by ID
     */
    suspend fun getNoteById(noteId: Long): Note? {
        return noteDao.getNoteById(noteId)
    }
    
    /**
     * Archive a note
     */
    suspend fun archiveNote(noteId: Long) {
        noteDao.archiveNote(noteId)
    }
    
    /**
     * Unarchive a note
     */
    suspend fun unarchiveNote(noteId: Long) {
        noteDao.unarchiveNote(noteId)
    }
    
    /**
     * Search notes
     */
    fun searchNotes(query: String): LiveData<List<Note>> {
        return noteDao.searchNotes(query)
    }
    
    /**
     * Get notes by priority
     */
    fun getNotesByPriority(priority: Note.Priority): LiveData<List<Note>> {
        return noteDao.getNotesByPriority(priority)
    }
    
    /**
     * Get notes sorted by urgency (smart sorting)
     */
    fun getNotesSortedByUrgency(): LiveData<List<Note>> {
        return noteDao.getNotesSortedByUrgency()
    }
    
    /**
     * Get overdue notes
     */
    fun getOverdueNotes(): LiveData<List<Note>> {
        return noteDao.getOverdueNotes()
    }
    
    /**
     * Get notes with upcoming reminders
     */
    fun getNotesWithReminders(): LiveData<List<Note>> {
        return noteDao.getNotesWithReminders()
    }
    
    /**
     * Get notes by tag
     */
    fun getNotesByTag(tag: String): LiveData<List<Note>> {
        return noteDao.getNotesByTag(tag)
    }
    
    /**
     * Get all notes as Flow
     */
    fun getAllNotesFlow(): Flow<List<Note>> {
        return noteDao.getAllNotesFlow()
    }
    
    /**
     * Batch operations
     */
    suspend fun insertAll(notes: List<Note>) {
        noteDao.insertAll(notes)
    }
    
    suspend fun deleteAll() {
        noteDao.deleteAll()
    }
}
