package com.example.noteapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Note entity representing a note in the database
 */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val title: String,
    
    val content: String,
    
    val createdAt: Long = System.currentTimeMillis(),
    
    val updatedAt: Long = System.currentTimeMillis(),
    
    val priority: Priority = Priority.MEDIUM,
    
    val isArchived: Boolean = false,
    
    val tags: String = "", // Comma-separated tags
    
    val dueDate: Long? = null, // Optional due date for tasks
    
    val reminderTime: Long? = null, // Optional reminder timestamp
    
    val color: Int? = null // Optional color for categorization
) {
    enum class Priority(val value: Int) {
        LOW(0),
        MEDIUM(1),
        HIGH(2),
        URGENT(3)
    }
    
    /**
     * Check if note is overdue
     */
    fun isOverdue(): Boolean {
        return dueDate?.let { it < System.currentTimeMillis() } ?: false
    }
    
    /**
     * Get formatted tags list
     */
    fun getTagsList(): List<String> {
        return tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
    
    /**
     * Calculate urgency score for smart sorting
     */
    fun getUrgencyScore(): Int {
        var score = priority.value * 10
        
        // Add urgency if due date is approaching
        dueDate?.let {
            val hoursUntilDue = (it - System.currentTimeMillis()) / (1000 * 60 * 60)
            when {
                hoursUntilDue < 0 -> score += 50 // Overdue
                hoursUntilDue < 24 -> score += 30 // Due within 24 hours
                hoursUntilDue < 72 -> score += 15 // Due within 3 days
            }
        }
        
        return score
    }
}
