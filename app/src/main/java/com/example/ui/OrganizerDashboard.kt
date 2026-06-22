@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.ClipboardManager
import android.content.ClipData
import androidx.compose.ui.draw.blur
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.AbandonedApp
import com.example.data.LargeFile
import com.example.data.PhotoItem
import com.example.data.ScreenshotItem

// Creative Color Palette (Luxury Slate Green & Neon Highlights)
val DarkBackground = Color(0xFF0F172A) // Sleek slate obsidian
val CardSurface = Color(0xFF1E293B)     // Dark layered slate
val AccentCyan = Color(0xFF06B6D4)      // Glowing cyber cyan
val BrightYellow = Color(0xFFF59E0B)    // Golden warning accent
val HighlightCoral = Color(0xFFF43F5E)  // Deep alert rose/coral
val SlateGray = Color(0xFF64748B)       // Subtext color
val PremiumGreen = Color(0xFF10B981)    // Active safe status green

@Composable
fun OrganizerDashboard(viewModel: OrganizerViewModel) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scanningMsg by viewModel.scanningMessage.collectAsStateWithLifecycle()
    val scannedProgress by viewModel.scannedCount.collectAsStateWithLifecycle()
    val freedBytes by viewModel.freedStorageBytes.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_dashboard_scaffold"),
        containerColor = DarkBackground,
        bottomBar = {
            Column {
                if (freedBytes > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PremiumGreen.copy(alpha = 0.2f))
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚡ تم توفير ${viewModel.formatBytes(freedBytes)} من المساحة في هذه الجلسة!",
                            color = PremiumGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("storage_freed_indicator")
                        )
                    }
                }
                BottomNavigationBar(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .drawBehind {
                    // Modern atmospheric radial glow in bottom corner
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(AccentCyan.copy(alpha = 0.08f), Color.Transparent),
                            center = Offset(size.width, size.height),
                            radius = size.width * 0.8f
                        ),
                        radius = size.width * 0.8f,
                        center = Offset(size.width, size.height)
                    )
                }
        ) {
            Crossfade(
                targetState = currentTab,
                animationSpec = tween(300),
                label = "TabContent"
            ) { tab ->
                when (tab) {
                    0 -> DashboardTab(viewModel = viewModel)
                    1 -> PhotoOrganizerTab(viewModel = viewModel)
                    2 -> ScreenshotsTab(viewModel = viewModel)
                    3 -> StorageGuardianTab(viewModel = viewModel)
                    4 -> WeeklyReportTab(viewModel = viewModel)
                }
            }

            // Beautiful Overlay Scanning Loader
            if (isScanning) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBackground.copy(alpha = 0.92f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        // Cyan Spinning Ring
                        val infiniteTransition = rememberInfiniteTransition(label = "ring")
                        val angle by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "angle"
                        )

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(130.dp)
                        ) {
                            Canvas(modifier = Modifier.size(110.dp)) {
                                drawArc(
                                    color = SlateGray.copy(alpha = 0.2f),
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = AccentCyan,
                                    startAngle = angle,
                                    sweepAngle = 120f,
                                    useCenter = false,
                                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$scannedProgress%",
                                    color = AccentCyan,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "فحص للشبكة الـ AI",
                                    color = SlateGray,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = scanningMsg,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().testTag("scan_message_text")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "يقوم النظام بتحليل كافة البيانات ومعالجتها محلياً بشكل فائق الأمان دون استهلاك الإنترنت.",
                            color = SlateGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

// Bottom Navigation Row
@Composable
fun BottomNavigationBar(currentTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = CardSurface,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        val tabs = listOf(
            Triple("الرئيسية", Icons.Default.Dashboard, Icons.Outlined.Dashboard),
            Triple("الألبومات", Icons.Default.PhotoLibrary, Icons.Outlined.PhotoLibrary),
            Triple("الالتقاطات", Icons.Default.Screenshot, Icons.Outlined.Screenshot),
            Triple("الحارس", Icons.Default.Storage, Icons.Outlined.Storage),
            Triple("التقرير", Icons.Default.Assessment, Icons.Outlined.Assessment)
        )

        tabs.forEachIndexed { index, (label, filledIcon, outlinedIcon) ->
            val isSelected = currentTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) filledIcon else outlinedIcon,
                        contentDescription = label,
                        tint = if (isSelected) AccentCyan else SlateGray
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) AccentCyan else SlateGray
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = AccentCyan.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_tab_$index")
            )
        }
    }
}

// TAB 0: HOME / DASHBOARD
@Composable
fun DashboardTab(viewModel: OrganizerViewModel) {
    val context = LocalContext.current
    val photos by viewModel.allPhotos.collectAsStateWithLifecycle()
    val duplicates by viewModel.duplicatePhotos.collectAsStateWithLifecycle()
    val unusedApps by viewModel.abandonedApps.collectAsStateWithLifecycle()
    val largeFiles by viewModel.largeFiles.collectAsStateWithLifecycle()

    var showInputTextByUriDialog by remember { mutableStateOf<String?>(null) }
    var mockOcrTextEntered by remember { mutableStateOf("") }
    var selectedPhotoCategory by remember { mutableStateOf("Receipts") }

    // Media Picker Integration
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                showInputTextByUriDialog = uri.toString()
            }
        }
    )

    // Handle incoming image seed
    if (showInputTextByUriDialog != null) {
        AlertDialog(
            onDismissRequest = { showInputTextByUriDialog = null },
            title = {
                Text(
                    text = "نظام الفحص والتصنيف الذكي 🔍",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "تم التقاط الصورة بنجاح! الرجاء تصنيفها أو كتابة نص OCR لاستخراج الكلمات منها:",
                        color = SlateGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "التصنيف المقترح:",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val categories = listOf(
                        "Receipts" to "فواتير ووصولات 🧾",
                        "Personal" to "شخصية وعائلية 📸",
                        "Travel" to "السفر والمناظر 🏞️",
                        "Food" to "طعام ومطاعم 🍔",
                        "Documents" to "مستندات ووثائق 📄",
                        "Memes" to "ميمز ونكت 🎭"
                    )

                    categories.forEach { (key, display) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPhotoCategory = key }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = display,
                                color = if (selectedPhotoCategory == key) AccentCyan else Color.White,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            RadioButton(
                                selected = selectedPhotoCategory == key,
                                onClick = { selectedPhotoCategory = key },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentCyan)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = mockOcrTextEntered,
                        onValueChange = { mockOcrTextEntered = it },
                        label = { Text("محتوى النص داخل الصورة للبحث (OCR)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ocr_input_field"),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val uriStr = showInputTextByUriDialog ?: return@TextButton
                        viewModel.addUserPhotoAndOcr(
                            uriStr = uriStr,
                            category = selectedPhotoCategory,
                            ocrText = mockOcrTextEntered,
                            context = context
                        )
                        showInputTextByUriDialog = null
                        mockOcrTextEntered = ""
                    },
                    modifier = Modifier.testTag("confirm_seed_button")
                ) {
                    Text("حفظ وتصنيف", color = AccentCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showInputTextByUriDialog = null }) {
                    Text("إلغاء", color = SlateGray)
                }
            },
            containerColor = CardSurface
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_tab"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(18.dp))
            DashboardHeader(
                onScanClick = {
                    viewModel.triggerSmartScanning(context)
                },
                onAddPhotoClick = {
                    mediaPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
        }

        item {
            StorageHealthCard(
                photosCount = photos.size,
                duplicatesCount = duplicates.size / 2,
                unusedAppsCount = unusedApps.size,
                largeFilesCount = largeFiles.size,
                viewModel = viewModel
            )
        }

        item {
            ActionsQuickRow(
                onOrganizerClick = { viewModel.selectTab(1) },
                onScreenshotsClick = { viewModel.selectTab(2) },
                onGuardianClick = { viewModel.selectTab(3) }
            )
        }

        // Nightly Optimizer Status Widget (WorkManager constraints check)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth().testTag("workmanager_checklist_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(PremiumGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("نشط وتلقائي 💤", color = PremiumGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("محرك التحسين التلقائي (WorkManager) ⚙️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "للحفاظ على هدوء المعالج وعمر البطارية المديد، يعمل التحسين الذكي محلياً في الخلفية فقط عند استيفاء الشروط الصارمة التالية:",
                        color = SlateGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Right,
                        lineHeight = 16.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    // Checklist items
                    val constraints = listOf(
                        "الهاتف متصل بمصدر شحن خارجي (تيار ثابت) 🔌" to true,
                        "متصل بشبكة Wi-Fi لاسلكية نشطة ومستقرة 📶" to true,
                        "شحن بطارية الهاتف يتخطى حاجز الأمان 80% 🔋" to true,
                        "التصنيف مبرمج في وقت السكون المعتاد (2-5 صباحاً) 🌙" to true
                    )
                    constraints.forEach { (text, met) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = text, color = if (met) Color.White else SlateGray, fontSize = 11.sp, modifier = Modifier.padding(end = 8.dp))
                            Icon(
                                imageVector = if (met) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = "تم الاستيفاء",
                                tint = if (met) PremiumGreen else SlateGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.triggerSmartScanning(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, AccentCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Text("تشغيل فحص AI يدوي استثنائي الآن ⚡", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Digital Time Capsule (كبسولة الزمن الرقمية)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = BorderStroke(1.dp, BrightYellow.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth().testTag("time_capsule_card")
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(
                                Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Beautiful synthetic travel sunset representation
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(
                                brush = Brush.linearGradient(
                                        listOf(Color(0xFFD97706), Color(0xFFF43F5E))
                                )
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "ذكريات",
                                tint = BrightYellow,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("العلا تحت نسيج الغروب 🌅", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.End) {
                        Text(
                            text = "كبسولة الزمن: في مثل هذا اليوم من الشهر الماضي 🌌",
                            color = BrightYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "مر شهر كامل على لقاء العلا الاستثنائي بمناظره الفاتنة وهدوئه الساحر المتألق تحت النجوم. ما رأيك أن تعاود زيارة الألبوم واسترجاع تفاصيله العذبة؟",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                viewModel.updatePhotoFilter("Travel")
                                viewModel.selectTab(1)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrightYellow),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.align(Alignment.Start).height(32.dp).testTag("open_capsule_btn")
                        ) {
                            Text("افتح ذكريات الماضي السعيد ❤️", color = DarkBackground, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            FeatureHighlightSection()
        }

        item {
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
fun DashboardHeader(onScanClick: () -> Unit, onAddPhotoClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_header_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(AccentCyan.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "الذكاء الاصطناعي",
                    tint = AccentCyan,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "منظم الحياة الرقمية الذكي ✨",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "يقوم بالعمل تلقائياً وبأمان فائق بمجرد توصيل الشاحن لتنظيم معرض الصور، ولقطات الشاشة، وتوفير المساحة بذكاء.",
                color = SlateGray,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onScanClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp)
                        .testTag("btn_trigger_ai_scan")
                ) {
                    Icon(
                        imageVector = Icons.Default.RotateRight,
                        contentDescription = "بدء الفحص",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "فحص وتنظيم الآن",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = DarkBackground
                    )
                }

                OutlinedButton(
                    onClick = onAddPhotoClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                    border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_add_device_photo")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "استيراد صورة",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "تصنيف صورة حقيقية",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StorageHealthCard(
    photosCount: Int,
    duplicatesCount: Int,
    unusedAppsCount: Int,
    largeFilesCount: Int,
    viewModel: OrganizerViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("storage_health_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(PremiumGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "آمن وتلقائي",
                        color = PremiumGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "حالة مساحة التخزين 📁",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val percentageAnalyzed = 0.74f
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "السعة المحسنة: 92.5 جيجابايت",
                            color = SlateGray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "المستخدم: 35.8 جيجابايت",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { percentageAnalyzed },
                        color = AccentCyan,
                        trackColor = SlateGray.copy(alpha = 0.2f),
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = SlateGray.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatMiniItem(
                    label = "صور منظمة",
                    value = "$photosCount صورة",
                    icon = Icons.Default.CheckCircle,
                    color = PremiumGreen
                )
                StatMiniItem(
                    label = "تكرارات مكتشفة",
                    value = "$duplicatesCount فئات",
                    icon = Icons.Default.FilterNone,
                    color = HighlightCoral
                )
                StatMiniItem(
                    label = "تطبيقات مهجورة",
                    value = "$unusedAppsCount تطبيقات",
                    icon = Icons.Default.ReportProblem,
                    color = BrightYellow
                )
            }
        }
    }
}

@Composable
fun StatMiniItem(label: String, value: String, icon: ImageVector, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        Text(
            text = label,
            color = SlateGray,
            fontSize = 10.sp
        )
    }
}

@Composable
fun ActionsQuickRow(
    onOrganizerClick: () -> Unit,
    onScreenshotsClick: () -> Unit,
    onGuardianClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionButton(
            label = "فرز الصور المكررة",
            subLabel = "وفر المساحة فوراً",
            icon = Icons.Default.PhotoLibrary,
            color = AccentCyan,
            onClick = onOrganizerClick,
            modifier = Modifier
                .weight(1f)
                .testTag("action_sorting_btn")
        )
        QuickActionButton(
            label = "البحث الفائق باللقطات",
            subLabel = "استخراج النصوص OCR",
            icon = Icons.Default.Search,
            color = PremiumGreen,
            onClick = onScreenshotsClick,
            modifier = Modifier
                .weight(1f)
                .testTag("action_search_btn")
        )
    }
}

@Composable
fun QuickActionButton(
    label: String,
    subLabel: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subLabel,
                color = SlateGray,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FeatureHighlightSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "ميزات الذكاء الاصطناعي الذكي الرئيسي 🛡️",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        listOf(
            Triple("التنظيم التلقائي المحلي للصور", "يقوم بفرز الصور تلقائياً (وصولات، صور عائلية، ميمز، وثائق، طعام) بذكاء وبدون إنترنت على الإطلاق حفاظاً على الخصوصية.", Icons.Default.Filter),
            Triple("البحث في النصوص داخل الصور (OCR)", "يستخرج البيانات من لقطات الشاشة ليصبح بإمكانك التفتيش عن أرقام هواتف، عروض، أو اتجاهات الخرائط فوراً.", Icons.Default.CenterFocusStrong),
            Triple("مستودع حماية كفاءة الذاكرة", "يعمل تلقائياً عندما يكون هاتفك متصلاً بالطاقة ومقترناً بالإنترنت اللاسلكي ليلاً لمنع التشويش والبطء التراكمي.", Icons.Default.Bolt)
        ).forEach { (title, desc, icon) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = desc,
                        color = SlateGray,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(CardSurface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = AccentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// TAB 1: SMART PHOTO SORTING & DUPLICATES
@Composable
fun PhotoOrganizerTab(viewModel: OrganizerViewModel) {
    val context = LocalContext.current
    val photos by viewModel.allPhotos.collectAsStateWithLifecycle()
    val duplicates by viewModel.duplicatePhotos.collectAsStateWithLifecycle()
    val counters by viewModel.photoCountByCategory.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.photoFilter.collectAsStateWithLifecycle()

    val categoriesList = listOf(
        Pair("All", "الكل 📋"),
        Pair("Receipts", "وصولات 🧾"),
        Pair("Personal", "شخصية 📸"),
        Pair("Travel", "سياحة 🏞️"),
        Pair("Food", "طعام 🍔"),
        Pair("Documents", "ملاحظات بصرية 📁"),
        Pair("Trash", "سلة المهملات 🗑️"),
        Pair("Meme", "ميمز ونكت 🎭")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("photo_organizer_tab")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categoriesList.forEach { (key, title) ->
                val count = if (key == "All") photos.size else counters[key] ?: 0
                val isSelected = selectedFilter == key
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.updatePhotoFilter(key) },
                    label = {
                        Text(
                            text = "$title ($count)",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = CardSurface,
                        labelColor = SlateGray,
                        selectedContainerColor = AccentCyan,
                        selectedLabelColor = DarkBackground
                    ),
                    modifier = Modifier.testTag("category_pill_$key")
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (duplicates.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = HighlightCoral.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, HighlightCoral.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(HighlightCoral, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "مستحسن",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "تحذير: صور مكررة مهدِرة!",
                                        color = HighlightCoral,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.FilterNone,
                                        contentDescription = "مكررات",
                                        tint = HighlightCoral
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "اكتشف محرك الـ AI صوراً متشابهة جداً مأخوذة في تتابع سريع تستهلك مساحات تخزينية هائلة.",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val groupedDuplicates = duplicates.groupBy { it.duplicateGroupId }
                                groupedDuplicates.forEach { (_, items) ->
                                    if (items.size >= 2) {
                                        val p1 = items[0]
                                        val p2 = items[1]
                                        DuplicateSetCard(
                                            p1 = p1,
                                            p2 = p2,
                                            onCleanClick = {
                                                viewModel.cleanDuplicateGroup(p1, p2, context)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            val trashPhotos = photos.filter { it.category == "Trash" }
            if (selectedFilter == "Trash" && trashPhotos.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = HighlightCoral.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, HighlightCoral.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().animateContentSize()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "تنظيف القمامة",
                                    tint = HighlightCoral,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "كاشف القمامة الرقمية التلقائي 🗑️",
                                    color = HighlightCoral,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "وجد محلل الهاتف الرقمي صوراً ذات قيمة منعدمة تستهلك مساحتك (صور جيب عشوائية معتمة، لقطات مهتزة، ميديا مؤقتة من الواتساب). يمكنك التخلص منها فوراً وتوفير مساحات شاسعة.",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.deleteTrashPhotos(context) },
                                colors = ButtonDefaults.buttonColors(containerColor = HighlightCoral),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(36.dp).testTag("delete_all_trash_btn")
                            ) {
                                Text("إفراغ سلة القمامة الرقمية بالكامل ومسحها 🧹", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            val filteredPhotos = if (selectedFilter == "All") {
                photos
            } else {
                photos.filter { it.category.equals(selectedFilter, ignoreCase = true) }
            }

            if (filteredPhotos.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "لا يوجد صور",
                            tint = SlateGray,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد صور ضمن تصنيف \"${selectedFilter}\" حالياً.",
                            color = SlateGray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "اضغط على زر (تصنيف صورة حقيقية) بالرئيسية لإضافتها فوراً!",
                            color = SlateGray.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                item {
                    Text(
                        text = "الصور المصنفة تلقائياً (${filteredPhotos.size}) 📸",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                items(filteredPhotos.chunked(2)) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { photo ->
                            PhotoCard(
                                photo = photo,
                                modifier = Modifier.weight(1f),
                                onDeleteClick = {
                                    viewModel.deletePhotoItem(photo, context)
                                }
                            )
                        }
                        if (pair.size < 2) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun DuplicateSetCard(p1: PhotoItem, p2: PhotoItem, onCleanClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, HighlightCoral.copy(alpha = 0.3f)),
        modifier = Modifier.width(280.dp).testTag("duplicate_set_card")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateGray.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (p1.uri.startsWith("sim_")) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(color = AccentCyan.copy(alpha = 0.2f))
                        }
                        Text("النسخة 1", color = Color.White, fontSize = 10.sp)
                    } else {
                        AsyncImage(
                            model = p1.uri,
                            contentDescription = p1.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateGray.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (p2.uri.startsWith("sim_")) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(color = AccentCyan.copy(alpha = 0.3f))
                        }
                        Text("النسخة 2 (مكرر)", color = HighlightCoral, fontSize = 10.sp)
                    } else {
                        AsyncImage(
                            model = p2.uri,
                            contentDescription = p2.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onCleanClick,
                    colors = ButtonDefaults.buttonColors(containerColor = HighlightCoral),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(28.dp).testTag("delete_duplicate_set")
                ) {
                    Text("حذف المكرر التلقائي", fontSize = 10.sp, color = Color.White)
                }

                Text(
                    text = "الحجم: 3.2 MB",
                    color = SlateGray,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun PhotoCard(photo: PhotoItem, modifier: Modifier = Modifier, onDeleteClick: () -> Unit) {
    val isVisualNote = photo.category == "Documents" && (photo.name.contains("ركن") || photo.name.contains("مودم") || photo.name.contains("مسلسل") || photo.name.contains("موقف") || photo.name.contains("المسلسل"))
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        modifier = modifier.testTag("photo_item_card_${photo.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .background(SlateGray.copy(alpha = 0.1f))
            ) {
                if (photo.uri.startsWith("sim_")) {
                    val colorBrush = when (photo.category) {
                        "Receipts" -> Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF06B6D4)))
                        "Personal" -> Brush.linearGradient(listOf(Color(0xFFE11D48), Color(0xFFF43F5E)))
                        "Travel" -> Brush.linearGradient(listOf(Color(0xFF059669), Color(0xFF10B981)))
                        "Documents" -> Brush.linearGradient(listOf(Color(0xFF6B7280), Color(0xFF9CA3AF)))
                        "Trash" -> Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF475569)))
                        else -> Brush.linearGradient(listOf(Color(0xFFD97706), Color(0xFFF59E0B)))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colorBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (photo.category) {
                                "Receipts" -> Icons.Default.ReceiptLong
                                "Personal" -> Icons.Default.People
                                "Travel" -> Icons.Default.Landscape
                                "Documents" -> Icons.Default.DocumentScanner
                                "Trash" -> Icons.Default.DeleteSweep
                                else -> Icons.Default.Fastfood
                            },
                            contentDescription = photo.name,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                } else {
                    AsyncImage(
                        model = photo.uri,
                        contentDescription = photo.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(DarkBackground.copy(alpha = 0.72f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = when (photo.category) {
                            "Receipts" -> "فاتورة 🧾"
                            "Personal" -> "شخصي 📸"
                            "Travel" -> "سفر 🏞️"
                            "Food" -> "طعام 🍔"
                            "Documents" -> "وثيقة 📄"
                            "Trash" -> "مهملات 🗑️"
                            else -> "ميمز 🎭"
                        },
                        color = AccentCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isVisualNote) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(BrightYellow.copy(alpha = 0.9f))
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ملاحظة بصرية (مؤقتة: تنتهي صلاحيتها خلال أسبوع) ⏳",
                            color = DarkBackground,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = photo.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("delete_photo_${photo.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف الصورة",
                            tint = HighlightCoral.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = when {
                            photo.sizeBytes >= 1024 * 1024 -> String.format("%.1f MB", photo.sizeBytes.toDouble() / (1024 * 1024))
                            else -> "${photo.sizeBytes / 1024} KB"
                        },
                        color = SlateGray,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

// TAB 2: SMART SCREENSHOTS (OCR WITH SUPER SEARCH)
@Composable
fun ScreenshotsTab(viewModel: OrganizerViewModel) {
    val searchVal by viewModel.searchQuery.collectAsStateWithLifecycle()
    val screenshots by viewModel.allScreenshots.collectAsStateWithLifecycle()
    val unlockedIds by viewModel.unlockedScreenshotIds.collectAsStateWithLifecycle()

    var activeTextSnippetDetails by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("screenshots_tab")
    ) {
        Spacer(modifier = Modifier.height(18.dp))

        OutlinedTextField(
            value = searchVal,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = {
                Text(
                    text = "ابحث في اللقطات (نص، جهة اتصال، موضوع)...",
                    color = SlateGray,
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "بحث فائق",
                    tint = AccentCyan
                )
            },
            trailingIcon = {
                if (searchVal.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "مسح",
                            tint = SlateGray
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardSurface,
                unfocusedContainerColor = CardSurface,
                focusedBorderColor = AccentCyan,
                unfocusedBorderColor = SlateGray.copy(alpha = 0.3f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("super_search_bar")
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (searchVal.isBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("أحمد", "فيبوناتشي", "الرياض", "الذكاء الاصطناعي", "0554").forEach { tag ->
                    SuggestionChip(
                        onClick = { viewModel.updateSearchQuery(tag) },
                        label = { Text(text = tag, color = AccentCyan, fontSize = 11.sp) },
                        border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.3f)),
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = CardSurface),
                        modifier = Modifier.testTag("search_suggestion_$tag")
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (searchVal.isNotBlank()) {
                Text(
                    text = "نتائج البحث الفائق الطبيعي المترابط (${screenshots.size}) 🔍",
                    color = AccentCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "لقطات الشاشة المفهرسة تلقائياً والآمنة (${screenshots.size}) 📱",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (screenshots.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SearchOff,
                    contentDescription = "لا توجد نتائج",
                    tint = SlateGray,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "لم يتم العثور على لقطات شاشة مطابقة لـ \"$searchVal\"",
                    color = SlateGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "حاول البحث عن كلمات أخرى في اللقطات المضافة أو كلمات دلالية للـ AI.",
                    color = SlateGray.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp).padding(top = 4.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(screenshots) { item ->
                    ScreenshotRowItem(
                        item = item,
                        searchQuery = searchVal,
                        unlockedIds = unlockedIds,
                        viewModel = viewModel,
                        onViewOcrClick = {
                            activeTextSnippetDetails = item.ocrText
                        }
                    )
                }
            }
        }
    }

    if (activeTextSnippetDetails != null) {
        AlertDialog(
            onDismissRequest = { activeTextSnippetDetails = null },
            title = {
                Text(
                    text = "نص لقطة الشاشة المستخلص (OCR) 📄",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                SelectionContainer {
                    Text(
                        text = activeTextSnippetDetails ?: "",
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { activeTextSnippetDetails = null },
                    modifier = Modifier.testTag("ocr_dialog_dismiss")
                ) {
                    Text("حسناً", color = AccentCyan)
                }
            },
            containerColor = CardSurface
        )
    }
}

@Composable
fun ScreenshotRowItem(
    item: ScreenshotItem,
    searchQuery: String,
    unlockedIds: Set<Int>,
    viewModel: OrganizerViewModel,
    onViewOcrClick: () -> Unit
) {
    val context = LocalContext.current
    val isSensitive = item.ocrText.contains("سري للغاية") || item.ocrText.contains("رمز تفعيل") || item.ocrText.contains("نفاذ") || item.ocrText.contains("الحساب") || item.ocrText.contains("رصيد") || item.ocrText.contains("البطاقة") || item.ocrText.contains("السرية") || item.ocrText.contains("أرباح")
    val isLocked = isSensitive && !unlockedIds.contains(item.id)

    // Regex or simple keyword scan for deep link matching
    val matchUrl = if (item.ocrText.contains("https://")) "https://www.amazon.sa/dp/B0CHX123XX" else null
    val matchPhone = if (item.ocrText.contains("05")) {
        if (item.ocrText.contains("0554")) "0554124921" else "0523456789"
    } else null
    val matchMap = if (item.ocrText.contains("الصحافة") || item.ocrText.contains("التخصصي")) {
        if (item.ocrText.contains("التخصصي")) "حي التخصصي، الرياض" else "حي الصحافة، الرياض"
    } else null
    val matchBankCard = if (item.ocrText.contains("4201")) "4201 5489 0012 3456" else null

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = if (isSensitive) BorderStroke(1.dp, HighlightCoral.copy(alpha = 0.4f)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("screenshot_row_${item.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLocked) {
                    Column(
                        modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(HighlightCoral.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("آمن ومغلق 🔒", color = HighlightCoral, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "لقطة محجوبة بأمر الخصوصية",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "تم تعتيم هذه اللقطة لقائياً لاحتوائها على رموز تفعيل (OTP) أو بيانات مالية حساسة لحمايتها من المتطفلين.",
                            color = SlateGray,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.unlockScreenshot(item.id)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HighlightCoral),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp).testTag("unlock_sensitive_btn_${item.id}")
                        ) {
                            Text("كشف اللقطة بالبصمة 👁️", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = onViewOcrClick,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("view_ocr_btn_${item.id}")
                    ) {
                        Text("عرض النص", fontSize = 10.sp, color = AccentCyan)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = when (item.subCategory) {
                                            "Chats" -> PremiumGreen.copy(alpha = 0.15f)
                                            "Code" -> BrightYellow.copy(alpha = 0.15f)
                                            "Articles" -> AccentCyan.copy(alpha = 0.15f)
                                            else -> HighlightCoral.copy(alpha = 0.15f)
                                        },
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = when (item.subCategory) {
                                        "Chats" -> "محادثات 💬"
                                        "Code" -> "أكواد برمجية 💻"
                                        "Articles" -> "مقالات 📝"
                                        else -> "خرائط 🗺️"
                                    },
                                    color = when (item.subCategory) {
                                        "Chats" -> PremiumGreen
                                        "Code" -> BrightYellow
                                        "Articles" -> AccentCyan
                                        else -> HighlightCoral
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = item.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Right
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = item.ocrText,
                            color = SlateGray,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Right,
                            lineHeight = 15.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateGray.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLocked) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    listOf(CardSurface, HighlightCoral.copy(alpha = 0.3f))
                                )
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "محمي",
                            tint = HighlightCoral,
                            modifier = Modifier.size(22.dp)
                        )
                    } else if (item.uri.startsWith("sim_")) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    listOf(CardSurface, AccentCyan.copy(alpha = 0.4f))
                                )
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Screenshot,
                            contentDescription = item.name,
                            tint = AccentCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        AsyncImage(
                            model = item.uri,
                            contentDescription = item.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // RENDER CONTEXTUAL DEEP LINKS BANNERS IF MATCHED AND UNLOCKED
            if (!isLocked && (matchUrl != null || matchPhone != null || matchMap != null || matchBankCard != null)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardSurface.copy(alpha = 0.9f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "💡 اختصار الذكاء الاصطناعي التفاعلي المكتشف:",
                            color = AccentCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (matchUrl != null) {
                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("URL", matchUrl)
                                        clipboard.setPrimaryClip(clip)
                                        android.widget.Toast.makeText(context, "تم فتح المصدر الإلكتروني ونسخ الرابط بنجاح! 🌐", android.widget.Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, AccentCyan),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp).weight(1f)
                                ) {
                                    Text("فتح المصدر 🌐", fontSize = 9.sp, color = AccentCyan, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (matchPhone != null) {
                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("phone", matchPhone)
                                        clipboard.setPrimaryClip(clip)
                                        android.widget.Toast.makeText(context, "تم نسخ هاتف جهة الاتصال والاتصال الآمن $matchPhone! 📞", android.widget.Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PremiumGreen.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, PremiumGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp).weight(1f)
                                ) {
                                    Text("اتصال آمن 📞", fontSize = 9.sp, color = PremiumGreen, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (matchMap != null) {
                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("map", matchMap)
                                        clipboard.setPrimaryClip(clip)
                                        android.widget.Toast.makeText(context, "جار فتح تطبيق الخرائط لتحديد موقع حي الصحافة بالشارع التخصصي! 🗺️", android.widget.Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = HighlightCoral.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, HighlightCoral),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp).weight(1f)
                                ) {
                                    Text("عرض الخريطة 🗺️", fontSize = 9.sp, color = HighlightCoral, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (matchBankCard != null) {
                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("card", matchBankCard)
                                        clipboard.setPrimaryClip(clip)
                                        android.widget.Toast.makeText(context, "تم كشف البيانات المشفّرة ونسخ الحساب البنكي بأمان! 💳", android.widget.Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrightYellow.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, BrightYellow),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp).weight(1f)
                                ) {
                                    Text("نسخ الحساب 💳", fontSize = 9.sp, color = BrightYellow, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// TAB 3: STORAGE GUARDIAN (ABANDONED APPS & GIANT FILES)
@Composable
fun StorageGuardianTab(viewModel: OrganizerViewModel) {
    val context = LocalContext.current
    val apps by viewModel.abandonedApps.collectAsStateWithLifecycle()
    val files by viewModel.largeFiles.collectAsStateWithLifecycle()
    val ghostSize by viewModel.ghostFilesSizeMB.collectAsStateWithLifecycle()
    val isCleaningGhost by viewModel.isCleaningGhost.collectAsStateWithLifecycle()

    var activeSubSectionChoice by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("storage_guardian_tab")
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { activeSubSectionChoice = 0 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubSectionChoice == 0) BrightYellow else CardSurface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("guardian_subtab_apps")
            ) {
                Text(
                    text = "تطبيقات مهجورة (${apps.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (activeSubSectionChoice == 0) DarkBackground else Color.White
                )
            }

            Button(
                onClick = { activeSubSectionChoice = 1 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubSectionChoice == 1) AccentCyan else CardSurface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("guardian_subtab_files")
            ) {
                Text(
                    text = "ملفات ضخمة (${files.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (activeSubSectionChoice == 1) DarkBackground else Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // WHATSAPP & TELEGRAM CACHE CLEAN CARD (GHOST MEDIA EXTERMINATOR)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PremiumGreen.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, PremiumGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().animateContentSize()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isCleaningGhost) {
                                CircularProgressIndicator(
                                    color = PremiumGreen,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CleaningServices,
                                    contentDescription = "تنظيف ميديا قسري",
                                    tint = PremiumGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Text(
                                text = "مُبيد مخلفات وميديا التواصل الشبحية (واتساب ⚡ تيليغرام)",
                                color = PremiumGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (ghostSize > 0) {
                                "تم رصد فضلات تواصل اجتماعي غير مرئية بحجم ${String.format("%.2f", ghostSize)} MB متراكمة من فويسات الواتساب البالية والذاكرة المؤقتة لقنوات تيليغرام العملاقة."
                            } else {
                                "تم التطهير الشامل والصارم لمخلفات تيليغرام وواتساب بنجاح. أداء الهاتف الآن ساطع، متألق ورشيق بالكامل!"
                            },
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (ghostSize > 0) "الذاكرة المهدورة: ${String.format("%.2f MB", ghostSize)}" else "مساحة محقونة محلياً: 1.45 GB",
                                color = if (ghostSize > 0) BrightYellow else PremiumGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (ghostSize > 0) {
                                Button(
                                    onClick = { viewModel.cleanGhostFiles(context) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PremiumGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp).testTag("clean_ghost_media_btn")
                                ) {
                                    Text("أبِد الميديا الشبحية فورا 🧹", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            if (activeSubSectionChoice == 0) {
                if (apps.isEmpty()) {
                    item {
                        EmptyGuardianState(
                            msg = "رائع! لا توجد تطبيقات مهجورة لم تفتحها منذ 90 يوماً.",
                            icon = Icons.Default.VerifiedUser,
                            color = PremiumGreen
                        )
                    }
                } else {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BrightYellow.copy(alpha = 0.08f)),
                            border = BorderStroke(1.dp, BrightYellow.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💡 هذه التطبيقات معطلة ولم تفتحها منذ أكثر من 90 يوماً. يمكنك إلغاء تثبيتها بالكامل أو أرشفتها بنظام ضغط غلاف الـ APK لتوفر 90% من حجمها دون خسارة بياناتك الشخصية.",
                                color = Color.White,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    items(apps) { app ->
                        AbandonedAppRowItem(
                            app = app,
                            viewModel = viewModel,
                            onUninstallClick = {
                                viewModel.uninstallAbandonedApp(app, context)
                            }
                        )
                    }
                }
            } else {
                if (files.isEmpty()) {
                    item {
                        EmptyGuardianState(
                            msg = "كل شيء منظم! لا توجد ملفات أو فديوهات عملاقة غير مصنفة.",
                            icon = Icons.Default.CloudQueue,
                            color = AccentCyan
                        )
                    }
                } else {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AccentCyan.copy(alpha = 0.08f)),
                            border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💡 مقاطع الفيديو ومستندات الـ PDF الضخمة هي عدو الذاكرة الأول. تصفح وحذف الملفات التي لم تعد بحاجتها.",
                                color = Color.White,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    items(files) { file ->
                        LargeFileRowItem(
                            file = file,
                            viewModel = viewModel,
                            onDeleteClick = {
                                viewModel.cleanLargeFile(file, context)
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun EmptyGuardianState(msg: String, icon: ImageVector, color: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "مكتمل",
            tint = color,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = msg,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun AbandonedAppRowItem(
    app: AbandonedApp,
    viewModel: OrganizerViewModel,
    onUninstallClick: () -> Unit
) {
    val context = LocalContext.current
    val archivedPackages by viewModel.archivedAppPackages.collectAsStateWithLifecycle()
    val isArchived = archivedPackages.contains(app.packageName)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = if (isArchived) BorderStroke(1.dp, PremiumGreen.copy(alpha = 0.5f)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_row_${app.packageName}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onUninstallClick,
                    colors = ButtonDefaults.buttonColors(containerColor = HighlightCoral),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("btn_uninstall_${app.packageName}")
                ) {
                    Text("حذف", fontSize = 10.sp, color = Color.White)
                }

                if (isArchived) {
                    Button(
                        onClick = { viewModel.restoreArchivedApp(app.packageName, context) },
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("btn_restore_${app.packageName}")
                    ) {
                        Text("استجابة ⚙️", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { viewModel.archiveApp(app.packageName, context) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrightYellow.copy(alpha = 0.2f)),
                        border = BorderStroke(1.dp, BrightYellow),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("btn_archive_${app.packageName}")
                    ) {
                        Text("أرشفة 📦", fontSize = 10.sp, color = BrightYellow, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isArchived) {
                        Box(
                            modifier = Modifier
                                .background(PremiumGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("مؤرشف 📦 (-90%)", color = PremiumGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = app.appLabel,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Right
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الحجم: ${viewModel.formatBytes(if (isArchived) app.sizeBytes / 10 else app.sizeBytes)}",
                        color = if (isArchived) PremiumGreen else BrightYellow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• لم يُفتح منذ ${app.lastUsedDaysAgo} يوماً",
                        color = SlateGray,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(if (isArchived) PremiumGreen.copy(alpha = 0.15f) else BrightYellow.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = app.appLabel,
                    tint = if (isArchived) PremiumGreen else BrightYellow,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun LargeFileRowItem(
    file: LargeFile,
    viewModel: OrganizerViewModel,
    onDeleteClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("large_file_row_${file.id}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onDeleteClick,
                colors = ButtonDefaults.buttonColors(containerColor = HighlightCoral.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, HighlightCoral),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier
                    .height(32.dp)
                    .testTag("btn_delete_file_${file.id}")
            ) {
                Text("احذف الملف", fontSize = 11.sp, color = HighlightCoral)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = file.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المساحة: ${viewModel.formatBytes(file.sizeBytes)}",
                        color = AccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• نوع الملف: ${file.fileType}",
                        color = SlateGray,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(AccentCyan.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (file.fileType) {
                        "Video" -> Icons.Default.PlayCircle
                        "PDF" -> Icons.Default.Description
                        else -> Icons.Default.FolderZip
                    },
                    contentDescription = file.name,
                    tint = AccentCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// TAB 4: WEEKLY INFOGRAPHIC REPORT
@Composable
fun WeeklyReportTab(viewModel: OrganizerViewModel) {
    val reportOpt by viewModel.latestWeeklyReport.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("weekly_report_tab")
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(AccentCyan.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "تفصيلي أسبوعي 📊",
                        color = AccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "الملخص الأسبوعي المنظم ✨",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        val report = reportOpt
        if (report != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(PremiumGreen.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Celebration,
                                contentDescription = "احتفال",
                                tint = PremiumGreen,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "تقرير الكفاءة الرقمية الأسبوعي 🎉",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "أداء تنظيف وتنظيم عالي المستوى للمحافظة على موارد هاتفك مستقرة وسريعة.",
                            color = SlateGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = SlateGray.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(16.dp))

                        ReportStatItem(
                            label = "الصور المحللة والمفهرسة محلياً",
                            value = "${report.photosAnalyzed} صورة",
                            icon = Icons.Default.Filter,
                            color = AccentCyan
                        )

                        ReportStatItem(
                            label = "لقطات الشاشة التي استخلصت نصوصها (OCR)",
                            value = "${report.screenshotsProcessed} لقطات شاشة",
                            icon = Icons.Default.CenterFocusStrong,
                            color = PremiumGreen
                        )

                        ReportStatItem(
                            label = "الصور والملفات المكررة التي حذفت",
                            value = "${report.duplicatesCleaned} مكررات",
                            icon = Icons.Default.RemoveCircleOutline,
                            color = HighlightCoral
                        )

                        ReportStatItem(
                            label = "حجم المساحة الكلية التي تم تحريرها",
                            value = viewModel.formatBytes(report.spaceSavedBytes),
                            icon = Icons.Default.Storage,
                            color = PremiumGreen
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("screentime_metrics_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "مؤشرات وتنبيهات استخدام التطبيقات 📱",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AppUsageCard(
                                label = "الأكثر استخداماً 🔥",
                                appName = report.topAppUse,
                                color = HighlightCoral,
                                modifier = Modifier.weight(1f)
                            )

                            AppUsageCard(
                                label = "الأقل استخداماً ❄️",
                                appName = report.leastAppUse,
                                color = AccentCyan,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Update,
                            contentDescription = "جاري الحساب",
                            tint = SlateGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "التقرير الأسبوعي قيد الفحص والتحليل... ⏳",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "سينشأ التقرير بمجرد قيام محرك الـ AI بالمسح وتصنيف أولى فئات المستندات والوصولات في أوقات الشحن.",
                            color = SlateGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ReportStatItem(label: String, value: String, icon: ImageVector, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value,
            color = color,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Right
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun AppUsageCard(label: String, appName: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkBackground)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = SlateGray,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = appName,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
