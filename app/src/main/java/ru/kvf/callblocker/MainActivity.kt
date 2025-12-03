package ru.kvf.callblocker

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import ru.kvf.callblocker.ui.AppNotSetAsCallBlockerView
import ru.kvf.callblocker.ui.Main
import ru.kvf.callblocker.ui.theme.CallBlockerTheme

private const val NOTIFICATION_PERMISSION = android.Manifest.permission.POST_NOTIFICATIONS
private const val NOTIFICATION_PERMISSION_CODE = 100

private const val CONTACTS_PERMISSION = android.Manifest.permission.READ_CONTACTS
private const val CONTACTS_PERMISSION_CODE = 101

class MainActivity : ComponentActivity() {
    private var isAppSetAsCallBlocker by mutableStateOf(false)
    private var isNotificationPermissionGranted by mutableStateOf(false)
    private var isContactsPermissionGranted by mutableStateOf(false)
    private val isAllOtherPermissionsGranted by derivedStateOf {
        isAppSetAsCallBlocker && isNotificationPermissionGranted && isContactsPermissionGranted
    }

    private var isSomePermissionBlockedBySystem by mutableStateOf(false)


    private lateinit var roleManager: RoleManager
    private lateinit var requestRoleLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        enableEdgeToEdge()
        setContent {
            CallBlockerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    val modifier = Modifier
                        .padding(innerPadding)
                    if (isAllOtherPermissionsGranted) {
                        Main(modifier)
                    } else {
                        AppNotSetAsCallBlockerView(
                            modifier = modifier,
                            isSomePermissionBlockedBySystem = isSomePermissionBlockedBySystem,
                            onSettingsClick = ::openAppSettings,
                            onSetAsBlockAppClick = ::checkAppSetAsCallBlocker,
                            isAppSetAsCallBlocker = isAppSetAsCallBlocker,
                            isNotificationPermissionGranted = isNotificationPermissionGranted,
                            askNotificationPermission = {
                                requestPermission(
                                    NOTIFICATION_PERMISSION,
                                    NOTIFICATION_PERMISSION_CODE
                                )
                            },
                            isContactsPermissionGranted = isContactsPermissionGranted,
                            askContactsPermission = {
                                requestPermission(
                                    CONTACTS_PERMISSION,
                                    CONTACTS_PERMISSION_CODE
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        val result = grantResults.getOrNull(0) == PackageManager.PERMISSION_GRANTED

        if (!result) {
            isSomePermissionBlockedBySystem = true
            return
        }

        when (requestCode) {
            NOTIFICATION_PERMISSION_CODE -> isNotificationPermissionGranted = true
            CONTACTS_PERMISSION_CODE -> isContactsPermissionGranted = true
        }
    }

    private fun init() {
        roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
        requestRoleLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val granted = it.resultCode == -1
                isAppSetAsCallBlocker = granted
            }
        checkAppSetAsCallBlocker()

        isNotificationPermissionGranted =
            checkPermission(NOTIFICATION_PERMISSION)
        isContactsPermissionGranted = checkPermission(CONTACTS_PERMISSION)
    }

    private fun requestPermission(permission: String, requestCode: Int) {
        requestPermissions(
            arrayOf(permission), requestCode
        )
    }

    private fun checkPermission(permission: String): Boolean = ContextCompat.checkSelfPermission(
        this,
        permission
    ) == PackageManager.PERMISSION_GRANTED

    private fun checkAppSetAsCallBlocker() {
        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
        requestRoleLauncher.launch(intent)
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${packageName}")
        }
        startActivity(intent)
    }
}


