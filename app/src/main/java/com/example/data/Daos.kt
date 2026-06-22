package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OrganizerDao {

    // --- Photos Queries ---
    @Query("SELECT * FROM photos ORDER BY timestamp DESC")
    fun getAllPhotos(): Flow<List<PhotoItem>>

    @Query("SELECT * FROM photos WHERE category = :category ORDER BY timestamp DESC")
    fun getPhotosByCategory(category: String): Flow<List<PhotoItem>>

    @Query("SELECT * FROM photos WHERE isDuplicate = 1 ORDER BY duplicateGroupId DESC")
    fun getDuplicatePhotos(): Flow<List<PhotoItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoItem>)

    @Delete
    suspend fun deletePhoto(photo: PhotoItem)

    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun deletePhotoById(id: Int)

    // --- Screenshot Queries ---
    @Query("SELECT * FROM screenshots ORDER BY timestamp DESC")
    fun getAllScreenshots(): Flow<List<ScreenshotItem>>

    @Query("SELECT * FROM screenshots WHERE ocrText LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchScreenshots(query: String): Flow<List<ScreenshotItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreenshot(screenshot: ScreenshotItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreenshots(screenshots: List<ScreenshotItem>)

    @Delete
    suspend fun deleteScreenshot(screenshot: ScreenshotItem)

    // --- Abandoned Apps ---
    @Query("SELECT * FROM abandoned_apps ORDER BY lastUsedDaysAgo DESC")
    fun getAbandonedApps(): Flow<List<AbandonedApp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbandonedApps(apps: List<AbandonedApp>)

    @Query("DELETE FROM abandoned_apps WHERE packageName = :packageName")
    suspend fun deleteAbandonedAppByPackage(packageName: String)

    // --- Large Files ---
    @Query("SELECT * FROM large_files ORDER BY sizeBytes DESC")
    fun getLargeFiles(): Flow<List<LargeFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLargeFiles(files: List<LargeFile>)

    @Query("DELETE FROM large_files WHERE id = :id")
    suspend fun deleteLargeFileById(id: Int)

    // --- Weekly Reports ---
    @Query("SELECT * FROM weekly_reports ORDER BY weekStartDate DESC LIMIT 1")
    fun getLatestWeeklyReport(): Flow<WeeklyReport?>

    @Query("SELECT * FROM weekly_reports ORDER BY weekStartDate DESC")
    fun getAllWeeklyReports(): Flow<List<WeeklyReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeeklyReport(report: WeeklyReport)
}
