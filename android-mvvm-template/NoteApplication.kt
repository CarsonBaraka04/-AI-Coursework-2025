package com.example.noteapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Configuration
import androidx.work.WorkManager

/**
 * Application class for global initialization
 */
class NoteApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize notification channels
        createNotificationChannels()
        
        // Initialize WorkManager (if not using default configuration)
        // WorkManager.initialize(this, Configuration.Builder().build())
    }
    
    /**
     * Create notification channels for Android O and above
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            
            // High priority channel for urgent reminders
            val urgentChannel = NotificationChannel(
                CHANNEL_URGENT,
                "Urgent Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for urgent tasks and overdue items"
                enableVibration(true)
                enableLights(true)
            }
            
            // Default channel for regular reminders
            val defaultChannel = NotificationChannel(
                CHANNEL_DEFAULT,
                "Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for task reminders"
                enableVibration(true)
            }
            
            // Low priority channel for general notifications
            val lowPriorityChannel = NotificationChannel(
                CHANNEL_LOW_PRIORITY,
                "General Notifications",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "General app notifications"
            }
            
            notificationManager.createNotificationChannel(urgentChannel)
            notificationManager.createNotificationChannel(defaultChannel)
            notificationManager.createNotificationChannel(lowPriorityChannel)
        }
    }
    
    companion object {
        const val CHANNEL_URGENT = "urgent_channel"
        const val CHANNEL_DEFAULT = "default_channel"
        const val CHANNEL_LOW_PRIORITY = "low_priority_channel"
    }
}
