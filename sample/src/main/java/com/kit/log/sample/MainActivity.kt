package com.kit.log.sample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kit.log.LogKit

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化全功能日志（Logcat 美化打印 + 本地磁盘持久化，支持应用内 Compose 控制台）
        LogKit.initAllLog(this)

        // 输出应用初始启动测试日志
        LogKit.v("APP", "LogKit Android Demo 启动初始化完成")
        LogKit.d("CONFIG", "加载初始配置: debug=true, env=development")
        LogKit.i("SYSTEM", "Android SDK: ${android.os.Build.VERSION.SDK_INT}, 设备: ${android.os.Build.MODEL}")
        LogKit.w("STORAGE", "检测到临时缓存目录占用 24.5MB")
        LogKit.e("NETWORK", "模拟网络连接尝试 (备用通道)")

        setContent {
            MaterialTheme {
                DemoMainScreen(
                    onOpenLogUi = { LogKit.showLogUi(this) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoMainScreen(
    onOpenLogUi: () -> Unit
) {
    val context = LocalContext.current
    var logCounter by remember { mutableStateOf(1) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LogKit Demo", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Header Banner
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = ">_",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = "LogKit Android Demo",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "纯 Kotlin 打造 · 原生 Jetpack Compose 调试控制台",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 2. Control Panel: Open Inspector
            Button(
                onClick = onOpenLogUi,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "打开应用内日志控制台",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            // 3. Quick Log Generators Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "日志输出测试",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 6 Level Buttons (2 列网格)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LevelTestButton(
                            title = "VERBOSE",
                            color = Color(0xFF9E9E9E),
                            modifier = Modifier.weight(1f)
                        ) {
                            LogKit.v("TAG_V", "这是第 $logCounter 条 Verbose 详细跟踪日志")
                            Toast.makeText(context, "已输出 Verbose 日志 #$logCounter", Toast.LENGTH_SHORT).show()
                            logCounter++
                        }

                        LevelTestButton(
                            title = "DEBUG",
                            color = Color(0xFF03A9F4),
                            modifier = Modifier.weight(1f)
                        ) {
                            LogKit.d("TAG_DEBUG", "调试数据 dump: id=$logCounter, time=${System.currentTimeMillis()}")
                            Toast.makeText(context, "已输出 Debug 日志 #$logCounter", Toast.LENGTH_SHORT).show()
                            logCounter++
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LevelTestButton(
                            title = "INFO",
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f)
                        ) {
                            LogKit.i("USER_ACTION", "用户点击了操作按钮, counter=$logCounter")
                            Toast.makeText(context, "已输出 Info 日志 #$logCounter", Toast.LENGTH_SHORT).show()
                            logCounter++
                        }

                        LevelTestButton(
                            title = "WARN",
                            color = Color(0xFFFF9800),
                            modifier = Modifier.weight(1f)
                        ) {
                            LogKit.w("CACHE_WARN", "缓存空间即将超限 (阈值: 80%)")
                            Toast.makeText(context, "已输出 Warn 日志 #$logCounter", Toast.LENGTH_SHORT).show()
                            logCounter++
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LevelTestButton(
                            title = "ERROR",
                            color = Color(0xFFF44336),
                            modifier = Modifier.weight(1f)
                        ) {
                            LogKit.e("NETWORK_ERR", "HTTP 请求失败: 502 Bad Gateway")
                            Toast.makeText(context, "已输出 Error 日志 #$logCounter", Toast.LENGTH_SHORT).show()
                            logCounter++
                        }

                        LevelTestButton(
                            title = "WTF / ASSERT",
                            color = Color(0xFFE91E63),
                            modifier = Modifier.weight(1f)
                        ) {
                            LogKit.wtf("CRITICAL", "捕获到无法恢复的断言状态")
                            Toast.makeText(context, "已输出 WTF 日志 #$logCounter", Toast.LENGTH_SHORT).show()
                            logCounter++
                        }
                    }

                    // Structured JSON / XML Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                LogKit.json("{\"orderId\": \"ORD_99812\", \"amount\": 199.9, \"items\": [{\"id\": 1, \"name\": \"Pro Member\"}], \"status\": \"paid\"}")
                                Toast.makeText(context, "已输出 JSON 格式化日志", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF9C27B0).copy(alpha = 0.15f),
                                contentColor = Color(0xFF9C27B0)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("打印 JSON", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                LogKit.xml("<response><status code=\"200\">OK</status><data count=\"2\"><item>Android</item><item>Google</item></data></response>")
                                Toast.makeText(context, "已输出 XML 格式化日志", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF009688).copy(alpha = 0.15f),
                                contentColor = Color(0xFF009688)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("打印 XML", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }

                    // 15 Mixed Logs Batch
                    Button(
                        onClick = {
                            val tags = listOf("AUTH", "PAYMENT", "DATABASE", "PUSH", "UI")
                            for (i in 1..15) {
                                val tag = tags[i % tags.size]
                                when (i % 5) {
                                    0 -> LogKit.v(tag, "批量生成跟踪日志: index=$i")
                                    1 -> LogKit.d(tag, "批量生成调试日志: query took ${i * 1.5}ms")
                                    2 -> LogKit.i(tag, "批量生成提示日志: operation finished")
                                    3 -> LogKit.w(tag, "批量生成警告日志: retry count=$i")
                                    else -> LogKit.e(tag, "批量生成错误日志: timeout code=ETIMEDOUT")
                                }
                            }
                            Toast.makeText(context, "已成功批量生成 15 条混合日志", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Text(
                            text = "⚡ 一键批量生成 15 条混合日志",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelTestButton(
    title: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.15f),
            contentColor = color
        ),
        contentPadding = PaddingValues(vertical = 10.dp),
        modifier = modifier
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
