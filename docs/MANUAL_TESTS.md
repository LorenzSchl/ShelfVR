# Manuelle Tests

Alles, was nur auf der Brille (oder durch Lorenz) geprüft bzw. erledigt werden kann.
Ergebnisse bitte im jeweiligen Abschnitt unter **Ergebnis** eintragen oder in den zugehörigen
PR schreiben. Status: `offen` → `ok` / `nicht ok` / `erledigt`.
Auswertungen der Spikes stehen in `docs/spikes/`.

> Das Repo ist öffentlich: Server-Adressen, Benutzernamen und Tokens nie hier oder im PR notieren.

---

## MT-00 · Vorbereitung: Quest einrichten und APK installieren

- **Status:** ok
- **Commit:** e5ac5b5 (CI baut das APK)
- **Schritte:**
  1. Entwicklermodus auf der Quest 2 aktivieren (Meta-Horizon-App am Handy → Gerät →
     Entwicklermodus; setzt eine Developer-Organisation bei Meta voraus).
  2. Horizon-OS-Version notieren (Einstellungen → System → Info). Falls vorhanden, auch auf der Quest 3.
  3. APK besorgen: im PR unter *Checks → CI → Summary* das Artefakt `shelfvr-debug-apk`
     herunterladen und entpacken.
  4. Installieren: `adb install -r app-debug.apk` oder über SideQuest.
     Bei `INSTALL_FAILED_UPDATE_INCOMPATIBLE` vorher `adb uninstall de.lorenzschl.shelfvr`.
  5. App starten (App-Bibliothek → Filter „Unbekannte Quellen“).
- **Erwartet:** Ein Fenster „ShelfVR · Spikes Phase 0“ öffnet sich; die Zeile „Dieses Fenster“
  zeigt etwa 1024 × 640 dp.
- **Ergebnis:**
  - Horizon OS Quest 2: 2.9 · Quest 3: NICHT VORHANDEN (kommt als Mindestversion ins README)
  - Angezeigte Fenstergröße: 1280 x 800 dp

## MT-01 · Spike S-1: Hintergrund-Audio und System-Mediensteuerung

- **Status:** teilweise – System-Mediensteuerung → #3; Hand-Tracking offen → #15
- **Commit:** 9ce750e
- **Schritte:**
  1. Im Abschnitt S-1 zuerst **ohne** „Benachrichtigungen erlauben“ den „Testton abspielen“.
     Der Testton piept im Sekundentakt, die Tonhöhe wechselt alle 15 s (3 min lang).
  2. Leiste oben im Fenster: Pause/Play, −15, +30 ausprobieren.
  3. Eine andere App (z. B. Browser) öffnen und in den Vordergrund holen.
  4. System-Mediensteuerung öffnen (Universalmenü/Schnelleinstellungen): Titel „S-1 Testton“,
     Play/Pause und Springen testen.
  5. Hauptfenster von ShelfVR schließen. Läuft der Ton weiter? Lässt er sich über die
     System-Mediensteuerung pausieren und fortsetzen?
  6. Schritte 1–5 mit „Benachrichtigungen erlauben“ (Dialog bestätigen) wiederholen und
     Unterschiede notieren.
  7. Brille ca. 1 min absetzen (Standby), wieder aufsetzen: Was ist mit der Wiedergabe?
  8. Alles einmal mit Controller-Ray und einmal mit Hand-Tracking (Pinch) bedienen.
- **Erwartet:** Der Ton läuft in Schritt 3–5 weiter; die System-Mediensteuerung zeigt den Titel
  und steuert die Wiedergabe; alle Buttons sind mit Controller und Hand gut treffbar.
- **Nebenbei beobachten (für S-4):** Kommt der Ton räumlich aus dem Fenster? Ändert sich das,
  wenn das Fenster geschlossen ist?
- **Danach entscheiden (§13 Punkt 4):** Design-Sprache – beim aktuellen Material-3-Theme
  bleiben oder auf das Horizon OS UI Set wechseln? Eindruck von Lesbarkeit und Kontrast auf dem
  Quest-2-Display.
- **Ergebnis:**
In dem Hauptfenster funktionieren -15s, +30s und Pause und Play. Die Tonspur wird korrekt abgespielt und der Fortschritt angezeigt. Der Ton wird auch weiter ausgegeben, wenn das ShelfVR Fenster geschlossen ist.
Über das Pop-UPd das nach dem Minimieren kommt kann der Ton nicht kontrolliert werden. Benachrichtigen erlauben Button scheint nichts zu machen. Nach dem Absetzen der Brille läuft der Ton weiter, auch nach > 2 Minuten ist hier nichts verändert. Ggf. liegt das am ADB über TCP?
Bisher nur mit Controller getestet, Handtracking vorerst ignoriert.
Der Ton kommt aus der Richtung des Fensters und "bewegt" sich, wenn das Fenster verschoben wird.
Die Designsprache passt erstmal so!

## MT-02 · Spike S-2: Mehrere Fenster und Mini-Player

- **Status:** ok – Bugs → #14
- **Commit:** 9ce750e
- **Schritte:**
  1. Testton starten.
  2. „Mini-Player daneben öffnen“: Wo erscheint das neue Panel? Größe ablesen (unten links im
     Mini-Player).
  3. Den Mini-Player so klein wie möglich ziehen. Kleinste erreichbare Größe notieren. Sind Text
     und Buttons dann noch lesbar und treffbar?
  4. Im Mini-Player Play/Pause, −15, +30 – zeigt das Hauptfenster denselben Zustand?
  5. Hauptfenster schließen – läuft die Wiedergabe weiter, bleibt der Mini-Player bedienbar?
  6. Im Mini-Player „Großes Fenster öffnen“: Hauptfenster erscheint, Mini-Player schließt sich,
     der Ton läuft ohne Unterbrechung weiter.
  7. „Zum Mini-Player wechseln (Hauptfenster schließen)“ ausprobieren.
  8. Aus dem Hauptfenster zweimal „Mini-Player daneben öffnen“ – entstehen zwei Mini-Player?
  9. Andere App in den Vordergrund holen – bleibt der Mini-Player sichtbar (FR-53)?
  10. Optional (Horizon OS v81+): Mini-Player im Loft/Passthrough anheften und Brille neu starten.
- **Erwartet:** Die Wiedergabe läuft bei allen Fensterwechseln unterbrechungsfrei; alle offenen
  Fenster zeigen denselben Zustand.
- **Danach entscheiden (§13 Punkte 5 und 6):** Mini-Player-Variante A (eigenes Panel, so ist es
  jetzt gebaut), B (Größenwechsel im selben Fenster) oder C (nur System-Mediensteuerung); und ob
  Mini-Player und späteres Playerfenster eine gemeinsame Activity werden. Screenshots oder ein
  kurzes Video im PR helfen.
- **Ergebnis:**
  - Initiale Mini-Player-Größe: 800 x 150 dp
  - Kleinste Mini-Player-Größe: 800 × 150 dp (Beim Versuch den Player kleiner zu machen wurde er Größer)
  - Wenn man den Hauptplayer Schließt öffnet sich ein zweiter Mini Player. Beide können korrekt steuern und zeigen den Fortschritt an.
  - Beim mehrfachen drücken auf Miniplayer daneben öffnen öffnen sich mehrere miniplayer.
  - Die Miniplayer bleiben bestehen, wenn andere Fenster geöffnet werden. GGf. werden sie in den Hintergrund gedrängt, wenn zu viele Fenster offen sind.
  - ERKENNTNIS: Wenn wir das System-Overlay zur Mediensteuerung funktional bekommen brauchen wir den Mini Player erstmal nicht. Kann man das ggf. erweitern oder konfigurieren=

## MT-03 · Spike S-3: Login, Streaming und Sync gegen deinen ABS-Server

- **Status:** ok – HTTP → #4
- **Commit:** 23b2323 (ABS-Client), 9ce750e (Spike-UI)
- **Hinweise:** Tokens liegen im Spike nur im Speicher; nach einem App-Neustart erneut anmelden.
  Der Sync läuft im Spike nur, solange das Hauptfenster offen ist (der richtige Sync im
  Playback-Service kommt mit M2).
- **Schritte:**
  1. Im Abschnitt S-3 Server-URL (`https://…`), Benutzername und Passwort eingeben. Dabei auch
     Einfügen aus der Zwischenablage ausprobieren.
  2. „Anmelden“: Die Statuszeile zeigt ABS-Version und Bibliothek, darunter die Bücher.
  3. Ein Buch aus **mehreren Audiodateien** antippen. Die Zeile „Läuft“ zeigt Anzahl der Tracks und
     `playMethod` (0 = direkt, 2 = HLS).
  4. Ca. 1 min hören: Die Zeile „Sync …“ aktualisiert sich alle 15 s.
  5. Pausieren: Es wird sofort synchronisiert.
  6. Mehrmals +30 drücken, bis eine Track-Grenze überschritten ist: Die Buchzeit im Sync läuft
     korrekt weiter (springt nicht auf 0 zurück).
  7. Im ABS-Web-UI am PC prüfen: Der Fortschritt stimmt (±15 s), und unter den Hör-Sessions
     erscheint das Gerät „ShelfVR“.
  8. Optional: Ein Buch in einem eher ungewöhnlichen Format (z. B. Opus) abspielen und prüfen,
     ob es direkt oder per HLS läuft.
  9. „Session schließen“.
  10. Fehlerfälle: falsches Passwort → „Anmeldung abgelehnt (401)“; Tippfehler in der Domain →
      „Server nicht gefunden (DNS)“; `http://` statt `https://` → Hinweis auf HTTPS.
- **Erwartet:** Das Multi-Track-Buch streamt, und der Sync kommt im ABS-Web-UI an
  (Erfolgskriterium S-3).
- **Ergebnis:**
  - http ist gesperrt: Das sollte nicht so sein!
  - Anmelden mit HTTPS klappt
  - ABS-Version deines Servers (§13 Punkt 9): 2.37.1
  - anklicken eines buches mit mehreren audiodateien klappt. Anzahl der Dateien wird angezeigt.
  - Springen, Stoppen und Sync klappt
  

## MT-04 · Zu erledigen (keine Tests)

- **Status:** erledigt
- **Commit:** e5ac5b5
- **Aufgaben:**
  1. `LICENSE`: Als Copyright-Inhaber steht der GitHub-Name „LorenzSchl“. Falls du deinen vollen
     Namen willst, im PR Bescheid geben oder direkt ändern.
  2. Prüfen, dass der CI-Lauf im PR grün ist und das APK-Artefakt herunterladbar ist.
  3. Optional: Branch-Schutz für `main` einrichten (PR + grüne CI vor dem Merge).
- **Ergebnis:**
- is mir egal.
- ci ist grün, apk lief auf der quest :)

## MT-05 · System-Mediensteuerung: Diagnose (#3)

- **Status:** offen
- **Commit:** dafd56b
- **Ziel:** Herausfinden, warum die Buttons im System-Pop-up nichts bewirken, und ob der
  `MediaButtonReceiver` das schon behebt.
- **Schritte:**
  1. APK aus dem CI-Lauf dieses PRs installieren (`adb install -r …`, notfalls vorher deinstallieren).
  2. Abschnitt **Diagnose** im Hauptfenster: Was steht bei „Benachrichtigungen“? Dann
     „Benachrichtigungen erlauben“ antippen. Erscheint ein Dialog? Was steht danach im Log und in
     der Statuszeile?
  3. „Log leeren“, dann „Testton abspielen“.
  4. Fenster minimieren, sodass das System-Pop-up erscheint. Nacheinander **Pause**, **Play**,
     **Vor**, **Zurück** antippen, jeweils mit 2–3 s Abstand. Wirkt einer der Buttons?
  5. Zurück ins Hauptfenster und das Diagnose-Log abfotografieren oder abschreiben. Einfacher:
     währenddessen `adb logcat -s ShelfVR-Diag` am PC mitlaufen lassen und die Ausgabe in den PR
     kopieren.
  6. Dasselbe mit einem Buch aus S-3 (Anmeldung über HTTPS) wiederholen.
- **Erwartet (Hypothesen):**
  - Erscheinen bei den Pop-up-Buttons Zeilen wie „Befehl PLAY_PAUSE von …“ oder „Media-Button …“,
    kommen die Befehle an. Dann liegt der Fehler in deren Verarbeitung.
  - Erscheint gar nichts, erreicht das Pop-up unsere Session nicht. Wichtig ist dann, welches
    Paket in den Zeilen „Verbunden: …“ steht.
  - Funktionieren die Buttons jetzt, hat der `MediaButtonReceiver` das Problem behoben.
- **Ergebnis:**
