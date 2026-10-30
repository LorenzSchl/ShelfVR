package de.lorenzschl.shelfvr

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import de.lorenzschl.shelfvr.core.designsystem.ShelfVrTheme
import de.lorenzschl.shelfvr.core.model.PlaybackSnapshot
import de.lorenzschl.shelfvr.playback.PlaybackClient
import de.lorenzschl.shelfvr.playback.PlaybackDiagnostics
import de.lorenzschl.shelfvr.spike.S3ViewModel
import de.lorenzschl.shelfvr.spike.SpikeActions
import de.lorenzschl.shelfvr.spike.SpikeAudio
import de.lorenzschl.shelfvr.spike.SpikeDiagnostics
import de.lorenzschl.shelfvr.spike.SpikeScreen
import de.lorenzschl.shelfvr.window.Windows
import de.lorenzschl.shelfvr.window.togglePlayPause
import de.lorenzschl.shelfvr.window.windowSizeLabel

/** Hauptfenster. Zeigt in Phase 0 die Spike-Oberfläche; ab M1 Onboarding und Bibliothek. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val playback by lazy { PlaybackClient(this) }
    private val s3: S3ViewModel by viewModels()
    private val notificationStatus = mutableStateOf("?")
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        PlaybackDiagnostics.log("Berechtigungsdialog beendet: erteilt=$granted")
        refreshNotificationStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShelfVrTheme {
                val snapshot by playback.snapshots.collectAsStateWithLifecycle(PlaybackSnapshot.Empty)
                val s3State by s3.uiState.collectAsStateWithLifecycle()
                val diagnosticEntries by PlaybackDiagnostics.entries.collectAsStateWithLifecycle()
                val diagnostics = SpikeDiagnostics(notificationStatus.value, diagnosticEntries)
                SpikeScreen(windowSizeLabel(), snapshot, s3State, diagnostics, actions)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        playback.connect()
    }

    override fun onResume() {
        super.onResume()
        refreshNotificationStatus()
    }

    private fun refreshNotificationStatus() {
        val enabled = NotificationManagerCompat.from(this).areNotificationsEnabled()
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (granted) "erteilt" else "nicht erteilt"
        } else {
            "nicht nötig (API ${Build.VERSION.SDK_INT})"
        }
        notificationStatus.value = "${if (enabled) "aktiv" else "deaktiviert"}, Berechtigung $permission"
    }

    override fun onStop() {
        playback.disconnect()
        super.onStop()
    }

    private val actions = object : SpikeActions {
        private val controller get() = playback.controller.value

        override fun playTestTone() {
            controller?.run {
                setMediaItem(SpikeAudio.testTone())
                prepare()
                play()
            }
        }

        override fun stop() {
            controller?.run {
                stop()
                clearMediaItems()
            }
        }

        override fun requestNotifications() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PlaybackDiagnostics.log("Berechtigungsdialog angefordert")
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        override fun openMiniPlayer(closeMainWindow: Boolean) {
            startActivity(Windows.miniPlayer(this@MainActivity))
            if (closeMainWindow) finish()
        }

        override fun playPause() {
            controller?.togglePlayPause()
        }

        override fun skipBack() {
            controller?.seekBack()
        }

        override fun skipForward() {
            controller?.seekForward()
        }

        override fun onServerUrlChange(value: String) = s3.onServerUrlChange(value)
        override fun onUsernameChange(value: String) = s3.onUsernameChange(value)
        override fun onPasswordChange(value: String) = s3.onPasswordChange(value)
        override fun login() = s3.login()
        override fun playItem(id: String) = s3.play(id)
        override fun closeSession() = s3.closeSession()
        override fun clearDiagnostics() = PlaybackDiagnostics.clear()
    }
}
