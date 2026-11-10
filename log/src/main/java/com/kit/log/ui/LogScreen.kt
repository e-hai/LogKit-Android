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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat

/**
 * 明暗双主题色彩配置 (完全自洽，不依赖任何 Material Design System)
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
    val dialogBackground: Color,
    val primary: Color
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
    dialogBackground = Color(0xFF1E1E1E),
    primary = Color(0xFF42A5F5)
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
    dialogBackground = Color(0xFFFFFFFF),
    primary = Color(0xFF1976D2)
)

@Composable
private fun LogText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = TextStyle(
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            lineHeight = lineHeight
        ),
        maxLines = maxLines,
        overflow = overflow
    )
}

@Composable
private fun LogText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = TextStyle(
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            lineHeight = lineHeight
        ),
        maxLines = maxLines,
        overflow = overflow
    )
}

@Composable
private fun LogIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Image(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        colorFilter = if (tint != Color.Unspecified) ColorFilter.tint(tint) else null
    )
}

@Composable
private fun LogIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@OptIn(ExperimentalFoundationApi::class)
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

    // 列表滚动状态
    val listState = rememberLazyListState()
    var isHeaderVisible by remember { mutableStateOf(true) }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .navigationBarsPadding()
    ) {
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
                    // 1. 顶栏 (纯 Foundation Row 构建，零 Material 依赖)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LogIconButton(onClick = onBackClick) {
                            LogIcon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "返回",
                                tint = themeColors.textPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        ) {
                            LogText(
                                text = "LogKit 日志控制台",
                                color = themeColors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val tagTitle = if (selectedTags.isEmpty()) {
                                "Tag: 全部标签"
                            } else {
                                "Tags (${selectedTags.size}): ${selectedTags.joinToString(", ")}"
                            }
                            LogText(
                                text = "$tagTitle (${displayedLogs.size}/${allLogs.size} 条)",
                                color = themeColors.textSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Tag 搜索折叠/展开按钮
                        LogIconButton(onClick = { isSearchExpanded = !isSearchExpanded }) {
                            LogIcon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "搜索过滤Tag",
                                tint = if (isSearchExpanded || selectedTags.isNotEmpty()) themeColors.primary else themeColors.textPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // 明/暗 主题一键切换按钮
                        LogIconButton(onClick = { isDarkTheme = !isDarkTheme }) {
                            LogText(
                                text = if (isDarkTheme) "☀️" else "🌙",
                                fontSize = 18.sp
                            )
                        }

                        // 手动收起 Header 控制栏按钮 (与 iOS 保持一致)
                        LogIconButton(onClick = { isHeaderVisible = false }) {
                            LogIcon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "收起控制栏",
                                tint = themeColors.textSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

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
                            // 搜索输入框 (使用 BasicTextField，纯 Foundation)
                            BasicTextField(
                                value = tagSearchQuery,
                                onValueChange = { tagSearchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = themeColors.textPrimary,
                                    fontSize = 13.sp
                                ),
                                cursorBrush = SolidColor(themeColors.primary),
                                decorationBox = { innerTextField ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(themeColors.chipBackground)
                                            .border(1.dp, themeColors.dividerColor, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        LogIcon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = themeColors.textSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(modifier = Modifier.weight(1f)) {
                                            if (tagSearchQuery.isEmpty()) {
                                                LogText(
                                                    text = "输入Tag名称模糊搜索...",
                                                    color = themeColors.textSecondary,
                                                    fontSize = 13.sp
                                                )
                                            }
                                            innerTextField()
                                        }
                                        if (tagSearchQuery.isNotEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable { tagSearchQuery = "" },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                LogIcon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = "清空",
                                                    tint = themeColors.textSecondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Tag 多选横向滚动条（自然字典序排列，支持点选勾选多选）
                            if (filteredTags.isEmpty()) {
                                LogText(
                                    text = "无匹配的标签",
                                    color = themeColors.textSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            } else {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(filteredTags) { tag ->
                                        val isSelected = selectedTags.contains(tag)
                                        val highlighted = buildHighlightedString(tag, tagSearchQuery)

                                        Box(
                                            modifier = Modifier
                                                .padding(vertical = 2.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) themeColors.primary else themeColors.chipBackground)
                                                .border(
                                                    1.dp,
                                                    if (isSelected) themeColors.primary else Color.Transparent,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    viewModel.toggleTag(tag)
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (isSelected) {
                                                    LogText(
                                                        text = "✓ ",
                                                        color = Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                LogText(
                                                    text = highlighted,
                                                    fontSize = 12.sp,
                                                    color = if (isSelected) Color.White else themeColors.textPrimary
                                                )
                                            }
                                        }
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // "ALL" 全选快捷开关
                        val isAllSelected = selectedLevels.size == allLevels.size
                        Box(
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isAllSelected) themeColors.primary else themeColors.chipBackground)
                                .clickable {
                                    selectedLevels = if (isAllSelected) setOf("ERROR") else allLevels.toSet()
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            LogText(
                                text = if (isAllSelected) "✓ 全部" else "全部",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isAllSelected) Color.White else (if (themeColors.isDark) Color(0xFF888888) else Color(0xFF666666))
                            )
                        }

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
                                    .padding(vertical = 2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) color else themeColors.chipBackground)
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
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                LogText(
                                    text = if (isSelected) "✓ $shortName ($count)" else "$shortName ($count)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.White else (if (themeColors.isDark) Color(0xFF888888) else Color(0xFF666666))
                                )
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
                            LogText(
                                text = "已选 (${selectedTags.size}):",
                                color = themeColors.primary,
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
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(themeColors.primary.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            LogText(
                                                text = tag,
                                                fontSize = 11.sp,
                                                color = themeColors.primary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { viewModel.removeTag(tag) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                LogIcon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "移除",
                                                    tint = themeColors.primary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .height(28.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { viewModel.clearAllTags() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                LogText("清空", fontSize = 11.sp, color = themeColors.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 分割线 (纯 Box 实现)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(themeColors.dividerColor)
                    )
                }
            }

            // 日志列表区域（在 Header 收起时自动沉浸全屏）
            if (displayedLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LogText(
                        text = if (selectedLevels.size < allLevels.size || selectedTags.isNotEmpty()) {
                            "未匹配到相关条件的日志"
                        } else {
                            "暂无相关日志记录"
                        },
                        color = themeColors.textSecondary,
                        fontSize = 14.sp
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
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(12.dp)
                    .size(40.dp)
                    .shadow(4.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(themeColors.primary.copy(alpha = 0.9f))
                    .clickable { isHeaderVisible = true },
                contentAlignment = Alignment.Center
            ) {
                LogIcon(
                    imageVector = Icons.Default.List,
                    contentDescription = "展开筛选控制栏",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(themeColors.cardBackground)
            .border(0.5.dp, themeColors.dividerColor, RoundedCornerShape(8.dp))
            .clickable { onCopyClick() }
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Priority Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(levelColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        LogText(
                            text = logInfo.priority,
                            color = levelColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Tag
                    LogText(
                        text = logInfo.tag,
                        color = themeColors.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // 时间
                LogText(
                    text = logInfo.dateString,
                    color = themeColors.textSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            // 日志消息
            SelectionContainer {
                LogText(
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
