package de.lorenzschl.shelfvr.spike

import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

/** Mitgelieferter Testton für Spike S-1 (3 min, piept im Sekundentakt, Tonhöhe wechselt alle 15 s). */
object SpikeAudio {

    private val testToneUri: Uri = "asset:///spike/s1-testton.mp3".toUri()

    fun testTone(): MediaItem = MediaItem.Builder()
        .setMediaId("spike-s1")
        .setUri(testToneUri)
        .setRequestMetadata(MediaItem.RequestMetadata.Builder().setMediaUri(testToneUri).build())
        .setMediaMetadata(MediaMetadata.Builder().setTitle("S-1 Testton").setArtist("ShelfVR").build())
        .build()
}
