package de.lorenzschl.shelfvr.playback

import android.app.PendingIntent
import android.content.Intent
import android.view.KeyEvent
import androidx.core.content.IntentCompat
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.OkHttpClient
import javax.inject.Inject

/**
 * Single Source of Truth für die Wiedergabe (PROJECT_SPEC.md §5.1). Alle Fenster und die
 * System-Mediensteuerung sind nur Controller auf diese Session.
 *
 * Streams laufen über die geteilte OkHttp-Instanz, damit Auth-Header und Token-Refresh für
 * API und Audio gleich funktionieren (§6.3).
 */
// UnstableApi: OkHttpDataSource, DefaultMediaSourceFactory und Diagnose-Felder der ControllerInfo.
@OptIn(UnstableApi::class)
@AndroidEntryPoint
class PlaybackService : MediaLibraryService() {

    @Inject lateinit var httpClient: OkHttpClient

    private var session: MediaLibrarySession? = null

    override fun onCreate() {
        super.onCreate()
        val dataSourceFactory = DefaultDataSource.Factory(this, OkHttpDataSource.Factory(httpClient))
        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .build(),
                /* handleAudioFocus = */ true, // FR-38
            )
            .setHandleAudioBecomingNoisy(true)
            // Defaults aus FR-32; werden mit den Einstellungen (FR-60) konfigurierbar.
            .setSeekBackIncrementMs(15_000)
            .setSeekForwardIncrementMs(30_000)
            .build()
        player.addListener(DiagnosticsListener)
        setListener(object : MediaSessionService.Listener {
            override fun onForegroundServiceStartNotAllowedException() {
                PlaybackDiagnostics.log("Foreground-Service-Start vom System abgelehnt")
            }
        })

        val builder = MediaLibrarySession.Builder(this, player, LibraryCallback())
        packageManager.getLaunchIntentForPackage(packageName)?.let { launch ->
            builder.setSessionActivity(
                PendingIntent.getActivity(this, 0, launch, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT),
            )
        }
        session = builder.build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? = session

    private var lastForegroundRequired: Boolean? = null

    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        if (lastForegroundRequired != startInForegroundRequired) {
            lastForegroundRequired = startInForegroundRequired
            PlaybackDiagnostics.log("Notification aktualisiert, Foreground nötig: $startInForegroundRequired")
        }
        super.onUpdateNotification(session, startInForegroundRequired)
    }

    override fun onDestroy() {
        session?.run {
            player.release()
            release()
        }
        session = null
        super.onDestroy()
    }

    @OptIn(UnstableApi::class)
    private class LibraryCallback : MediaLibrarySession.Callback {

        // Diagnose (#3): Welche Controller verbinden sich, welche Befehle kommen an?
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): MediaSession.ConnectionResult {
            val result = super.onConnect(session, controller)
            PlaybackDiagnostics.log(
                "Verbunden: ${controller.packageName} (v${controller.controllerVersion}, " +
                    "trusted=${controller.isTrusted}, akzeptiert=${result.isAccepted})",
            )
            return result
        }

        override fun onDisconnected(session: MediaSession, controller: MediaSession.ControllerInfo) {
            PlaybackDiagnostics.log("Getrennt: ${controller.packageName}")
        }

        override fun onPlayerInteractionFinished(
            session: MediaSession,
            controllerInfo: MediaSession.ControllerInfo,
            playerCommands: Player.Commands,
        ) {
            val names = (0 until playerCommands.size()).map { PlaybackDiagnostics.commandName(playerCommands.get(it)) }
            PlaybackDiagnostics.log("Befehl ${names.joinToString()} von ${controllerInfo.packageName}")
        }

        override fun onMediaButtonEvent(
            session: MediaSession,
            controllerInfo: MediaSession.ControllerInfo,
            intent: Intent,
        ): Boolean {
            val key = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
            PlaybackDiagnostics.log(
                "Media-Button ${key?.let { PlaybackDiagnostics.keyName(it.keyCode) } ?: "?"} " +
                    "(action=${key?.action}) von ${controllerInfo.packageName}",
            )
            return super.onMediaButtonEvent(session, controllerInfo, intent)
        }

        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            isForPlayback: Boolean,
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            PlaybackDiagnostics.log("Wiederaufnahme angefragt von ${controller.packageName} (play=$isForPlayback)")
            return super.onPlaybackResumption(mediaSession, controller, isForPlayback)
        }

        // Browsing-Baum folgt mit Podcasts/Downloads (§11); bis dahin nur eine leere Wurzel.
        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?,
        ): ListenableFuture<LibraryResult<MediaItem>> = Futures.immediateFuture(
            LibraryResult.ofItem(
                MediaItem.Builder()
                    .setMediaId(ROOT_ID)
                    .setMediaMetadata(
                        MediaMetadata.Builder().setIsBrowsable(true).setIsPlayable(false).build(),
                    )
                    .build(),
                params,
            ),
        )

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?,
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> =
            Futures.immediateFuture(LibraryResult.ofItemList(ImmutableList.of(), params))

        // Controller übergeben die Stream-URL in requestMetadata.mediaUri, weil die
        // localConfiguration beim Weg zur Session nicht garantiert erhalten bleibt.
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
        ): ListenableFuture<MutableList<MediaItem>> = Futures.immediateFuture(
            mediaItems.map { item -> item.withUri(item.requestMetadata.mediaUri) }.toMutableList(),
        )
    }

    private object DiagnosticsListener : Player.Listener {
        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            PlaybackDiagnostics.log("playWhenReady=$playWhenReady (${PlaybackDiagnostics.playWhenReadyReason(reason)})")
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            PlaybackDiagnostics.log("isPlaying=$isPlaying")
        }

        override fun onPlayerError(error: PlaybackException) {
            PlaybackDiagnostics.log("Fehler: ${error.errorCodeName}")
        }
    }

    private companion object {
        const val ROOT_ID = "root"

        fun MediaItem.withUri(uri: Uri?): MediaItem =
            if (uri == null || localConfiguration != null) this else buildUpon().setUri(uri).build()
    }
}
