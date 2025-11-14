# Android MVVM + Room Database Template

This template provides a complete setup for an Android app using MVVM architecture with Room database.

## Project Structure

```
app/src/main/java/com/example/noteapp/
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
│   │   ├── NoteAdapter.kt
│   │   └── NoteViewModel.kt
│   └── MainActivity.kt
└── util/
    └── SwipeToArchiveCallback.kt
```

## Dependencies

Add these to your `build.gradle.kts` (Module: app):

```kotlin
dependencies {
    // Room
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")

    // ViewModel and LiveData
    val lifecycle_version = "2.7.0"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}
```

Add KSP plugin to `build.gradle.kts` (Module: app):

```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
}
```

## Features Included

1. ✅ MVVM Architecture
2. ✅ Room Database with Dao
3. ✅ Repository Pattern
4. ✅ LiveData Observation
5. ✅ Swipe to Archive Gesture
6. ✅ Smart Sorting Options
7. ✅ Time-based Features

## Bonus Features Explained

See `BONUS_FEATURES.md` for unique feature implementations.
