package de.lorenzschl.shelfvr.playback

import android.util.Log
import android.view.KeyEvent
import androidx.media3.common.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Prozessweites Diagnose-Log für die Anbindung an die System-Mediensteuerung (#3).
 * Enthält nur Paketnamen, Befehle und Zustände – keine Tokens, URLs oder Nutzerdaten.
 * Zusätzlich in logcat: `adb logcat -s ShelfVR-Diag`.
 */
object PlaybackDiagnostics {

    const val TAG = "ShelfVR-Diag"
    private const val MAX_ENTRIES = 60

    private val state = MutableStateFlow<List<String>>(emptyList())
    val entries: StateFlow<List<String>> = state

    fun log(message: String) {
        Log.i(TAG, message)
        val time = SimpleDateFormat("HH:mm:ss", Locale.ROOT).format(Date())
        state.update { (it + "$time $message").takeLast(MAX_ENTRIES) }
    }

    fun clear() {
        state.value = emptyList()
    }

    fun commandName(command: Int): String = when (command) {
        Player.COMMAND_PLAY_PAUSE -> "PLAY_PAUSE"
        Player.COMMAND_PREPARE -> "PREPARE"
        Player.COMMAND_STOP -> "STOP"
        Player.COMMAND_SEEK_TO_DEFAULT_POSITION -> "SEEK_TO_DEFAULT_POSITION"
        Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM -> "SEEK_IN_CURRENT_MEDIA_ITEM"
        Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM -> "SEEK_TO_PREVIOUS_MEDIA_ITEM"
        Player.COMMAND_SEEK_TO_PREVIOUS -> "SEEK_TO_PREVIOUS"
        Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM -> "SEEK_TO_NEXT_MEDIA_ITEM"
        Player.COMMAND_SEEK_TO_NEXT -> "SEEK_TO_NEXT"
        Player.COMMAND_SEEK_TO_MEDIA_ITEM -> "SEEK_TO_MEDIA_ITEM"
        Player.COMMAND_SEEK_BACK -> "SEEK_BACK"
        Player.COMMAND_SEEK_FORWARD -> "SEEK_FORWARD"
        Player.COMMAND_SET_SPEED_AND_PITCH -> "SET_SPEED_AND_PITCH"
        Player.COMMAND_SET_MEDIA_ITEM -> "SET_MEDIA_ITEM"
        Player.COMMAND_CHANGE_MEDIA_ITEMS -> "CHANGE_MEDIA_ITEMS"
        Player.COMMAND_RELEASE -> "RELEASE"
        else -> "COMMAND_$command"
    }

    fun keyName(keyCode: Int): String = when (keyCode) {
        KeyEvent.KEYCODE_MEDIA_PLAY -> "MEDIA_PLAY"
        KeyEvent.KEYCODE_MEDIA_PAUSE -> "MEDIA_PAUSE"
        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> "MEDIA_PLAY_PAUSE"
        KeyEvent.KEYCODE_HEADSETHOOK -> "HEADSETHOOK"
        KeyEvent.KEYCODE_MEDIA_NEXT -> "MEDIA_NEXT"
        KeyEvent.KEYCODE_MEDIA_PREVIOUS -> "MEDIA_PREVIOUS"
        KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> "MEDIA_FAST_FORWARD"
        KeyEvent.KEYCODE_MEDIA_REWIND -> "MEDIA_REWIND"
        KeyEvent.KEYCODE_MEDIA_STOP -> "MEDIA_STOP"
        else -> "KEYCODE_$keyCode"
    }

    fun playWhenReadyReason(reason: Int): String = when (reason) {
        Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST -> "USER_REQUEST"
        Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS -> "AUDIO_FOCUS_LOSS"
        Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_BECOMING_NOISY -> "AUDIO_BECOMING_NOISY"
        Player.PLAY_WHEN_READY_CHANGE_REASON_REMOTE -> "REMOTE"
        Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM -> "END_OF_MEDIA_ITEM"
        Player.PLAY_WHEN_READY_CHANGE_REASON_SUPPRESSED_TOO_LONG -> "SUPPRESSED_TOO_LONG"
        else -> "REASON_$reason"
    }
}
