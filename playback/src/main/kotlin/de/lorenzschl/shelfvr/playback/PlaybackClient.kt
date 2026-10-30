package de.lorenzschl.shelfvr.playback

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import de.lorenzschl.shelfvr.core.model.PlaybackSnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Eigene MediaController-Verbindung eines Fensters zum [PlaybackService]. Jedes Fenster hält
 * seine eigene Instanz (PROJECT_SPEC.md §5.1), verbindet in onStart und trennt in onStop.
 */
class PlaybackClient(context: Context) {

    private val appContext = context.applicationContext
    private var future: ListenableFuture<MediaController>? = null
    private val controllerState = MutableStateFlow<MediaController?>(null)

    val controller: StateFlow<MediaController?> = controllerState

    @OptIn(ExperimentalCoroutinesApi::class)
    val snapshots: Flow<PlaybackSnapshot> = controllerState.flatMapLatest { controller ->
        controller?.snapshots() ?: flowOf(PlaybackSnapshot.Empty)
    }

    fun connect() {
        if (future != null) return
        val token = SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java))
        future = MediaController.Builder(appContext, token).buildAsync().also { pending ->
            pending.addListener(
                { if (future === pending) controllerState.value = pending.get() },
                ContextCompat.getMainExecutor(appContext),
            )
        }
    }

    fun disconnect() {
        future?.let(MediaController::releaseFuture)
        future = null
        controllerState.value = null
    }
}

/** Zustand des Players als Flow; Position wird während der Wiedergabe zweimal pro Sekunde aktualisiert. */
fun Player.snapshots(): Flow<PlaybackSnapshot> = callbackFlow {
    val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            trySend(snapshot())
        }
    }
    addListener(listener)
    trySend(snapshot())
    val ticker = launch {
        while (isActive) {
            delay(500)
            if (isPlaying) trySend(snapshot())
        }
    }
    awaitClose {
        ticker.cancel()
        removeListener(listener)
    }
}.distinctUntilChanged()

fun Player.snapshot(): PlaybackSnapshot {
    if (currentMediaItem == null) return PlaybackSnapshot.Empty
    return PlaybackSnapshot(
        title = mediaMetadata.title?.toString() ?: "",
        subtitle = mediaMetadata.artist?.toString(),
        isPlaying = isPlaying,
        positionMs = currentPosition.coerceAtLeast(0),
        durationMs = duration.takeIf { it != C.TIME_UNSET }?.coerceAtLeast(0) ?: 0,
    )
}
