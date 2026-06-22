package com.example.data

import kotlinx.coroutines.flow.Flow

class OrganizerRepository(private val dao: OrganizerDao) {

    val allPhotos: Flow<List<PhotoItem>> = dao.getAllPhotos()
    val duplicatePhotos: Flow<List<PhotoItem>> = dao.getDuplicatePhotos()
    val allScreenshots: Flow<List<ScreenshotItem>> = dao.getAllScreenshots()
    val abandonedApps: Flow<List<AbandonedApp>> = dao.getAbandonedApps()
    val largeFiles: Flow<List<LargeFile>> = dao.getLargeFiles()
    val latestWeeklyReport: Flow<WeeklyReport?> = dao.getLatestWeeklyReport()
    val allWeeklyReports: Flow<List<WeeklyReport>> = dao.getAllWeeklyReports()

    fun getPhotosByCategory(category: String): Flow<List<PhotoItem>> {
        return dao.getPhotosByCategory(category)
    }

    fun searchScreenshots(query: String): Flow<List<ScreenshotItem>> {
        return dao.searchScreenshots(query)
    }

    suspend fun insertPhoto(photo: PhotoItem) {
        dao.insertPhoto(photo)
    }

    suspend fun insertPhotos(photos: List<PhotoItem>) {
        dao.insertPhotos(photos)
    }

    suspend fun deletePhoto(photo: PhotoItem) {
        dao.deletePhoto(photo)
    }

    suspend fun deletePhotoById(id: Int) {
        dao.deletePhotoById(id)
    }

    suspend fun insertScreenshot(screenshot: ScreenshotItem) {
        dao.insertScreenshot(screenshot)
    }

    suspend fun insertScreenshots(screenshots: List<ScreenshotItem>) {
        dao.insertScreenshots(screenshots)
    }

    suspend fun deleteScreenshot(screenshot: ScreenshotItem) {
        dao.deleteScreenshot(screenshot)
    }

    suspend fun insertAbandonedApps(apps: List<AbandonedApp>) {
        dao.insertAbandonedApps(apps)
    }

    suspend fun deleteAbandonedAppByPackage(packageName: String) {
        dao.deleteAbandonedAppByPackage(packageName)
    }

    suspend fun insertLargeFiles(files: List<LargeFile>) {
        dao.insertLargeFiles(files)
    }

    suspend fun deleteLargeFileById(id: Int) {
        dao.deleteLargeFileById(id)
    }

    suspend fun insertWeeklyReport(report: WeeklyReport) {
        dao.insertWeeklyReport(report)
    }
}
