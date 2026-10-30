package de.lorenzschl.shelfvr.playback

import android.view.KeyEvent
import androidx.media3.common.Player
import kotlin.test.Test
import kotlin.test.assertEquals

class PlaybackDiagnosticsTest {

    @Test
    fun `Befehle, Tasten und Gründe werden lesbar benannt`() {
        assertEquals("PLAY_PAUSE", PlaybackDiagnostics.commandName(Player.COMMAND_PLAY_PAUSE))
        assertEquals("SEEK_FORWARD", PlaybackDiagnostics.commandName(Player.COMMAND_SEEK_FORWARD))
        assertEquals("COMMAND_999", PlaybackDiagnostics.commandName(999))
        assertEquals("MEDIA_PLAY_PAUSE", PlaybackDiagnostics.keyName(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
        assertEquals(
            "AUDIO_FOCUS_LOSS",
            PlaybackDiagnostics.playWhenReadyReason(Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS),
        )
    }
}
