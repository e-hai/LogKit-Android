package com.kit.log.ui

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat

/**
 * 明暗双主题色彩配置
 */
private data class LogThemeColors(
    val isDark: Boolean,
    val background: Color,
    val appBarBackground: Color,
    val filterBarBackground: Color,
    val chipBackground: Color,
    val cardBackground: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val dividerColor: Color,
    val dialogBackground: Color
)

private val DarkColors = LogThemeColors(
    isDark = true,
    background = Color(0xFF121212),
    appBarBackground = Color(0xFF1A1A1A),
    filterBarBackground = Color(0xFF161616),
    chipBackground = Color(0xFF2A2A2A),
    cardBackground = Color(0xFF1E1E1E),
    textPrimary = Color(0xFFE0E0E0),
    textSecondary = Color(0xFFA0A0A0),
    dividerColor = Color(0xFF2C2C2C),
    dialogBackground = Color(0xFF1E1E1E)
)

private val LightColors = LogThemeColors(
    isDark = false,
    background = Color(0xFFF5F5F5),
    appBarBackground = Color(0xFFFFFFFF),
    filterBarBackground = Color(0xFFEAEAEA),
    chipBackground = Color(0xFFE8E8E8),
    cardBackground = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF212121),
    textSecondary = Color(0xFF666666),
    dividerColor = Color(0xFFE0E0E0),
    dialogBackground = Color(0xFFFFFFFF)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(
    viewModel: LogViewModel,
    onBackClick: () -> Unit
) {
    val tags by viewModel.tagMSF.collectAsState()
    val logs by viewModel.logMSF.collectAsState()
    var selectedTag by remember { mutableStateOf(LogViewModel.TAG_SHOW_ALL_LOG) }
    var showTagDialog by remember { mutableStateOf(false) }

    // 明暗主题切换状态 (默认暗色主题)
    var isDarkTheme by remember { mutableStateOf(true) }
    val themeColors = if (isDarkTheme) DarkColors else LightColors

    // 动态调整 Android 沉浸式状态栏图标文字颜色
    val context = LocalContext.current
    val window = (context as? Activity)?.window
    SideEffect {
        window?.let { w ->
            val controller = WindowInsetsControllerCompat(w, w.decorView)
            controller.isAppearanceLightStatusBars = !isDarkTheme
        }
    }

    // 级别多选筛选 (默认全选)
    val allLevels = remember { listOf("VERBOSE", "DEBUG", "INFO", "WARN", "ERROR") }
    var selectedLevels by remember { mutableStateOf(allLevels.toSet()) }

    // 列表滚动状态（用于实现滑动时自动收起/展开 Header）
    val listState = rememberLazyListState()
    var isHeaderVisible by remember { mutableStateOf(true) }

    // 监测列表滚动方向与位置
    var previousIndex by remember { mutableStateOf(0) }
    var previousScrollOffset by remember { mutableStateOf(0) }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        val currentIndex = listState.firstVisibleItemIndex
        val currentOffset = listState.firstVisibleItemScrollOffset

        if (currentIndex > previousIndex || (currentIndex == previousIndex && currentOffset > previousScrollOffset + 15)) {
            // 向上滑动浏览：自动收起顶部 Header，释放 100% 列表全屏空间
            if (currentIndex > 0) {
                isHeaderVisible = false
            }
        } else if (currentIndex < previousIndex || (currentIndex == previousIndex && currentOffset < previousScrollOffset - 15)) {
            // 向下滑动或回到顶部：展开顶部 Header
            isHeaderVisible = true
        }

        previousIndex = currentIndex
        previousScrollOffset = currentOffset
    }

    // 依据 Level 过滤后的日志列表
    val displayedLogs = remember(logs, selectedLevels) {
        logs.filter { log ->
            isLevelSelected(log.priority, selectedLevels)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        color = themeColors.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {

                // 可动画折叠收起的沉浸式顶部控制区域（带有 statusBarsPadding 保证不被状态栏遮挡）
                AnimatedVisibility(
                    visible = isHeaderVisible,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .background(themeColors.appBarBackground)
                            .statusBarsPadding()
                    ) {
                        // 标准舒适 TopAppBar
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = "LogKit 日志控制台",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.textPrimary
                                        )
                                    )
                                    Text(
                                        text = "Tag: $selectedTag (${displayedLogs.size}/${logs.size} 条)",
                                        style = MaterialTheme.typography.bodySmall.copy(color = themeColors.textSecondary)
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = onBackClick) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "返回",
                                        tint = themeColors.textPrimary
                                    )
                                }
                            },
                            actions = {
                                // 明/暗 主题一键切换按钮
                                IconButton(onClick = { isDarkTheme = !isDarkTheme }) {
                                    Text(
                                        text = if (isDarkTheme) "☀️" else "🌙",
                                        fontSize = 18.sp
                                    )
                                }

                                // Tag 选择与搜索弹窗触发按钮
                                IconButton(onClick = { showTagDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.List,
                                        contentDescription = "选择Tag",
                                        tint = if (selectedTag != LogViewModel.TAG_SHOW_ALL_LOG) MaterialTheme.colorScheme.primary else themeColors.textPrimary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.smallTopAppBarColors(
                                containerColor = themeColors.appBarBackground
                            )
                        )

                        // 1. 级别 (Priority) 筛选开关 Chip 栏 (舒适触控尺寸)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(themeColors.filterBarBackground)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "级别 (${selectedLevels.size}/${allLevels.size}):",
                                color = if (selectedLevels.size < allLevels.size) MaterialTheme.colorScheme.primary else themeColors.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // 一键全选 ALL 按钮
                            val isAllSelected = selectedLevels.size == allLevels.size
                            FilterChip(
                                selected = isAllSelected,
                                onClick = { selectedLevels = allLevels.toSet() },
                                label = {
                                    Text(
                                        text = "ALL",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = themeColors.chipBackground,
                                    labelColor = themeColors.textSecondary,
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isAllSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    borderWidth = 1.dp
                                )
                            )

                            allLevels.forEach { level ->
                                val isSelected = selectedLevels.contains(level)
                                val color = getPriorityColor(level)
                                val shortName = when (level) {
                                    "VERBOSE" -> "V"
                                    "DEBUG" -> "D"
                                    "INFO" -> "I"
                                    "WARN" -> "W"
                                    "ERROR" -> "E"
                                    else -> level
                                }

                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedLevels = if (isSelected) {
                                            if (selectedLevels.size > 1) selectedLevels - level else selectedLevels
                                        } else {
                                            selectedLevels + level
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = if (isSelected) "✓ $shortName" else shortName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = themeColors.chipBackground,
                                        labelColor = if (themeColors.isDark) Color(0xFF666666) else Color(0xFF888888),
                                        selectedContainerColor = color,
                                        selectedLabelColor = Color.White
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Transparent,
                                        borderWidth = 1.dp
                                    )
                                )
                            }
                        }

                        // 2. 常用 Tag 横向快捷列表 (舒适按压体验)
                        if (tags.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(themeColors.filterBarBackground)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(tags) { tag ->
                                    val isSelected = tag == selectedTag
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedTag = tag
                                            viewModel.loadLog(tag)
                                        },
                                        label = { Text(tag) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = themeColors.chipBackground,
                                            labelColor = themeColors.textPrimary,
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        Divider(color = themeColors.dividerColor, thickness = 1.dp)
                    }
                }

                // 日志列表区域（在 Header 收起时自动沉浸全屏）
                if (displayedLogs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedLevels.size < allLevels.size) {
                                "未匹配到相关级别的日志"
                            } else {
                                "暂无相关日志记录"
                            },
                            color = themeColors.textSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = if (isHeaderVisible) 8.dp else 4.dp,
                            bottom = 8.dp,
                            start = 10.dp,
                            end = 10.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(displayedLogs) { logInfo ->
                            LogItemCard(
                                logInfo = logInfo,
                                themeColors = themeColors,
                                onCopyClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("LogKit", "${logInfo.dateString} [${logInfo.priority}] ${logInfo.tag}: ${logInfo.message}")
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "已复制日志至剪贴板", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }

            // 当 Header 处于收起状态时，右上角沉浸式显示迷你展开控制按钮
            if (!isHeaderVisible) {
                SmallFloatingActionButton(
                    onClick = { isHeaderVisible = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(12.dp),
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "展开筛选控制栏",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // 当 Tag 较多时专用的 Tag 搜索与过滤弹窗对话框
    if (showTagDialog) {
        TagSelectionDialog(
            tags = tags,
            selectedTag = selectedTag,
            themeColors = themeColors,
            onTagSelected = { tag ->
                selectedTag = tag
                viewModel.loadLog(tag)
            },
            onDismissRequest = { showTagDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagSelectionDialog(
    tags: List<String>,
    selectedTag: String,
    themeColors: LogThemeColors,
    onTagSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    var tagQuery by remember { mutableStateOf("") }
    val filteredTags = remember(tagQuery, tags) {
        if (tagQuery.isBlank()) {
            tags
        } else {
            tags.filter { it.contains(tagQuery, ignoreCase = true) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("关闭", color = MaterialTheme.colorScheme.primary)
            }
        },
        title = {
            Text(
                text = "查找并筛选 Tag 标签",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
            ) {
                // 搜索 Tag 的输入框
                OutlinedTextField(
                    value = tagQuery,
                    onValueChange = { tagQuery = it },
                    placeholder = { Text("输入 Tag 名称过滤...", color = themeColors.textSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = themeColors.textSecondary) },
                    trailingIcon = {
                        if (tagQuery.isNotEmpty()) {
                            IconButton(onClick = { tagQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "清除", tint = themeColors.textSecondary)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = themeColors.textPrimary,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = themeColors.dividerColor
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tag 网格列表（支持搜索高亮）
                if (filteredTags.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("未找到包含 \"$tagQuery\" 的 Tag", color = themeColors.textSecondary, fontSize = 13.sp)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredTags) { tag ->
                            val isSelected = tag == selectedTag
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    onTagSelected(tag)
                                    onDismissRequest()
                                },
                                label = {
                                    Text(
                                        text = buildHighlightedString(tag, tagQuery),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = themeColors.chipBackground,
                                    labelColor = themeColors.textPrimary,
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        },
        containerColor = themeColors.dialogBackground
    )
}

@Composable
private fun LogItemCard(
    logInfo: LogInfo,
    themeColors: LogThemeColors,
    onCopyClick: () -> Unit
) {
    val levelColor = getPriorityColor(logInfo.priority)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCopyClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Priority Badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = levelColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = logInfo.priority,
                            color = levelColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Tag
                    Text(
                        text = logInfo.tag,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // 时间
                Text(
                    text = logInfo.dateString,
                    color = themeColors.textSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            // 日志消息
            SelectionContainer {
                Text(
                    text = logInfo.message,
                    color = themeColors.textPrimary,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

/**
 * 构建含高亮底色的 AnnotatedString
 */
private fun buildHighlightedString(text: String, query: String): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)
    return buildAnnotatedString {
        var start = 0
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        while (start < text.length) {
            val index = lowerText.indexOf(lowerQuery, start)
            if (index == -1) {
                append(text.substring(start))
                break
            }
            if (index > start) {
                append(text.substring(start, index))
            }
            val end = index + query.length
            withStyle(
                SpanStyle(
                    background = Color(0xFFFFD54F), // 亮黄色背景
                    color = Color.Black,             // 深色文字
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(text.substring(index, end))
            }
            start = end
        }
    }
}

private fun isLevelSelected(logPriority: String, selectedLevels: Set<String>): Boolean {
    val norm = logPriority.uppercase()
    return when {
        norm.contains("V") || norm.contains("VERBOSE") -> selectedLevels.contains("VERBOSE")
        norm.contains("D") || norm.contains("DEBUG") -> selectedLevels.contains("DEBUG")
        norm.contains("I") || norm.contains("INFO") -> selectedLevels.contains("INFO")
        norm.contains("W") || norm.contains("WARN") -> selectedLevels.contains("WARN")
        norm.contains("E") || norm.contains("ERROR") -> selectedLevels.contains("ERROR")
        else -> true
    }
}

private fun getPriorityColor(priority: String): Color {
    return when (priority.uppercase()) {
        "V", "VERBOSE" -> Color(0xFF9E9E9E)
        "D", "DEBUG" -> Color(0xFF03A9F4)
        "I", "INFO" -> Color(0xFF4CAF50)
        "W", "WARN" -> Color(0xFFFF9800)
        "E", "ERROR" -> Color(0xFFF44336)
        "ASSERT", "WTF" -> Color(0xFFE91E63)
        else -> Color(0xFFB0BEC5)
    }
}
