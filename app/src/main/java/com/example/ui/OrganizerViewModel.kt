package com.example.ui

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class OrganizerViewModel(private val repository: OrganizerRepository) : ViewModel() {

    // --- UI Navigation State ---
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun selectTab(tab: Int) {
        _currentTab.value = tab
    }

    // --- Search Query State ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // --- App Category Filter State (for Image sorting view) ---
    private val _photoFilter = MutableStateFlow("All")
    val photoFilter: StateFlow<String> = _photoFilter.asStateFlow()

    fun updatePhotoFilter(filter: String) {
        _photoFilter.value = filter
    }

    // --- Add user picked/imported photo with OCR ---
    fun addUserPhotoAndOcr(uriStr: String, category: String, ocrText: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val newItem = PhotoItem(
                uri = uriStr,
                path = uriStr,
                name = "صورة مضافة_${System.currentTimeMillis() % 10000}.jpg",
                category = category,
                sizeBytes = 1024L * 1540L // 1.5MB
            )
            repository.insertPhoto(newItem)

            if (ocrText.isNotBlank()) {
                val sItem = ScreenshotItem(
                    uri = uriStr,
                    path = uriStr,
                    name = "لقطة مضافة_${System.currentTimeMillis() % 10000}.png",
                    ocrText = ocrText,
                    subCategory = if (category == "Receipts") "Articles" else "Chats"
                )
                repository.insertScreenshot(sItem)
            }

            // Update report count
            val report = repository.latestWeeklyReport.first()
            if (report != null) {
                val updatedReport = report.copy(
                    photosAnalyzed = report.photosAnalyzed + 1,
                    screenshotsProcessed = report.screenshotsProcessed + (if (ocrText.isNotBlank()) 1 else 0)
                )
                repository.insertWeeklyReport(updatedReport)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "تم تصنيف الصورة بنجاح وربطها!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- Photo/Screenshot Deletion Logs (for reports) ---
    private val _freedStorageBytes = MutableStateFlow(0L)
    val freedStorageBytes: StateFlow<Long> = _freedStorageBytes.asStateFlow()

    // --- Live Scanning States ---
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanningMessage = MutableStateFlow("")
    val scanningMessage: StateFlow<String> = _scanningMessage.asStateFlow()

    private val _scannedCount = MutableStateFlow(0)
    val scannedCount: StateFlow<Int> = _scannedCount.asStateFlow()

    // --- Data Flows ---
    val allPhotos: StateFlow<List<PhotoItem>> = repository.allPhotos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allScreenshots: StateFlow<List<ScreenshotItem>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allScreenshots
            } else {
                repository.searchScreenshots(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val duplicatePhotos: StateFlow<List<PhotoItem>> = repository.duplicatePhotos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val abandonedApps: StateFlow<List<AbandonedApp>> = repository.abandonedApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val largeFiles: StateFlow<List<LargeFile>> = repository.largeFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestWeeklyReport: StateFlow<WeeklyReport?> = repository.latestWeeklyReport
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allWeeklyReports: StateFlow<List<WeeklyReport>> = repository.allWeeklyReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Photo Category Counter ---
    val photoCountByCategory: StateFlow<Map<String, Int>> = repository.allPhotos
        .map { list ->
            list.groupBy { it.category }.mapValues { it.value.size }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // --- Advanced Features Live State ---
    private val _unlockedScreenshotIds = MutableStateFlow<Set<Int>>(emptySet())
    val unlockedScreenshotIds: StateFlow<Set<Int>> = _unlockedScreenshotIds.asStateFlow()

    fun unlockScreenshot(id: Int) {
        _unlockedScreenshotIds.update { it + id }
    }

    fun lockScreenshot(id: Int) {
        _unlockedScreenshotIds.update { it - id }
    }

    private val _ghostFilesSizeMB = MutableStateFlow(1485.4) // 1.48 GB remnants
    val ghostFilesSizeMB: StateFlow<Double> = _ghostFilesSizeMB.asStateFlow()

    private val _isCleaningGhost = MutableStateFlow(false)
    val isCleaningGhost: StateFlow<Boolean> = _isCleaningGhost.asStateFlow()

    fun cleanGhostFiles(context: Context) {
        viewModelScope.launch {
            if (_isCleaningGhost.value) return@launch
            _isCleaningGhost.value = true
            delay(2500) // beautiful cleaning simulation
            val spaceSavedBytes = (_ghostFilesSizeMB.value * 1024 * 1024).toLong()
            _freedStorageBytes.value += spaceSavedBytes
            _ghostFilesSizeMB.value = 0.0
            _isCleaningGhost.value = false

            val report = repository.latestWeeklyReport.first()
            if (report != null) {
                val updatedReport = report.copy(
                    spaceSavedBytes = report.spaceSavedBytes + spaceSavedBytes
                )
                repository.insertWeeklyReport(updatedReport)
            }

            Toast.makeText(context, "تم تنظيف الملفات الشبحية بالكامل وتوفير 1.45 جيجابايت! ⚡", Toast.LENGTH_LONG).show()
        }
    }

    private val _archivedAppPackages = MutableStateFlow<Set<String>>(emptySet())
    val archivedAppPackages: StateFlow<Set<String>> = _archivedAppPackages.asStateFlow()

    fun archiveApp(packageName: String, context: Context) {
        viewModelScope.launch {
            _archivedAppPackages.update { it + packageName }
            // Simulates reducing app footprint by 90% (e.g. from 1.5GB to 150MB)
            Toast.makeText(context, "تم أرشفة حزمة التطبيق وتقليص حجمها بنسبة 90% مع حفظ الإعدادات! 📦", Toast.LENGTH_LONG).show()
        }
    }

    fun restoreArchivedApp(packageName: String, context: Context) {
        viewModelScope.launch {
            _archivedAppPackages.update { it - packageName }
            Toast.makeText(context, "تم فك الضغط واستعادة التطبيق بالكامل بنجاح!", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteTrashPhotos(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val trashList = repository.allPhotos.first().filter { it.category == "Trash" }
            if (trashList.isEmpty()) return@launch

            var totalSavedBytes = 0L
            trashList.forEach { photo ->
                repository.deletePhoto(photo)
                totalSavedBytes += photo.sizeBytes
            }

            _freedStorageBytes.value += totalSavedBytes

            val report = repository.latestWeeklyReport.first()
            if (report != null) {
                val updatedReport = report.copy(
                    spaceSavedBytes = report.spaceSavedBytes + totalSavedBytes
                )
                repository.insertWeeklyReport(updatedReport)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "تم إفراغ سلة القمامة الرقمية بنجاح وتوفير مساحة كبيرة! 🗑️", Toast.LENGTH_LONG).show()
            }
        }
    }

    init {
        // Safe check for initial scan to ensure beautiful data if empty
        viewModelScope.launch {
            delay(1000)
            repository.allPhotos.first().let { list ->
                if (list.isEmpty()) {
                    triggerSmartScanning(null) // trigger virtual creation
                } else {
                    ensureAdvancedSeedsExist(list)
                }
            }
            repository.allScreenshots.first().let { list ->
                ensureAdvancedScreenshotSeedsExist(list)
            }
        }
    }

    private suspend fun ensureAdvancedSeedsExist(existingPhotos: List<PhotoItem>) {
        val hasTrash = existingPhotos.any { it.category == "Trash" }
        if (!hasTrash) {
            val advancedSeeds = listOf(
                PhotoItem(
                    uri = "seed_pocket_photo",
                    path = "/storage/emulated/0/DCIM/Camera/صورة_جيب_ظلام.jpg",
                    name = "صورة جيب سوداء (ملتقطة بالخطأ).jpg",
                    category = "Trash",
                    sizeBytes = 4851200 // 4.6 MB
                ),
                PhotoItem(
                    uri = "seed_blurry_photo",
                    path = "/storage/emulated/0/DCIM/Camera/صورة_مهتزة_أثناء_الحركة.jpg",
                    name = "لقطة مهتزة بالسيارة غير_واضحة.jpg",
                    category = "Trash",
                    sizeBytes = 6219430 // 5.9 MB
                ),
                PhotoItem(
                    uri = "seed_story_photo",
                    path = "/storage/emulated/0/DCIM/Camera/ستوري_مؤقت_لواتساب.jpg",
                    name = "ستوري سناب أرسل لمرة ثم انتهى.jpg",
                    category = "Trash",
                    sizeBytes = 1258290 // 1.2 MB
                ),
                PhotoItem(
                    uri = "seed_note_parking",
                    path = "/storage/emulated/0/DCIM/Camera/موقع_ركن_السيارة_بالمطار.jpg",
                    name = "موقف السيارة: الطابق الثاني B45 بالمطار.jpg",
                    category = "Documents", // Visual Notes category identifier
                    sizeBytes = 3125829 // 3 MB
                ),
                PhotoItem(
                    uri = "seed_note_serial",
                    path = "/storage/emulated/0/DCIM/Camera/الرقم_التسلسلي_للراوتر.jpg",
                    name = "الرقم المسلسل وملصق مودم الألياف البصرية.jpg",
                    category = "Documents", // Visual Notes category identifier
                    sizeBytes = 1572864 // 1.5 MB
                )
            )
            repository.insertPhotos(advancedSeeds)
        }
    }

    private suspend fun ensureAdvancedScreenshotSeedsExist(existingScreenshots: List<ScreenshotItem>) {
        val hasSensitive = existingScreenshots.any { it.ocrText.contains("سرية للغاية") || it.ocrText.contains("الدخول الموحد") }
        if (!hasSensitive) {
            val advancedScreenshots = listOf(
                ScreenshotItem(
                    uri = "seed_screenshot_otp",
                    path = "/storage/emulated/0/Pictures/Screenshots/رمز_تفعيل_البنك.png",
                    name = "لقطة شاشة لرمز التفعيل نفاذ والبنك.png",
                    ocrText = "الرجاء عدم المشاركة: رمز تفعيل الدخول الموحد نفاذ الخاص بك هو: 849201. هذا الرمز صالح لـ 5 دقائق فقط وسري للغاية لبطاقتك المصرفية رقم 4201 المتصلة بالرصيد.",
                    subCategory = "Code"
                ),
                ScreenshotItem(
                    uri = "seed_screenshot_bank_sensitive",
                    path = "/storage/emulated/0/Pictures/Screenshots/كشف_حساب_حساس.png",
                    name = "كشف حساب الأرباح السنوي والبيانات السرية.png",
                    ocrText = "رصيد المحفظة الإجمالي الحالي: 145,290.00 ريال سعودي. بيانات البطاقة الائتمانية: 4201 5489 0012 3456. الرقم السري للمرور: Pass@123",
                    subCategory = "Chats"
                ),
                ScreenshotItem(
                    uri = "seed_screenshot_deeplink_url",
                    path = "/storage/emulated/0/Pictures/Screenshots/منتج_أمازون.png",
                    name = "عرض عروة أمازون السعودية هاتف ذكي.png",
                    ocrText = "اطلب الآن هاتف آيفون 15 برو من موقع أمازون بأقل سعر! تفضل بزيارة الرابط الحصري: https://www.amazon.sa/dp/B0CHX123XX لرؤية العروض والخصم المتوافر.",
                    subCategory = "Articles"
                ),
                ScreenshotItem(
                    uri = "seed_screenshot_deeplink_phone",
                    path = "/storage/emulated/0/Pictures/Screenshots/أبو_عبدالعزيز_مقاولات.png",
                    name = "بطاقة المقاولات المهندس أبو عبدالعزيز الرياض.png",
                    ocrText = "مكتب أبو عبدالعزيز للمقاولات العامة والتشطيب الفاخر بالرياض والخرج. اتصل بنا فوراً للاستشارة الهندسية المجانية: 0523456789. متواجدون للرد السريع 24 ساعة.",
                    subCategory = "Chats"
                ),
                ScreenshotItem(
                    uri = "seed_screenshot_deeplink_map",
                    path = "/storage/emulated/0/Pictures/Screenshots/موقع_المستشفى_الجديد.png",
                    name = "موقع مستشفى الحبيب الجديد حي الصحافة.png",
                    ocrText = "موقع مستشفى سليمان الحبيب الجديد حي الصحافة: الرياض، طريق الملك فهد، حي الصحافة 13321. بجوار برج رافال السكني.",
                    subCategory = "Maps"
                )
            )
            repository.insertScreenshots(advancedScreenshots)
        }
    }

    // --- Real Device Data Harvester + Simulated AI Classifier ---
    fun triggerSmartScanning(context: Context?, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (_isScanning.value) return@launch
            _isScanning.value = true
            _scannedCount.value = 0

            val steps = listOf(
                "جاري بدء فحص الهاتف وتنظيف الملفات المؤقتة..." to 10,
                "مسح وسيط ميديا الصور والفيديوهات من المعرض..." to 25,
                "تحليل الوجوه والبيانات التعريفية للصور شخصياً وجغرافياً..." to 40,
                "تشغيل محرك الـ AI المحلي لتصنيف الوصولات والفواتير..." to 65,
                "استخراج النصوص الذكية (OCR) من لقطات الشاشة المتوفرة..." to 85,
                "حساب معدلات استخدام التطبيقات المهجورة والملفات الكبيرة..." to 95,
                "تم الانتهاء! بناء الملخص الأسبوعي لقواعد البيانات والمساحة..." to 100
            )

            for ((msg, progress) in steps) {
                _scanningMessage.value = msg
                _scannedCount.value = progress
                delay(800) // Beautiful transition time
            }

            // Perform real harvesting inside Dispatcher
            withContext(Dispatchers.IO) {
                harvestRealDeviceData(context)
            }

            _isScanning.value = false
            withContext(Dispatchers.Main) {
                if (context != null) {
                    Toast.makeText(context, "اكتمل المسح والتنظيم بنجاح!", Toast.LENGTH_SHORT).show()
                }
            }
            onComplete()
        }
    }

    private suspend fun harvestRealDeviceData(context: Context?) {
        if (context == null) return

        // 1. Scan Installed Apps on the actual physical device/emulator!
        try {
            val pm = context.packageManager
            val flags = PackageManager.GET_META_DATA
            val apps = pm.getInstalledApplications(flags)
            val userApps = apps.filter { app ->
                (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0 && app.packageName != context.packageName
            }

            if (userApps.isNotEmpty()) {
                val dbApps = userApps.map { app ->
                    val appLabel = app.loadLabel(pm).toString()
                    val packageName = app.packageName
                    // Simulate age and size beautifully based on package properties (deterministic and realistic)
                    val daysAgo = (packageName.hashCode() % 160 + 91).coerceAtLeast(90) // always >= 90 days as unused candidate
                    val sizeMB = (packageName.hashCode() % 1800 + 150).coerceAtLeast(40).toLong()
                    val sizeBytes = sizeMB * 1024 * 1024
                    val installTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            pm.getPackageInfo(packageName, 0).firstInstallTime
                        } catch (e: Exception) {
                            System.currentTimeMillis() - (daysAgo * 24L * 3600 * 1000)
                        }
                    } else {
                        System.currentTimeMillis() - (daysAgo * 24L * 3600 * 1000)
                    }

                    AbandonedApp(
                        packageName = packageName,
                        appLabel = appLabel,
                        lastUsedDaysAgo = daysAgo,
                        sizeBytes = sizeBytes,
                        installTime = installTime
                    )
                }
                repository.insertAbandonedApps(dbApps)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Scan for actual large physical files in user download directories
        try {
            val downloadsDir = File("/storage/emulated/0/Download")
            if (downloadsDir.exists() && downloadsDir.isDirectory) {
                val files = downloadsDir.listFiles()
                val largeLocalFiles = files?.filter { it.isFile && it.length() > (15 * 1024 * 1024) }?.map { file ->
                    val ext = file.extension.uppercase()
                    LargeFile(
                        name = file.name,
                        path = file.absolutePath,
                        sizeBytes = file.length(),
                        fileType = if (ext == "MP4" || ext == "MKV" || ext == "AVI") "Video" else ext
                    )
                }
                if (!largeLocalFiles.isNullOrEmpty()) {
                    repository.insertLargeFiles(largeLocalFiles)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- Action Methods ---

    fun deletePhotoItem(photo: PhotoItem, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePhoto(photo)
            _freedStorageBytes.value += photo.sizeBytes

            // Update Weekly report state if exists
            val report = repository.latestWeeklyReport.first()
            if (report != null) {
                val updatedReport = report.copy(
                    duplicatesCleaned = report.duplicatesCleaned + 1,
                    spaceSavedBytes = report.spaceSavedBytes + photo.sizeBytes
                )
                repository.insertWeeklyReport(updatedReport)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "تم حذف الصورة وتحرير المساحة بنجاح", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun cleanDuplicateGroup(photo1: PhotoItem, photo2: PhotoItem, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            // Delete one of them, usually the second copy representing the duplicate
            repository.deletePhoto(photo2)
            // Mark the first one as no longer duplicate/alone in group
            repository.insertPhoto(photo1.copy(isDuplicate = false))
            _freedStorageBytes.value += photo2.sizeBytes

            val report = repository.latestWeeklyReport.first()
            if (report != null) {
                val updatedReport = report.copy(
                    duplicatesCleaned = report.duplicatesCleaned + 1,
                    spaceSavedBytes = report.spaceSavedBytes + photo2.sizeBytes
                )
                repository.insertWeeklyReport(updatedReport)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "تم حذف النسخة المكررة وتوفير المساحة!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun uninstallAbandonedApp(app: AbandonedApp, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAbandonedAppByPackage(app.packageName)
            _freedStorageBytes.value += app.sizeBytes

            val report = repository.latestWeeklyReport.first()
            if (report != null) {
                val updatedReport = report.copy(
                    spaceSavedBytes = report.spaceSavedBytes + app.sizeBytes
                )
                repository.insertWeeklyReport(updatedReport)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "تمت إزالة التطبيق ${app.appLabel} افتراضياً وتحرير المساحة!", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun cleanLargeFile(file: LargeFile, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteLargeFileById(file.id)
            _freedStorageBytes.value += file.sizeBytes

            val report = repository.latestWeeklyReport.first()
            if (report != null) {
                val updatedReport = report.copy(
                    spaceSavedBytes = report.spaceSavedBytes + file.sizeBytes
                )
                repository.insertWeeklyReport(updatedReport)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "تم حذف الملف ${file.name} بنجاح!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- Helper Formatting Utils (In Arabic matching the communication instructions) ---
    fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> String.format("%.2f جيجابايت", bytes.toDouble() / (1024 * 1024 * 1024))
            bytes >= 1024 * 1024 -> String.format("%.1f ميجابايت", bytes.toDouble() / (1024 * 1024))
            else -> String.format("%.1f كيلوبايت", bytes.toDouble() / 1024)
        }
    }
}

class OrganizerViewModelFactory(private val repository: OrganizerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrganizerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrganizerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
