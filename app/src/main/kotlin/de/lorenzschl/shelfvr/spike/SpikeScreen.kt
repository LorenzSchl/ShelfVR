package de.lorenzschl.shelfvr.spike

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import de.lorenzschl.shelfvr.core.designsystem.ShelfButton
import de.lorenzschl.shelfvr.core.designsystem.ShelfDimens
import de.lorenzschl.shelfvr.core.designsystem.ShelfOutlinedButton
import de.lorenzschl.shelfvr.core.model.PlaybackSnapshot
import de.lorenzschl.shelfvr.feature.miniplayer.MiniPlayerBar

/** Aktionen des Spike-Bildschirms; Umsetzung in MainActivity. */
interface SpikeActions {
    fun playTestTone()
    fun stop()
    fun requestNotifications()
    fun openMiniPlayer(closeMainWindow: Boolean)
    fun playPause()
    fun skipBack()
    fun skipForward()
    fun onServerUrlChange(value: String)
    fun onUsernameChange(value: String)
    fun onPasswordChange(value: String)
    fun login()
    fun playItem(id: String)
    fun closeSession()
    fun clearDiagnostics()
}

/** Diagnose für #3: Berechtigungsstatus und Log aus dem PlaybackService (neueste zuerst). */
data class SpikeDiagnostics(val notificationStatus: String, val entries: List<String>)

/**
 * Bedienoberfläche für die Spikes S-1 bis S-3 (PROJECT_SPEC.md §12). Wegwerf-UI für Phase 0,
 * deshalb ohne String-Ressourcen; die echte UI entsteht ab M1.
 */
@Composable
fun SpikeScreen(
    windowSize: String,
    snapshot: PlaybackSnapshot,
    s3: S3UiState,
    diagnostics: SpikeDiagnostics,
    actions: SpikeActions,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(ShelfDimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text("ShelfVR · Spikes Phase 0", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Dieses Fenster: $windowSize",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            MiniPlayerBar(
                snapshot = snapshot,
                onPlayPause = actions::playPause,
                onSkipBack = actions::skipBack,
                onSkipForward = actions::skipForward,
                onExpand = null,
                modifier = Modifier.fillMaxWidth(),
            )

            SpikeSection(
                title = "S-1 · Hintergrund-Audio",
                description = "Testton starten, dann eine andere App in den Vordergrund holen. " +
                    "Der Ton muss weiterlaufen und über die System-Mediensteuerung bedienbar sein.",
            ) {
                ButtonRow {
                    ShelfButton("Testton abspielen", actions::playTestTone)
                    ShelfOutlinedButton("Stopp", actions::stop)
                    ShelfOutlinedButton("Benachrichtigungen erlauben", actions::requestNotifications)
                }
            }

            SpikeSection(
                title = "Diagnose · System-Mediensteuerung (#3)",
                description = "Zeigt, welche Controller sich verbinden und welche Befehle ankommen. " +
                    "Auch per adb logcat -s ShelfVR-Diag.",
            ) {
                Text("Benachrichtigungen: ${diagnostics.notificationStatus}")
                ShelfOutlinedButton("Log leeren", actions::clearDiagnostics)
                if (diagnostics.entries.isEmpty()) {
                    Text("Noch keine Einträge.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                diagnostics.entries.asReversed().take(20).forEach { entry ->
                    Text(entry, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                }
            }

            SpikeSection(
                title = "S-2 · Mehrere Fenster",
                description = "Mini-Player als eigenes Panel öffnen. Die Wiedergabe darf dabei nicht abbrechen. " +
                    "Im Mini-Player steht die tatsächliche Fenstergröße.",
            ) {
                ButtonRow {
                    ShelfButton("Mini-Player daneben öffnen", { actions.openMiniPlayer(closeMainWindow = false) })
                    ShelfOutlinedButton(
                        "Zum Mini-Player wechseln (Hauptfenster schließen)",
                        { actions.openMiniPlayer(closeMainWindow = true) },
                    )
                }
            }

            SpikeSection(
                title = "S-3 · ABS-Server",
                description = "Anmelden, ein Buch antippen. Fortschritt wird alle 15 s und bei Pause synchronisiert.",
            ) {
                val fieldModifier = Modifier.widthIn(max = 640.dp).fillMaxWidth()
                OutlinedTextField(
                    value = s3.serverUrl,
                    onValueChange = actions::onServerUrlChange,
                    label = { Text("Server-URL") },
                    placeholder = { Text("https://abs.example.org") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
                    modifier = fieldModifier,
                )
                OutlinedTextField(
                    value = s3.username,
                    onValueChange = actions::onUsernameChange,
                    label = { Text("Benutzername") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = fieldModifier,
                )
                OutlinedTextField(
                    value = s3.password,
                    onValueChange = actions::onPasswordChange,
                    label = { Text("Passwort") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    modifier = fieldModifier,
                )
                ButtonRow {
                    ShelfButton("Anmelden", actions::login, enabled = !s3.busy)
                    if (s3.playing != null) ShelfOutlinedButton("Session schließen", actions::closeSession)
                }
                s3.status?.let { Text(it) }
                s3.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                s3.playing?.let { Text("Läuft: $it", style = MaterialTheme.typography.bodyMedium) }
                s3.lastSync?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                s3.items.forEach { item ->
                    val details = listOfNotNull(item.author, item.tracks?.let { "$it Track(s)" }).joinToString(" · ")
                    ShelfOutlinedButton(
                        text = if (details.isEmpty()) item.title else "${item.title} — $details",
                        onClick = { actions.playItem(item.id) },
                        enabled = !s3.busy,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun SpikeSection(title: String, description: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(ShelfDimens.ItemSpacing)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
        }
    }
}

@Composable
private fun ButtonRow(content: @Composable () -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(ShelfDimens.ItemSpacing),
        verticalArrangement = Arrangement.spacedBy(ShelfDimens.ItemSpacing),
    ) { content() }
}
