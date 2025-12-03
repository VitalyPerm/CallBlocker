@file:OptIn(ExperimentalMaterial3Api::class)

package ru.kvf.callblocker

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun Main(modifier: Modifier = Modifier) {
    val blockedCalls by ContactsStorage.blockedCallsFlow.collectAsState()
    val context = LocalContext.current
    val clipboard =
        remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Заблокированные номера",
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
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
                    .border(5.dp, Color.Red)
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
                    modifier = Modifier
                        .padding(16.dp)
                )
                Text(
                    dateTimeFormatter.format(call.date),
                    modifier = Modifier
                        .padding(16.dp)
                )
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
    isCallListPermissionGranted: Boolean,
    askCallListPermission: () -> Unit,
    isPhoneStatePermissionGranted: Boolean,
    askPhoneStatePermission: () -> Unit,
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
    ) {
        Text(
            "Дай необходимые разрешения",
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
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

        if (!isCallListPermissionGranted) {
            PermissionButton(
                onClick = askCallListPermission,
                title = "Список звонков"
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
            modifier = Modifier
                .padding(16.dp)
        )
    }
}
