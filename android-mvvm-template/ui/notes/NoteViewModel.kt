package com.example.noteapp.ui.notes

import android.app.Application
import androidx.lifecycle.*
import com.example.noteapp.data.local.NoteDatabase
import com.example.noteapp.data.model.Note
import com.example.noteapp.data.repository.NoteRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for managing Note data
 * Survives configuration changes and manages UI-related data
 */
class NoteViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: NoteRepository
    
    // LiveData for observing notes
    val allNotes: LiveData<List<Note>>
    val archivedNotes: LiveData<List<Note>>
    val activeNotesCount: LiveData<Int>
    
    // Sorting options
    private val _sortOption = MutableLiveData(SortOption.DATE_CREATED)
    val sortOption: LiveData<SortOption> = _sortOption
    
    // Search query
    private val _searchQuery = MutableLiveData<String>()
    
    // Filtered and sorted notes
    val displayedNotes: LiveData<List<Note>>
    
    init {
        val noteDao = NoteDatabase.getDatabase(application, viewModelScope).noteDao()
        repository = NoteRepository(noteDao)
        allNotes = repository.allNotes
        archivedNotes = repository.archivedNotes
        activeNotesCount = repository.activeNotesCount
        
        // Combine search and sort
        displayedNotes = MediatorLiveData<List<Note>>().apply {
            var notes: List<Note>? = null
            var query: String? = null
            var sort: SortOption? = null
            
            addSource(allNotes) { notesList ->
                notes = notesList
                value = applyFiltersAndSort(notesList, query, sort)
            }
            
            addSource(_searchQuery) { searchQuery ->
                query = searchQuery
                value = applyFiltersAndSort(notes, searchQuery, sort)
            }
            
            addSource(_sortOption) { sortOpt ->
                sort = sortOpt
                value = applyFiltersAndSort(notes, query, sortOpt)
            }
        }
    }
    
    /**
     * Apply search filter and sorting
     */
    private fun applyFiltersAndSort(
        notes: List<Note>?,
        query: String?,
        sort: SortOption?
    ): List<Note> {
        var result = notes ?: emptyList()
        
        // Apply search filter
        if (!query.isNullOrBlank()) {
            result = result.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.content.contains(query, ignoreCase = true) ||
                it.tags.contains(query, ignoreCase = true)
            }
        }
        
        // Apply sorting
        result = when (sort ?: SortOption.DATE_CREATED) {
            SortOption.DATE_CREATED -> result.sortedByDescending { it.createdAt }
            SortOption.DATE_UPDATED -> result.sortedByDescending { it.updatedAt }
            SortOption.TITLE -> result.sortedBy { it.title.lowercase() }
            SortOption.PRIORITY -> result.sortedByDescending { it.priority.value }
            SortOption.URGENCY -> result.sortedByDescending { it.getUrgencyScore() }
            SortOption.DUE_DATE -> result.sortedBy { it.dueDate ?: Long.MAX_VALUE }
        }
        
        return result
    }
    
    /**
     * Insert a new note
     */
    fun insert(note: Note) = viewModelScope.launch {
        repository.insert(note)
    }
    
    /**
     * Update an existing note
     */
    fun update(note: Note) = viewModelScope.launch {
        repository.update(note)
    }
    
    /**
     * Delete a note
     */
    fun delete(note: Note) = viewModelScope.launch {
        repository.delete(note)
    }
    
    /**
     * Archive a note
     */
    fun archiveNote(noteId: Long) = viewModelScope.launch {
        repository.archiveNote(noteId)
    }
    
    /**
     * Unarchive a note
     */
    fun unarchiveNote(noteId: Long) = viewModelScope.launch {
        repository.unarchiveNote(noteId)
    }
    
    /**
     * Get note by ID
     */
    suspend fun getNoteById(noteId: Long): Note? {
        return repository.getNoteById(noteId)
    }
    
    /**
     * Set search query
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * Set sort option
     */
    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }
    
    /**
     * Get overdue notes
     */
    fun getOverdueNotes(): LiveData<List<Note>> {
        return repository.getOverdueNotes()
    }
    
    /**
     * Get notes with reminders
     */
    fun getNotesWithReminders(): LiveData<List<Note>> {
        return repository.getNotesWithReminders()
    }
    
    /**
     * Get notes by tag
     */
    fun getNotesByTag(tag: String): LiveData<List<Note>> {
        return repository.getNotesByTag(tag)
    }
    
    /**
     * Sorting options enum
     */
    enum class SortOption {
        DATE_CREATED,
        DATE_UPDATED,
        TITLE,
        PRIORITY,
        URGENCY,
        DUE_DATE
    }
}

/**
 * Factory for creating NoteViewModel with Application parameter
 */
class NoteViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
