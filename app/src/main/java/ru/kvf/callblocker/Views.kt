@file:OptIn(ExperimentalMaterial3Api::class)

package ru.kvf.callblocker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Main(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Text("time to work")
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
