package com.example.noteapp.ui.notes

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.data.model.Note
import java.text.SimpleDateFormat
import java.util.*

/**
 * RecyclerView Adapter for displaying notes
 * Uses ListAdapter with DiffUtil for efficient updates
 */
class NoteAdapter(
    private val onNoteClick: (Note) -> Unit,
    private val onNoteLongClick: (Note) -> Boolean
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = createNoteItemView(parent)
        return NoteViewHolder(view, onNoteClick, onNoteLongClick)
    }
    
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    /**
     * Create note item view programmatically
     * In a real app, you would inflate from XML layout
     */
    private fun createNoteItemView(parent: ViewGroup): View {
        val context = parent.context
        
        // Create card-like container
        val cardView = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 8, 16, 8)
            }
            setBackgroundColor(Color.WHITE)
            elevation = 4f
        }
        
        // Title TextView
        val titleView = TextView(context).apply {
            id = View.generateViewId()
            textSize = 18f
            setTextColor(Color.BLACK)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        
        // Content TextView
        val contentView = TextView(context).apply {
            id = View.generateViewId()
            textSize = 14f
            setTextColor(Color.DKGRAY)
            maxLines = 3
        }
        
        // Metadata TextView (date, priority, tags)
        val metadataView = TextView(context).apply {
            id = View.generateViewId()
            textSize = 12f
            setTextColor(Color.GRAY)
        }
        
        // Priority indicator
        val priorityIndicator = View(context).apply {
            id = View.generateViewId()
            layoutParams = ViewGroup.LayoutParams(8, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        
        // Add views to card
        cardView.addView(titleView)
        cardView.addView(contentView)
        cardView.addView(metadataView)
        
        // Wrap card with priority indicator
        val container = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        
        container.addView(priorityIndicator)
        container.addView(cardView)
        
        // Store view IDs as tags for ViewHolder
        container.tag = ViewIds(
            titleView.id,
            contentView.id,
            metadataView.id,
            priorityIndicator.id
        )
        
        return container
    }
    
    /**
     * ViewHolder for note items
     */
    class NoteViewHolder(
        itemView: View,
        private val onNoteClick: (Note) -> Unit,
        private val onNoteLongClick: (Note) -> Boolean
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val viewIds = itemView.tag as ViewIds
        private val titleView: TextView = itemView.findViewById(viewIds.titleId)
        private val contentView: TextView = itemView.findViewById(viewIds.contentId)
        private val metadataView: TextView = itemView.findViewById(viewIds.metadataId)
        private val priorityIndicator: View = itemView.findViewById(viewIds.priorityId)
        
        private var currentNote: Note? = null
        
        init {
            itemView.setOnClickListener {
                currentNote?.let { onNoteClick(it) }
            }
            
            itemView.setOnLongClickListener {
                currentNote?.let { onNoteLongClick(it) } ?: false
            }
        }
        
        fun bind(note: Note) {
            currentNote = note
            
            titleView.text = note.title
            contentView.text = note.content
            
            // Format metadata
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val dateStr = dateFormat.format(Date(note.createdAt))
            
            val metadata = buildString {
                append(dateStr)
                append(" • ")
                append(note.priority.name)
                
                if (note.tags.isNotEmpty()) {
                    append(" • ")
                    append(note.getTagsList().joinToString(", "))
                }
                
                if (note.isOverdue()) {
                    append(" • OVERDUE")
                }
            }
            
            metadataView.text = metadata
            
            // Set priority indicator color
            priorityIndicator.setBackgroundColor(getPriorityColor(note.priority))
            
            // Highlight overdue notes
            if (note.isOverdue()) {
                itemView.setBackgroundColor(Color.parseColor("#FFEBEE"))
            } else {
                itemView.setBackgroundColor(Color.WHITE)
            }
        }
        
        private fun getPriorityColor(priority: Note.Priority): Int {
            return when (priority) {
                Note.Priority.LOW -> Color.parseColor("#4CAF50")      // Green
                Note.Priority.MEDIUM -> Color.parseColor("#2196F3")   // Blue
                Note.Priority.HIGH -> Color.parseColor("#FF9800")     // Orange
                Note.Priority.URGENT -> Color.parseColor("#F44336")   // Red
            }
        }
    }
    
    /**
     * Data class to store view IDs
     */
    private data class ViewIds(
        val titleId: Int,
        val contentId: Int,
        val metadataId: Int,
        val priorityId: Int
    )
}

/**
 * DiffUtil callback for efficient list updates
 */
class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem == newItem
    }
}
