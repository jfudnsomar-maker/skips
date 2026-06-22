package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uri: String,
    val path: String,
    val name: String,
    val category: String, // Receipts, Personal, Travel, Food, Documents, Memes
    val isDuplicate: Boolean = false,
    val duplicateGroupId: Int = 0,
    val sizeBytes: Long,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "screenshots")
data class ScreenshotItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uri: String,
    val path: String,
    val name: String,
    val ocrText: String,
    val subCategory: String, // Chats, Articles, Code, Maps
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "abandoned_apps")
data class AbandonedApp(
    @PrimaryKey val packageName: String,
    val appLabel: String,
    val lastUsedDaysAgo: Int,
    val sizeBytes: Long,
    val installTime: Long
)

@Entity(tableName = "large_files")
data class LargeFile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val fileType: String // Video, PDF, ZIP, etc.
)

@Entity(tableName = "weekly_reports")
data class WeeklyReport(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val weekStartDate: Long,
    val photosAnalyzed: Int,
    val screenshotsProcessed: Int,
    val duplicatesCleaned: Int,
    val spaceSavedBytes: Long,
    val topAppUse: String, // e.g. "إنستغرام (4 ساعات)"
    val leastAppUse: String // e.g. "Kindle (0 دقيقة)"
)
