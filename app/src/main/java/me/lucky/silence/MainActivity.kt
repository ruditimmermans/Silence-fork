package me.lucky.silence

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import me.lucky.silence.ui.App

// Define a constant to identify the SEND_SMS permission request
private const val MY_PERMISSIONS_REQUEST_SEND_SMS = 0

@SuppressLint("BatteryLife")
open class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationManager(this).createNotificationChannels()
        // Check and request SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // If not, request the permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), MY_PERMISSIONS_REQUEST_SEND_SMS)
        }

        // Ignore Battery Optimization
        val pm = getSystemService(PowerManager::class.java)
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = "package:$packageName".toUri()
            startActivity(intent)
        }

        setContent {
            val view = LocalView.current
            val isAndroid12OrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            val isDarkMode = isSystemInDarkTheme()
            val colorScheme = when {
                isAndroid12OrLater && isDarkMode -> dynamicDarkColorScheme(this)
                isAndroid12OrLater -> dynamicLightColorScheme(this)
                isDarkMode -> darkColorScheme()
                else -> lightColorScheme()
            }
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    window.statusBarColor = colorScheme.surface.toArgb()
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkMode
                }
            }
            MaterialTheme(colorScheme = colorScheme) {
                App(ctx = this, navController = rememberNavController())
            }
        }
    }

   // This method is called when the user responds to the permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            // If the result is for the SEND_SMS permission request
            MY_PERMISSIONS_REQUEST_SEND_SMS -> {
                // If the permission is granted
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Show a dialog to check Settings
                    AlertDialog.Builder(this)
                        .setTitle(R.string.AlertTitle)
                        .setMessage(R.string.AlertMessage)
                        .setPositiveButton("OK") { dialog, _ ->
                            // Dismiss the dialog when the OK button is clicked
                            dialog.dismiss()
                        }
                        .show()
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }
}
