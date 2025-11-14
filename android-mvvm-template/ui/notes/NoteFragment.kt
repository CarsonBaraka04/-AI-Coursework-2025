package com.example.noteapp.ui.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.data.model.Note
import com.example.noteapp.util.SwipeToArchiveCallback
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Fragment that displays a list of notes
 * Observes LiveData from ViewModel and updates UI accordingly
 */
class NoteFragment : Fragment() {
    
    private val viewModel: NoteViewModel by viewModels()
    private lateinit var adapter: NoteAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddNote: FloatingActionButton
    private lateinit var searchView: SearchView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // In a real app, you would inflate a layout XML file
        // For this template, we'll create views programmatically
        return createLayout()
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupObservers()
        setupSwipeToArchive()
        setupFab()
        setupSearch()
    }
    
    /**
     * Create layout programmatically (in real app, use XML layout)
     */
    private fun createLayout(): View {
        val context = requireContext()
        
        // Create root layout
        val rootLayout = android.widget.FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        
        // Create vertical LinearLayout for search and recycler
        val verticalLayout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        
        // Create SearchView
        searchView = SearchView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            queryHint = "Search notes..."
        }
        
        // Create RecyclerView
        recyclerView = RecyclerView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        
        // Create FAB
        fabAddNote = FloatingActionButton(context).apply {
            val params = android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.BOTTOM or android.view.Gravity.END
                setMargins(0, 0, 48, 48)
            }
            layoutParams = params
        }
        
        verticalLayout.addView(searchView)
        verticalLayout.addView(recyclerView)
        rootLayout.addView(verticalLayout)
        rootLayout.addView(fabAddNote)
        
        return rootLayout
    }
    
    /**
     * Setup RecyclerView with adapter and layout manager
     */
    private fun setupRecyclerView() {
        adapter = NoteAdapter(
            onNoteClick = { note ->
                // Handle note click - navigate to detail/edit screen
                Toast.makeText(context, "Clicked: ${note.title}", Toast.LENGTH_SHORT).show()
            },
            onNoteLongClick = { note ->
                // Handle long click - show options menu
                showNoteOptions(note)
                true
            }
        )
        
        recyclerView.apply {
            this.adapter = this@NoteFragment.adapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }
    }
    
    /**
     * Setup LiveData observers
     */
    private fun setupObservers() {
        // Observe displayed notes (filtered and sorted)
        viewModel.displayedNotes.observe(viewLifecycleOwner) { notes ->
            adapter.submitList(notes)
        }
        
        // Observe active notes count
        viewModel.activeNotesCount.observe(viewLifecycleOwner) { count ->
            // Update UI with count (e.g., in toolbar subtitle)
            activity?.title = "Notes ($count)"
        }
        
        // Observe overdue notes for notifications
        viewModel.getOverdueNotes().observe(viewLifecycleOwner) { overdueNotes ->
            if (overdueNotes.isNotEmpty()) {
                // Show notification or badge
                showOverdueNotification(overdueNotes.size)
            }
        }
    }
    
    /**
     * Setup swipe to archive gesture
     */
    private fun setupSwipeToArchive() {
        val swipeCallback = SwipeToArchiveCallback(requireContext()) { position ->
            val note = adapter.currentList[position]
            viewModel.archiveNote(note.id)
            
            Toast.makeText(
                context,
                "${note.title} archived",
                Toast.LENGTH_SHORT
            ).show()
        }
        
        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
    
    /**
     * Setup FAB click listener
     */
    private fun setupFab() {
        fabAddNote.setOnClickListener {
            // Navigate to add note screen or show dialog
            createNewNote()
        }
    }
    
    /**
     * Setup search functionality
     */
    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })
    }
    
    /**
     * Create a new note
     */
    private fun createNewNote() {
        val newNote = Note(
            title = "New Note",
            content = "Enter your note content here...",
            priority = Note.Priority.MEDIUM
        )
        viewModel.insert(newNote)
        Toast.makeText(context, "Note created", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Show options for a note
     */
    private fun showNoteOptions(note: Note) {
        // In a real app, show a bottom sheet or dialog with options
        val options = arrayOf("Edit", "Delete", "Archive", "Change Priority")
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(note.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editNote(note)
                    1 -> deleteNote(note)
                    2 -> archiveNote(note)
                    3 -> changePriority(note)
                }
            }
            .show()
    }
    
    private fun editNote(note: Note) {
        // Navigate to edit screen
        Toast.makeText(context, "Edit: ${note.title}", Toast.LENGTH_SHORT).show()
    }
    
    private fun deleteNote(note: Note) {
        viewModel.delete(note)
        Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show()
    }
    
    private fun archiveNote(note: Note) {
        viewModel.archiveNote(note.id)
        Toast.makeText(context, "Note archived", Toast.LENGTH_SHORT).show()
    }
    
    private fun changePriority(note: Note) {
        val priorities = Note.Priority.values()
        val priorityNames = priorities.map { it.name }.toTypedArray()
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Priority")
            .setItems(priorityNames) { _, which ->
                val updatedNote = note.copy(priority = priorities[which])
                viewModel.update(updatedNote)
                Toast.makeText(context, "Priority updated", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
    
    private fun showOverdueNotification(count: Int) {
        // Show a subtle notification about overdue notes
        Toast.makeText(
            context,
            "You have $count overdue note(s)",
            Toast.LENGTH_LONG
        ).show()
    }
    
    /**
     * Change sort option (call this from menu or UI control)
     */
    fun changeSortOption(option: NoteViewModel.SortOption) {
        viewModel.setSortOption(option)
    }
}
