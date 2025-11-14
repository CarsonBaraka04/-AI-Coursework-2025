# Quick Start Guide

## Setup Instructions

### 1. Create New Android Project

1. Open Android Studio
2. Create New Project
3. Select "Empty Activity"
4. Set package name: `com.example.noteapp`
5. Language: Kotlin
6. Minimum SDK: API 24 (Android 7.0)

### 2. Add Dependencies

Copy the contents of `build.gradle.kts` to your app module's build.gradle.kts file.

**Important:** Add KSP plugin to project-level `build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
}
```

### 3. Copy Files

Copy all Kotlin files to your project following the package structure:

```
app/src/main/java/com/example/noteapp/
├── NoteApplication.kt
├── data/
│   ├── local/
│   │   ├── NoteDatabase.kt
│   │   └── NoteDao.kt
│   ├── model/
│   │   └── Note.kt
│   └── repository/
│       └── NoteRepository.kt
├── ui/
│   ├── notes/
│   │   ├── NoteFragment.kt
│   │   ├── NoteViewModel.kt
│   │   └── NoteAdapter.kt
│   └── MainActivity.kt
└── util/
    └── SwipeToArchiveCallback.kt
```

### 4. Update AndroidManifest.xml

Replace your AndroidManifest.xml with the provided one, or add the Application class:

```xml
<application
    android:name=".NoteApplication"
    ...>
```

### 5. Create MainActivity

```kotlin
package com.example.noteapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.noteapp.R
import com.example.noteapp.ui.notes.NoteFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, NoteFragment())
                .commit()
        }
    }
}
```

### 6. Create Layout Files

**res/layout/activity_main.xml:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/container"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

### 7. Sync and Build

1. Sync Gradle files
2. Build project
3. Run on emulator or device

## Testing the App

### Basic Operations

1. **View Notes**: App opens with list of sample notes
2. **Add Note**: Tap FAB (floating action button)
3. **Search**: Use search bar at top
4. **Archive**: Swipe note left or right
5. **Options**: Long press on note

### Testing Features

```kotlin
// In your test or main code
val testNote = Note(
    title = "Test Note",
    content = "This is a test",
    priority = Note.Priority.HIGH,
    tags = "test,demo",
    dueDate = System.currentTimeMillis() + 24 * 60 * 60 * 1000
)

viewModel.insert(testNote)
```

## Common Issues & Solutions

### Issue 1: KSP Not Found
**Solution:** Add KSP plugin to project-level build.gradle.kts

### Issue 2: Room Schema Export Error
**Solution:** Create `schemas` directory in app module or disable schema export:
```kotlin
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
```

### Issue 3: ViewBinding Not Working
**Solution:** Enable in build.gradle.kts:
```kotlin
buildFeatures {
    viewBinding = true
}
```

### Issue 4: Coroutines Error
**Solution:** Ensure you have coroutines dependency:
```kotlin
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

## Next Steps

### 1. Improve UI
- Create proper XML layouts instead of programmatic views
- Add Material Design components
- Implement proper themes and styles

### 2. Add Features
- Note editing screen
- Settings screen
- Export/Import functionality
- Dark mode

### 3. Implement Bonus Features
See `BONUS_FEATURES.md` for:
- Time-based notifications
- Calendar integration
- Voice input
- Productivity analytics

### 4. Add Testing
```kotlin
// Example ViewModel test
@Test
fun insertNote_addsNoteToDatabase() = runTest {
    val note = Note(title = "Test", content = "Content")
    viewModel.insert(note)
    
    val notes = viewModel.allNotes.getOrAwaitValue()
    assertThat(notes).contains(note)
}
```

### 5. Optimize Performance
- Add pagination for large lists
- Implement caching
- Optimize database queries
- Add ProGuard rules for release

## Resources

- [Android Developers - Room](https://developer.android.com/training/data-storage/room)
- [Android Developers - ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [Android Developers - LiveData](https://developer.android.com/topic/libraries/architecture/livedata)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)

## Support

For issues or questions:
1. Check `PACKAGE_STRUCTURE.md` for architecture guidance
2. Review `BONUS_FEATURES.md` for feature implementation
3. Consult Android documentation
4. Search Stack Overflow

## License

This template is provided as-is for educational purposes.
