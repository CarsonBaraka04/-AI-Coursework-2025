# Bonus Features for Task Management App

This document outlines unique and innovative features that go beyond standard task management functionality.

## 1. Smart Sorting & Prioritization

### Urgency-Based Sorting
The app includes an intelligent urgency scoring system that considers multiple factors:

```kotlin
fun getUrgencyScore(): Int {
    var score = priority.value * 10
    
    dueDate?.let {
        val hoursUntilDue = (it - System.currentTimeMillis()) / (1000 * 60 * 60)
        when {
            hoursUntilDue < 0 -> score += 50      // Overdue
            hoursUntilDue < 24 -> score += 30     // Due within 24 hours
            hoursUntilDue < 72 -> score += 15     // Due within 3 days
        }
    }
    
    return score
}
```

**Features:**
- Automatically surfaces overdue tasks
- Prioritizes tasks due soon
- Combines manual priority with time-based urgency
- Dynamic reordering as deadlines approach

### Multiple Sort Options
- Date Created (newest first)
- Date Updated (recently modified)
- Title (alphabetical)
- Priority (high to low)
- **Urgency Score** (smart sorting)
- Due Date (soonest first)

## 2. Time-Based Notifications & Reminders

### Smart Reminder System
```kotlin
// In Note entity
val reminderTime: Long? = null  // Optional reminder timestamp
val dueDate: Long? = null       // Optional due date

// Query for upcoming reminders
@Query("SELECT * FROM notes WHERE reminderTime > :currentTime AND isArchived = 0 ORDER BY reminderTime ASC")
fun getNotesWithReminders(currentTime: Long): LiveData<List<Note>>
```

**Implementation Ideas:**
1. **WorkManager Integration** - Schedule background work for reminders
2. **Notification Channels** - Separate channels for different priority levels
3. **Smart Timing** - Suggest reminder times based on due date
4. **Recurring Reminders** - Support for daily/weekly tasks

### Example WorkManager Setup:
```kotlin
class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val noteId = inputData.getLong("NOTE_ID", -1)
        // Show notification for the note
        showNotification(noteId)
        return Result.success()
    }
}

// Schedule reminder
fun scheduleReminder(note: Note) {
    val delay = note.reminderTime!! - System.currentTimeMillis()
    val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(workDataOf("NOTE_ID" to note.id))
        .build()
    
    WorkManager.getInstance(context).enqueue(workRequest)
}
```

## 3. Gesture Controls

### Swipe to Archive
Already implemented in `SwipeToArchiveCallback.kt`

**Features:**
- Visual feedback with colored background
- Icon indication of action
- Configurable swipe threshold
- Smooth animations

### Additional Gesture Ideas:

#### Double-Tap to Complete
```kotlin
class DoubleTapGestureDetector(
    private val onDoubleTap: () -> Unit
) : GestureDetector.SimpleOnGestureListener() {
    override fun onDoubleTap(e: MotionEvent): Boolean {
        onDoubleTap()
        return true
    }
}
```

#### Long-Press for Quick Actions
Already implemented in `NoteFragment.kt` - shows options menu

#### Pinch to Change View Mode
```kotlin
class PinchGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
    override fun onScale(detector: ScaleGestureDetector): Boolean {
        if (detector.scaleFactor > 1.2f) {
            // Zoom in - show detailed view
        } else if (detector.scaleFactor < 0.8f) {
            // Zoom out - show compact view
        }
        return true
    }
}
```

## 4. Integration with Other Apps

### Calendar Integration
```kotlin
// Add note to calendar
fun addToCalendar(note: Note) {
    val intent = Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, note.title)
        putExtra(CalendarContract.Events.DESCRIPTION, note.content)
        note.dueDate?.let {
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, it)
        }
    }
    startActivity(intent)
}
```

### Email/Share Integration
```kotlin
fun shareNote(note: Note) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, note.title)
        putExtra(Intent.EXTRA_TEXT, note.content)
    }
    startActivity(Intent.createChooser(shareIntent, "Share note via"))
}
```

### Voice Input Integration
```kotlin
fun startVoiceInput() {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your note")
    }
    startActivityForResult(intent, VOICE_INPUT_REQUEST_CODE)
}
```

## 5. Tag-Based Organization

### Smart Tagging System
```kotlin
// In Note entity
val tags: String = ""  // Comma-separated tags

fun getTagsList(): List<String> {
    return tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

// Query by tag
@Query("SELECT * FROM notes WHERE tags LIKE '%' || :tag || '%' AND isArchived = 0")
fun getNotesByTag(tag: String): LiveData<List<Note>>
```

**Features:**
- Auto-suggest tags based on content
- Tag cloud visualization
- Filter by multiple tags
- Tag-based color coding

### Tag Auto-Suggestion
```kotlin
fun suggestTags(content: String): List<String> {
    val keywords = listOf("work", "personal", "urgent", "meeting", "shopping", "health")
    return keywords.filter { content.contains(it, ignoreCase = true) }
}
```

## 6. Color Coding & Visual Organization

### Color-Based Categories
```kotlin
// In Note entity
val color: Int? = null  // Optional color for categorization

// Predefined color schemes
object NoteColors {
    val RED = Color.parseColor("#FFCDD2")
    val BLUE = Color.parseColor("#BBDEFB")
    val GREEN = Color.parseColor("#C8E6C9")
    val YELLOW = Color.parseColor("#FFF9C4")
    val PURPLE = Color.parseColor("#E1BEE7")
}
```

## 7. Productivity Analytics

### Track Completion Patterns
```kotlin
@Entity(tableName = "note_history")
data class NoteHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noteId: Long,
    val action: String,  // "created", "completed", "archived"
    val timestamp: Long
)

// Generate insights
fun getProductivityStats(): ProductivityStats {
    // Calculate:
    // - Tasks completed per day/week
    // - Average completion time
    // - Most productive hours
    // - Task completion rate
}
```

## 8. Offline-First with Sync

### Room + Cloud Sync
```kotlin
class SyncRepository(
    private val localDao: NoteDao,
    private val remoteApi: NoteApi
) {
    suspend fun syncNotes() {
        // 1. Upload local changes
        val localNotes = localDao.getAllNotesFlow().first()
        remoteApi.uploadNotes(localNotes)
        
        // 2. Download remote changes
        val remoteNotes = remoteApi.fetchNotes()
        localDao.insertAll(remoteNotes)
    }
}
```

## 9. Quick Capture Widget

### Home Screen Widget
```kotlin
class QuickNoteWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Create quick capture button on home screen
        // Opens app directly to new note creation
    }
}
```

## 10. Focus Mode

### Distraction-Free Writing
```kotlin
class FocusModeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Full-screen mode
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        
        // Minimal UI - just text editor
        // Auto-save every few seconds
        // No notifications during focus mode
    }
}
```

## 11. Natural Language Processing

### Smart Date Parsing
```kotlin
fun parseNaturalLanguageDate(text: String): Long? {
    return when {
        text.contains("tomorrow", ignoreCase = true) -> 
            System.currentTimeMillis() + 24 * 60 * 60 * 1000
        text.contains("next week", ignoreCase = true) -> 
            System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000
        text.contains("in 2 hours", ignoreCase = true) -> 
            System.currentTimeMillis() + 2 * 60 * 60 * 1000
        else -> null
    }
}

// Usage: "Remind me tomorrow to buy milk"
// Automatically sets due date and creates note
```

## 12. Collaborative Notes

### Share & Collaborate
```kotlin
@Entity(tableName = "shared_notes")
data class SharedNote(
    @PrimaryKey val noteId: Long,
    val sharedWith: String,  // User IDs
    val permissions: String  // "view", "edit"
)

// Real-time sync with Firebase or similar
class CollaborativeNoteViewModel : ViewModel() {
    fun shareNote(noteId: Long, userId: String, permission: Permission) {
        // Share note with another user
        // Set up real-time listeners for changes
    }
}
```

## Implementation Priority

1. ✅ **Already Implemented:**
   - Smart sorting with urgency scores
   - Swipe to archive gesture
   - Tag-based organization
   - Priority system
   - Search functionality

2. **High Priority (Easy to Implement):**
   - Time-based notifications with WorkManager
   - Calendar integration
   - Share functionality
   - Color coding

3. **Medium Priority:**
   - Voice input
   - Productivity analytics
   - Home screen widget
   - Focus mode

4. **Advanced Features:**
   - Natural language processing
   - Collaborative notes with real-time sync
   - Cloud backup and sync

## Testing Recommendations

1. Test swipe gestures on different screen sizes
2. Verify notification delivery at correct times
3. Test offline functionality
4. Validate data persistence across app restarts
5. Performance testing with large datasets (1000+ notes)
