package ru.kvf.callblocker

import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import ru.kvf.callblocker.data.Contact
import ru.kvf.callblocker.data.ContactsStorage
import ru.kvf.callblocker.ui.theme.CallBlockerTheme

private const val NOTIFICATION_PERMISSION = android.Manifest.permission.POST_NOTIFICATIONS
private const val NOTIFICATION_PERMISSION_CODE = 100

private const val CONTACTS_PERMISSION = android.Manifest.permission.READ_CONTACTS
private const val CONTACTS_PERMISSION_CODE = 101

private const val PHONE_STATE_PERMISSION = android.Manifest.permission.READ_PHONE_STATE
private const val PHONE_STATE_PERMISSION_CODE = 102

class MainActivity : ComponentActivity() {
    private var isAppSetAsCallBlocker by mutableStateOf(false)
    private var isNotificationPermissionGranted by mutableStateOf(false)
    private var isContactsPermissionGranted by mutableStateOf(false)
    private var isPhoneStatePermissionGranted by mutableStateOf(false)
    private val isAllOtherPermissionsGranted by derivedStateOf {
        isAppSetAsCallBlocker && isNotificationPermissionGranted && isContactsPermissionGranted
                && isPhoneStatePermissionGranted
    }

    private var isSomePermissionBlockedBySystem by mutableStateOf(false)


    private lateinit var roleManager: RoleManager
    private lateinit var requestRoleLauncher: ActivityResultLauncher<Intent>

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        enableEdgeToEdge()
        setContent {
            CallBlockerTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(appBackground),
                ) {
                    if (isAllOtherPermissionsGranted) {
                        Main()
                    } else {
                        AppNotSetAsCallBlockerView(
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
                            },
                            isPhoneStatePermissionGranted = isPhoneStatePermissionGranted,
                            askPhoneStatePermission = {
                                requestPermission(
                                    PHONE_STATE_PERMISSION,
                                    PHONE_STATE_PERMISSION_CODE,
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
            CONTACTS_PERMISSION_CODE -> {
                isContactsPermissionGranted = true
                loadContactPhones()
            }

            PHONE_STATE_PERMISSION_CODE -> isPhoneStatePermissionGranted = true
        }
    }

    private fun init() {
        roleManager = getSystemService(ROLE_SERVICE) as RoleManager
        requestRoleLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val granted = it.resultCode == -1
                isAppSetAsCallBlocker = granted
            }
        checkAppSetAsCallBlocker()

        isNotificationPermissionGranted =
            checkPermission(NOTIFICATION_PERMISSION)
        isContactsPermissionGranted = checkPermission(CONTACTS_PERMISSION)
        isPhoneStatePermissionGranted = checkPermission(PHONE_STATE_PERMISSION)

        if (isContactsPermissionGranted) loadContactPhones()
    }

    private fun requestPermission(permission: String, requestCode: Int) = requestPermissions(
        arrayOf(permission), requestCode
    )


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
            data = "package:${packageName}".toUri()
        }
        startActivity(intent)
    }

    private fun loadContactPhones() {
        ContactsStorage.subscribeToBlockedCalls(this)

        val contacts = mutableListOf<Contact>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val resolver = contentResolver
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                val number = it.getString(numberIndex).filterNumber()

                if (!number.isNullOrBlank()) {
                    contacts.add(Contact(name, number))
                }
            }
        }

        ContactsStorage.saveContacts(this, contacts)
    }
}


