package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PhotoItem::class,
        ScreenshotItem::class,
        AbandonedApp::class,
        LargeFile::class,
        WeeklyReport::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun organizerDao(): OrganizerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "digital_life_organizer_db"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.organizerDao())
                }
            }
        }

        suspend fun populateDatabase(dao: OrganizerDao) {
            // Seed Photos
            val photos = listOf(
                PhotoItem(
                    uri = "sim_photo_receipt_1",
                    path = "/storage/emulated/0/DCIM/Camera/وصل_بقالة_العثيم.jpg",
                    name = "وصل البقالة - أسواق العثيم.jpg",
                    category = "Receipts",
                    sizeBytes = 552960 // 540 KB
                ),
                PhotoItem(
                    uri = "sim_photo_receipt_2",
                    path = "/storage/emulated/0/DCIM/Camera/فاتورة_الكهرباء_مارس.jpg",
                    name = "وصل فاتورة الكهرباء - مارس.jpg",
                    category = "Receipts",
                    sizeBytes = 819200 // 800 KB
                ),
                PhotoItem(
                    uri = "sim_photo_personal_1",
                    path = "/storage/emulated/0/DCIM/Camera/العائلة_في_حديقة_الملك_عبدالله.jpg",
                    name = "العائلة في الحديقة.jpg",
                    category = "Personal",
                    sizeBytes = 2516582 // 2.4 MB
                ),
                PhotoItem(
                    uri = "sim_photo_travel_1",
                    path = "/storage/emulated/0/DCIM/Camera/شروق_الشمس_في_العلا.jpg",
                    name = "شروق الشمس في العلا.jpg",
                    category = "Travel",
                    sizeBytes = 4194304 // 4 MB
                ),
                PhotoItem(
                    uri = "sim_photo_food_1",
                    path = "/storage/emulated/0/DCIM/Camera/كبسة_لحم_غداء_أمس.jpg",
                    name = "كبسة لحم - غداء أمس.jpg",
                    category = "Food",
                    sizeBytes = 1887436 // 1.8 MB
                ),
                PhotoItem(
                    uri = "sim_photo_document_1",
                    path = "/storage/emulated/0/DCIM/Camera/الضمان_الاجتماعي_سند.jpg",
                    name = "وثيقة الضمان الاجتماعي.jpg",
                    category = "Documents",
                    sizeBytes = 1258291 // 1.2 MB
                ),
                PhotoItem(
                    uri = "sim_photo_meme_1",
                    path = "/storage/emulated/0/DCIM/Camera/رياكشن_فلو_كود_ههههه.jpg",
                    name = "ميم الكود البرمجي والقهوة.jpg",
                    category = "Memes",
                    sizeBytes = 358400 // 350 KB
                ),
                // Duplicates sample
                PhotoItem(
                    uri = "sim_photo_duplicate_1",
                    path = "/storage/emulated/0/DCIM/Camera/صورة_الغروب_جميلة_1.jpg",
                    name = "غروب الشمس بالصحراء (1).jpg",
                    category = "Travel",
                    isDuplicate = true,
                    duplicateGroupId = 101,
                    sizeBytes = 3258291 // 3.1 MB
                ),
                PhotoItem(
                    uri = "sim_photo_duplicate_2",
                    path = "/storage/emulated/0/DCIM/Camera/صورة_الغروب_جميلة_2.jpg",
                    name = "غروب الشمس بالصحراء (2) مكرر.jpg",
                    category = "Travel",
                    isDuplicate = true,
                    duplicateGroupId = 101,
                    sizeBytes = 3258291 // 3.1 MB
                )
            )
            dao.insertPhotos(photos)

            // Seed Screenshots with OCR
            val screenshots = listOf(
                ScreenshotItem(
                    uri = "sim_screenshot_1",
                    path = "/storage/emulated/0/Pictures/Screenshots/رقم_أحمد_الجديد.png",
                    name = "لقطة شاشة رقم هاتف أحمد.png",
                    ocrText = "أحمد: أهلاً يا صاحبي، هذا هو رقم الهاتف الجديد الخاص بي: 0554124921. لا تنسى حفظه للحالات الطارئة!",
                    subCategory = "Chats"
                ),
                ScreenshotItem(
                    uri = "sim_screenshot_2",
                    path = "/storage/emulated/0/Pictures/Screenshots/مقالة_الذكاء_الاصطناعي.png",
                    name = "مقالة المستقبل والتقنية التوليدية.png",
                    ocrText = "الذكاء الاصطناعي التوليدي والشبكات العصبية العميقة تغير مستقبل العمل والإنتاجية والهندسة البرمجية في عام 2026 وتوفر ميزة البحث الفائق الفوري.",
                    subCategory = "Articles"
                ),
                ScreenshotItem(
                    uri = "sim_screenshot_3",
                    path = "/storage/emulated/0/Pictures/Screenshots/كود_بايثون_الترتيب.png",
                    name = "خوارزمية فيبوناتشي البرمجية.png",
                    ocrText = "def fibonacci(n):\n    if n <= 1: return n\n    return fibonacci(n-1) + fibonacci(n-2)",
                    subCategory = "Code"
                ),
                ScreenshotItem(
                    uri = "sim_screenshot_4",
                    path = "/storage/emulated/0/Pictures/Screenshots/خريطة_موقع_النادي.png",
                    name = "موقع صالة تدريب كيك بوكسينغ.png",
                    ocrText = "سوبر فتنس - شارع التخصصي بالرياض، الاتجاه غرباً بعد تقاطع فهد، بجانب مجمع عيادات أسنان نجد الطبية.",
                    subCategory = "Maps"
                )
            )
            dao.insertScreenshots(screenshots)

            // Seed Abandoned Apps
            val abandonedApps = listOf(
                AbandonedApp(
                    packageName = "com.game.temple.run",
                    appLabel = "Temple Run 2",
                    lastUsedDaysAgo = 195,
                    sizeBytes = 1610612736L, // 1.5 GB
                    installTime = System.currentTimeMillis() - (180 * 24 * 3600 * 1000L)
                ),
                AbandonedApp(
                    packageName = "com.editor.video.pro",
                    appLabel = "Cut Pro Editor",
                    lastUsedDaysAgo = 110,
                    sizeBytes = 912261120L, // 870 MB
                    installTime = System.currentTimeMillis() - (120 * 24 * 3600 * 1000L)
                ),
                AbandonedApp(
                    packageName = "com.pdf.scanner.trial",
                    appLabel = "Scanner Trial Plus",
                    lastUsedDaysAgo = 92,
                    sizeBytes = 251658240L, // 240 MB
                    installTime = System.currentTimeMillis() - (100 * 24 * 3600 * 1000L)
                ),
                AbandonedApp(
                    packageName = "com.learn.spanish.offline",
                    appLabel = "Learn Spanish Offline",
                    lastUsedDaysAgo = 145,
                    sizeBytes = 419430400L, // 400 MB
                    installTime = System.currentTimeMillis() - (150 * 24 * 3600 * 1000L)
                )
            )
            dao.insertAbandonedApps(abandonedApps)

            // Seed Large Files
            val largeFiles = listOf(
                LargeFile(
                    name = "فيديو حفل تخرج الجامعة دقة عالية.mp4",
                    path = "/storage/emulated/0/Movies/فيديو_التخرج.mp4",
                    sizeBytes = 1342177280L, // 1.25 GB
                    fileType = "Video"
                ),
                LargeFile(
                    name = "كتاب هندسة النظم البرمجية العملاقة.pdf",
                    path = "/storage/emulated/0/Download/Software_Architecture.pdf",
                    sizeBytes = 157286400L, // 150 MB
                    fileType = "PDF"
                ),
                LargeFile(
                    name = "النسخة الاحتياطية للهاتف القديم الكاملة.zip",
                    path = "/storage/emulated/0/Backup/old_backup.zip",
                    sizeBytes = 901865472L, // 860 MB
                    fileType = "ZIP"
                )
            )
            dao.insertLargeFiles(largeFiles)

            // Seed Weekly Report
            val weeklyReport = WeeklyReport(
                weekStartDate = System.currentTimeMillis() - (4 * 24 * 3600 * 1000), // 4 days ago
                photosAnalyzed = 45,
                screenshotsProcessed = 12,
                duplicatesCleaned = 200,
                spaceSavedBytes = 1288490188L, // 1.2 GB
                topAppUse = "إنستغرام (4 ساعات)",
                leastAppUse = "Kindle (0 دقيقة)"
            )
            dao.insertWeeklyReport(weeklyReport)
        }
    }
}
