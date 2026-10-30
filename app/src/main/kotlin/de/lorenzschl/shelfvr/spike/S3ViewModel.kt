package de.lorenzschl.shelfvr.spike

import android.content.Context
import android.os.Build
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.lorenzschl.shelfvr.core.model.AudioTrack
import de.lorenzschl.shelfvr.core.network.AbsApi
import de.lorenzschl.shelfvr.core.network.AbsClientFactory
import de.lorenzschl.shelfvr.core.network.AbsSession
import de.lorenzschl.shelfvr.core.network.ServerUrl
import de.lorenzschl.shelfvr.core.network.TokenStore
import de.lorenzschl.shelfvr.core.network.dto.DeviceInfoDto
import de.lorenzschl.shelfvr.core.network.dto.LoginRequest
import de.lorenzschl.shelfvr.core.network.dto.PlayRequest
import de.lorenzschl.shelfvr.core.network.dto.PlaybackSessionDto
import de.lorenzschl.shelfvr.core.network.dto.SyncRequest
import de.lorenzschl.shelfvr.core.network.resolveServerPath
import de.lorenzschl.shelfvr.domain.TrackPosition
import de.lorenzschl.shelfvr.domain.TrackTimeline
import de.lorenzschl.shelfvr.playback.PlaybackClient
import de.lorenzschl.shelfvr.playback.SupportedMimeTypes
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.UnknownServiceException
import java.text.DateFormat
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.net.ssl.SSLException

/**
 * Spike S-3: Login, Play-Session, Streaming und Sync gegen einen echten ABS-Server.
 * Wegwerf-Code für Phase 0; Login (M1) und Sync im PlaybackService (M2) werden neu gebaut.
 */
@HiltViewModel
class S3ViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenStore: TokenStore,
    private val httpClient: OkHttpClient,
) : ViewModel() {

    private val state = MutableStateFlow(S3UiState())
    val uiState: StateFlow<S3UiState> = state

    private val playback = PlaybackClient(context).also { it.connect() }
    private var api: AbsApi? = null
    private var serverUrl: HttpUrl? = null
    private var active: ActiveSession? = null
    private var syncJob: Job? = null

    private class ActiveSession(val dto: PlaybackSessionDto, val timeline: TrackTimeline)

    fun onServerUrlChange(value: String) = state.update { it.copy(serverUrl = value) }
    fun onUsernameChange(value: String) = state.update { it.copy(username = value) }
    fun onPasswordChange(value: String) = state.update { it.copy(password = value) }

    fun login() {
        val current = state.value
        val url = ServerUrl.normalize(current.serverUrl)
            ?: return state.update { it.copy(error = "Ungültige Server-URL.") }
        state.update { it.copy(busy = true, error = null, status = "Melde an …") }
        viewModelScope.launch {
            try {
                val client = AbsClientFactory.createApi(url, httpClient)
                val login = client.login(LoginRequest(current.username.trim(), current.password))
                val access = login.user.accessToken
                val refresh = login.user.refreshToken
                if (access == null || refresh == null) {
                    state.update { it.copy(busy = false, error = "Server liefert keine JWT-Tokens – ABS < 2.26?") }
                    return@launch
                }
                tokenStore.update(AbsSession(url, login.user.username, access, refresh))
                api = client
                serverUrl = url

                val version = client.authorize().serverSettings?.version
                val books = client.libraries().libraries.filter { it.mediaType == "book" }
                val library = books.firstOrNull { it.id == login.userDefaultLibraryId } ?: books.firstOrNull()
                val items = library?.let {
                    client.libraryItems(it.id, limit = 50, sort = "media.metadata.title").results
                }.orEmpty()
                state.update {
                    it.copy(
                        busy = false,
                        password = "",
                        status = "Angemeldet als ${login.user.username} · ABS ${version ?: "?"} · " +
                            (library?.name ?: "keine Buch-Bibliothek"),
                        items = items.map { item ->
                            S3Item(
                                id = item.id,
                                title = item.media.metadata.title ?: item.id,
                                author = item.media.metadata.authorName,
                                tracks = item.media.numTracks,
                            )
                        },
                    )
                }
            } catch (e: Exception) {
                state.update { it.copy(busy = false, status = null, error = describe(e)) }
            }
        }
    }

    fun play(itemId: String) {
        val client = api ?: return
        val url = serverUrl ?: return
        val controller = playback.controller.value
            ?: return state.update { it.copy(error = "Player-Service nicht verbunden.") }
        state.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try {
                closeActiveSession()
                val session = client.play(itemId, PlayRequest(deviceInfo(), "ExoPlayer", SupportedMimeTypes.forThisDevice()))
                val tracks = session.audioTracks.sortedBy { it.startOffset }.map {
                    AudioTrack(it.index, it.startOffset, it.duration, it.contentUrl, it.mimeType)
                }
                val timeline = TrackTimeline(tracks)
                val start = timeline.toTrackPosition(session.currentTime)
                controller.setMediaItems(tracks.map { mediaItem(url, session, it) }, start.trackIndex, start.positionMs)
                controller.prepare()
                controller.play()
                active = ActiveSession(session, timeline)
                startSyncLoop()
                state.update {
                    it.copy(
                        busy = false,
                        playing = "${session.displayTitle} · ${tracks.size} Track(s) · " +
                            "playMethod ${session.playMethod} · Start bei ${session.currentTime.toInt()} s",
                        lastSync = null,
                    )
                }
            } catch (e: Exception) {
                state.update { it.copy(busy = false, error = describe(e)) }
            }
        }
    }

    fun closeSession() {
        viewModelScope.launch {
            playback.controller.value?.stop()
            closeActiveSession()
            state.update { it.copy(playing = null) }
        }
    }

    private fun mediaItem(server: HttpUrl, session: PlaybackSessionDto, track: AudioTrack): MediaItem {
        val uri = server.resolveServerPath(track.contentUrl).toString().toUri()
        return MediaItem.Builder()
            .setMediaId("${session.id}/${track.index}")
            .setUri(uri)
            .setMimeType(if (track.mimeType?.contains("mpegurl", ignoreCase = true) == true) MimeTypes.APPLICATION_M3U8 else null)
            .setRequestMetadata(MediaItem.RequestMetadata.Builder().setMediaUri(uri).build())
            .setMediaMetadata(
                MediaMetadata.Builder().setTitle(session.displayTitle).setArtist(session.displayAuthor).build(),
            )
            .build()
    }

    /** Sync alle 15 s Wiedergabe und sofort bei Pause (§6.3). timeListened = Wanduhrzeit (offen, s. Spike-Notiz). */
    private fun startSyncLoop() {
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            var listenedSeconds = 0.0
            var secondsSinceSync = 0
            var wasPlaying = false
            while (isActive) {
                delay(1_000)
                val controller = playback.controller.value ?: continue
                val playing = controller.isPlaying
                if (playing) {
                    listenedSeconds += 1.0
                    secondsSinceSync++
                }
                if ((playing && secondsSinceSync >= 15) || (wasPlaying && !playing)) {
                    if (sync(controller, listenedSeconds)) listenedSeconds = 0.0
                    secondsSinceSync = 0
                }
                wasPlaying = playing
            }
        }
    }

    private suspend fun sync(controller: Player, listenedSeconds: Double): Boolean {
        val session = active ?: return false
        val client = api ?: return false
        if (controller.currentMediaItem?.mediaId?.startsWith(session.dto.id) != true) return false
        val bookTime = currentBookTime(controller, session)
        return try {
            client.syncSession(session.dto.id, SyncRequest(bookTime, listenedSeconds, session.dto.duration))
            val time = DateFormat.getTimeInstance(DateFormat.MEDIUM).format(Date())
            state.update {
                it.copy(lastSync = "Sync $time · Buchzeit ${bookTime.toInt()} s · gehört ${listenedSeconds.toInt()} s")
            }
            true
        } catch (e: Exception) {
            state.update { it.copy(lastSync = "Sync fehlgeschlagen: ${describe(e)}") }
            false
        }
    }

    private suspend fun closeActiveSession() {
        syncJob?.cancel()
        val session = active ?: return
        active = null
        val controller = playback.controller.value
        val bookTime = controller?.let { currentBookTime(it, session) } ?: session.dto.currentTime
        runCatching { api?.closeSession(session.dto.id, SyncRequest(bookTime, 0.0, session.dto.duration)) }
    }

    private fun currentBookTime(controller: Player, session: ActiveSession): Double =
        session.timeline.toBookTimeSeconds(TrackPosition(controller.currentMediaItemIndex, controller.currentPosition))

    private fun deviceInfo(): DeviceInfoDto {
        val prefs = context.getSharedPreferences("spike", Context.MODE_PRIVATE)
        val deviceId = prefs.getString("deviceId", null) ?: UUID.randomUUID().toString().also {
            prefs.edit { putString("deviceId", it) }
        }
        val version = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
        return DeviceInfoDto("ShelfVR", version, deviceId, Build.MANUFACTURER, Build.MODEL, Build.VERSION.SDK_INT)
    }

    override fun onCleared() {
        syncJob?.cancel()
        playback.disconnect()
    }

    private fun describe(e: Exception): String = when (e) {
        is UnknownHostException -> "Server nicht gefunden (DNS)."
        is SSLException -> "TLS-Fehler: ${e.message}"
        is SocketTimeoutException -> "Zeitüberschreitung."
        is ConnectException -> "Verbindung abgelehnt."
        is UnknownServiceException -> "Unverschlüsseltes HTTP ist gesperrt – bitte https:// verwenden."
        is HttpException -> if (e.code() == 401) "Anmeldung abgelehnt (401)." else "HTTP-Fehler ${e.code()}."
        is SerializationException -> "Unerwartete Antwort – ist das ein ABS-Server?"
        else -> e::class.simpleName ?: "Unbekannter Fehler"
    }
}

data class S3UiState(
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val busy: Boolean = false,
    val status: String? = null,
    val error: String? = null,
    val items: List<S3Item> = emptyList(),
    val playing: String? = null,
    val lastSync: String? = null,
)

data class S3Item(val id: String, val title: String, val author: String?, val tracks: Int?)
