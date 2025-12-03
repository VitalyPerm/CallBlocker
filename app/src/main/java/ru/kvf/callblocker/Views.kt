@file:OptIn(ExperimentalMaterial3Api::class)

package ru.kvf.callblocker

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.kvf.callblocker.data.ContactsStorage
import java.text.SimpleDateFormat
import java.util.Locale

private val dateTimeFormatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
private val appBackground = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1a2980),
        Color(0xFF26d0ce)
    ),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
)

@Composable
fun Main(modifier: Modifier = Modifier) {
    val blockedCalls by ContactsStorage.blockedCallsFlow.collectAsState()
    val isBlockingEnable by ContactsStorage.blockedCallsEnableFlow.collectAsState()
    val context = LocalContext.current
    val clipboard =
        remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    val scrollState = rememberScrollState()
    val appBarAlpha by remember {
        derivedStateOf {
            val offset = scrollState.value.toFloat()
            if (offset <= 0) {
                1f
            } else if (offset >= 100) {
                0f
            } else {
                1f - (offset / 100f)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appBackground)
    ) {
        Column(
            modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                title = {
                    val text = if (isBlockingEnable) {
                        "Блокировка включена"
                    } else {
                        "Блокировка выключена"
                    }
                    Text(text)
                },
                actions = {
                    val icon = if (isBlockingEnable) {
                        Icons.Default.Done
                    } else {
                        Icons.Default.Close
                    }

                    IconButton(
                        onClick = { ContactsStorage.changeIsBlockingEnable(context) }
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.Red
                        )
                    }
                },
                modifier = Modifier
                    .alpha(appBarAlpha)
                    .drawBehind {
                        drawLine(
                            color = Color.Red,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = density * 3f
                        )
                    }
            )
            Text(
                "Заблокированные номера",
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            )
            blockedCalls.onEach { call ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .border(5.dp, Color.White)
                        .clickable {
                            clipboard.setPrimaryClip(
                                ClipData.newPlainText(
                                    "Заблокированный номер",
                                    call.phone ?: ""
                                )
                            )
                            Toast.makeText(context, "Номер скопирован", Toast.LENGTH_SHORT).show()
                        }
                ) {
                    Text(
                        call.phone ?: "",
                        color = Color.White,
                        fontStyle = FontStyle.Italic,
                        fontSize = 22.sp,
                        modifier = Modifier
                            .padding(16.dp)
                    )
                    Text(
                        dateTimeFormatter.format(call.date),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AppNotSetAsCallBlockerView(
    modifier: Modifier = Modifier,
    isSomePermissionBlockedBySystem: Boolean,
    onSettingsClick: () -> Unit,
    onSetAsBlockAppClick: () -> Unit,
    isAppSetAsCallBlocker: Boolean,
    isNotificationPermissionGranted: Boolean,
    askNotificationPermission: () -> Unit,
    isContactsPermissionGranted: Boolean,
    askContactsPermission: () -> Unit,
    isPhoneStatePermissionGranted: Boolean,
    askPhoneStatePermission: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(appBackground)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                "Дай необходимые разрешения",
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                color = Color.White,
                modifier = Modifier
                    .padding(20.dp)
            )
            if (isSomePermissionBlockedBySystem) {
                PermissionButton(
                    onClick = onSettingsClick,
                    title = "Дай разрешения через настройки"
                )
            }
            if (!isAppSetAsCallBlocker) {
                PermissionButton(
                    onClick = onSetAsBlockAppClick,
                    title = "Установи приложение блокровщиком звонком по умолчанию"
                )
            }

            if (!isNotificationPermissionGranted) {
                PermissionButton(
                    onClick = askNotificationPermission,
                    title = "Уведомления"
                )
            }


            if (!isContactsPermissionGranted) {
                PermissionButton(
                    onClick = askContactsPermission,
                    title = "Контакты"
                )
            }

            if (!isPhoneStatePermissionGranted) {
                PermissionButton(
                    onClick = askPhoneStatePermission,
                    title = "Перехват звонков"
                )
            }
        }
    }
}

@Composable
private fun PermissionButton(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .padding(20.dp)
    ) {
        Text(
            title,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            color = Color.Red,
            modifier = Modifier
                .padding(16.dp)
        )
    }
}
