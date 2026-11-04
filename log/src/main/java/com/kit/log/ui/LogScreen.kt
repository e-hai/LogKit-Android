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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LogScreen(
    viewModel: LogViewModel,
    onBackClick: () -> Unit
) {
    val allTags by viewModel.tagMSF.collectAsState()
    val allLogs by viewModel.logMSF.collectAsState()
    val selectedTags by viewModel.selectedTagsMSF.collectAsState()

    // 可折叠搜索栏状态
    var isSearchExpanded by remember { mutableStateOf(false) }
    var tagSearchQuery by remember { mutableStateOf("") }

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

    // 统计各 Level 在当前数据集中的条数
    val levelCounts = remember(allLogs) {
        val counts = mutableMapOf<String, Int>()
        allLevels.forEach { level -> counts[level] = 0 }
        allLogs.forEach { log ->
            val p = log.priority.uppercase()
            when {
                p.contains("V") || p.contains("VERBOSE") -> counts["VERBOSE"] = (counts["VERBOSE"] ?: 0) + 1
                p.contains("D") || p.contains("DEBUG") -> counts["DEBUG"] = (counts["DEBUG"] ?: 0) + 1
                p.contains("I") || p.contains("INFO") -> counts["INFO"] = (counts["INFO"] ?: 0) + 1
                p.contains("W") || p.contains("WARN") -> counts["WARN"] = (counts["WARN"] ?: 0) + 1
                p.contains("E") || p.contains("ERROR") -> counts["ERROR"] = (counts["ERROR"] ?: 0) + 1
            }
        }
        counts
    }

    // 列表滚动状态（用于实现滑动时自动收起/展开 Header）
    val listState = rememberLazyListState()
    var isHeaderVisible by remember { mutableStateOf(true) }

    var previousIndex by remember { mutableStateOf(0) }
    var previousScrollOffset by remember { mutableStateOf(0) }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        val currentIndex = listState.firstVisibleItemIndex
        val currentOffset = listState.firstVisibleItemScrollOffset

        if (currentIndex > previousIndex || (currentIndex == previousIndex && currentOffset > previousScrollOffset + 15)) {
            if (currentIndex > 0) {
                isHeaderVisible = false
            }
        } else if (currentIndex < previousIndex || (currentIndex == previousIndex && currentOffset < previousScrollOffset - 15)) {
            isHeaderVisible = true
        }

        previousIndex = currentIndex
        previousScrollOffset = currentOffset
    }

    // 依据 Level 过滤后的日志列表
    val displayedLogs = remember(allLogs, selectedLevels) {
        allLogs.filter { log ->
            isLevelSelected(log.priority, selectedLevels)
        }
    }

    // 按输入关键词过滤的 Tag 列表（保持原自然字典顺序，不作热度重排）
    val filteredTags = remember(tagSearchQuery, allTags) {
        val rawList = allTags.filter { it != LogViewModel.TAG_SHOW_ALL_LOG }
        if (tagSearchQuery.isBlank()) {
            rawList
        } else {
            rawList.filter { it.contains(tagSearchQuery, ignoreCase = true) }
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

                // 顶部可收起折叠 Header
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
                        // 1. 顶栏 TopAppBar
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
                                    val tagTitle = if (selectedTags.isEmpty()) {
                                        "Tag: 全部标签"
                                    } else {
                                        "Tags (${selectedTags.size}): ${selectedTags.joinToString(", ")}"
                                    }
                                    Text(
                                        text = "$tagTitle (${displayedLogs.size}/${allLogs.size} 条)",
                                        style = MaterialTheme.typography.bodySmall.copy(color = themeColors.textSecondary),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
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
                                // Tag 搜索折叠/展开按钮
                                IconButton(onClick = { isSearchExpanded = !isSearchExpanded }) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "搜索过滤Tag",
                                        tint = if (isSearchExpanded || selectedTags.isNotEmpty()) MaterialTheme.colorScheme.primary else themeColors.textPrimary
                                    )
                                }

                                // 明/暗 主题一键切换按钮
                                IconButton(onClick = { isDarkTheme = !isDarkTheme }) {
                                    Text(
                                        text = if (isDarkTheme) "☀️" else "🌙",
                                        fontSize = 18.sp
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.smallTopAppBarColors(
                                containerColor = themeColors.appBarBackground
                            )
                        )

                        // 2. 可折叠的 Tag 搜索与多选操作栏
                        AnimatedVisibility(
                            visible = isSearchExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(themeColors.filterBarBackground)
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                // 搜索输入框
                                OutlinedTextField(
                                    value = tagSearchQuery,
                                    onValueChange = { tagSearchQuery = it },
                                    placeholder = { Text("搜索并多选 Tag...", color = themeColors.textSecondary, fontSize = 13.sp) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = themeColors.textSecondary) },
                                    trailingIcon = {
                                        if (tagSearchQuery.isNotEmpty()) {
                                            IconButton(onClick = { tagSearchQuery = "" }) {
                                                Icon(Icons.Default.Clear, contentDescription = "清空", tint = themeColors.textSecondary)
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        textColor = themeColors.textPrimary,
                                        cursorColor = MaterialTheme.colorScheme.primary,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = themeColors.dividerColor
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Tag 多选滚动条（自然字典序排列，支持点选勾选多选）
                                if (filteredTags.isEmpty()) {
                                    Text(
                                        text = "未找到匹配 \"$tagSearchQuery\" 的标签",
                                        color = themeColors.textSecondary,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                } else {
                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // 全选 / 清空全部按钮
                                        item {
                                            FilterChip(
                                                selected = selectedTags.isEmpty(),
                                                onClick = { viewModel.clearAllTags() },
                                                label = { Text("全部 Tag", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    containerColor = themeColors.chipBackground,
                                                    labelColor = themeColors.textPrimary,
                                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }

                                        items(filteredTags) { tag ->
                                            val isSelected = selectedTags.contains(tag)
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { viewModel.toggleTag(tag) },
                                                label = {
                                                    val prefix = if (isSelected) "✓ " else ""
                                                    val highlighted = buildHighlightedString(tag, tagSearchQuery)
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        if (prefix.isNotEmpty()) {
                                                            Text(prefix, fontWeight = FontWeight.Bold)
                                                        }
                                                        Text(highlighted, fontSize = 12.sp)
                                                    }
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    containerColor = themeColors.chipBackground,
                                                    labelColor = themeColors.textPrimary,
                                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                    selectedLabelColor = Color.White
                                                ),
                                                border = FilterChipDefaults.filterChipBorder(
                                                    borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                    borderWidth = 1.dp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. 级别 (Priority) 筛选开关 Chip 栏 (支持短按多选 + 长按独占只看该级别)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(themeColors.filterBarBackground)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "级别:",
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
                                val count = levelCounts[level] ?: 0

                                Box(
                                    modifier = Modifier
                                        .combinedClickable(
                                            onClick = {
                                                // 短按：常规多选/反选切换
                                                selectedLevels = if (isSelected) {
                                                    if (selectedLevels.size > 1) selectedLevels - level else selectedLevels
                                                } else {
                                                    selectedLevels + level
                                                }
                                            },
                                            onLongClick = {
                                                // 长按：独占该级别 (Solo Mode)！若已是独占态则恢复全选
                                                selectedLevels = if (selectedLevels == setOf(level)) {
                                                    allLevels.toSet()
                                                } else {
                                                    setOf(level)
                                                }
                                                Toast.makeText(context, if (selectedLevels.size == 1) "已独占过滤: 只看 $level" else "已恢复全选级别", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) color else themeColors.chipBackground,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isSelected) "✓ $shortName ($count)" else "$shortName ($count)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (isSelected) Color.White else (if (themeColors.isDark) Color(0xFF888888) else Color(0xFF666666)),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 4. 活动 Tag 胶囊栏（当选中了具体 Tag 时常驻显示，支持单个一键点击 ✕ 清除）
                        if (selectedTags.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(themeColors.filterBarBackground)
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "已选 (${selectedTags.size}):",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 6.dp)
                                )

                                LazyRow(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    items(selectedTags.toList()) { tag ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = tag,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                IconButton(
                                                    onClick = { viewModel.removeTag(tag) },
                                                    modifier = Modifier.size(16.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "移除",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                TextButton(
                                    onClick = { viewModel.clearAllTags() },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("清空", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
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
                            text = if (selectedLevels.size < allLevels.size || selectedTags.isNotEmpty()) {
                                "未匹配到相关条件的日志"
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
