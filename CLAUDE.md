# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# ShelfVR

Audiobookshelf-Client für Meta Quest (Horizon OS, Android-14-Basis).
Kotlin, Jetpack Compose, Gradle (Kotlin DSL).

**Vor jeder größeren Änderung `PROJECT_SPEC.md` lesen.** Sie ist die verbindliche
Quelle für Funktionsumfang, Architektur (Modulschnitt §5, ADRs §4) und Prioritäten.
Widerspricht etwas hier der Spec, gilt die Spec – und weise mich auf den Widerspruch hin.

Spec-Regeln, die beim Coden ständig greifen (Details in §0):
- Die ABS-API-Doku ist veraltet; maßgeblich sind der Server-Code (`advplyr/audiobookshelf`)
  und das Verhalten des Testservers.
- Code aus der offiziellen ABS-App (GPL-3.0) nie kopieren. Code aus Lissen (MIT) nur mit
  Herkunfts-Header und Eintrag in `THIRD_PARTY_NOTICES.md`.
- Keine ABS-Management-Endpunkte in der App. Die Admin-Zugänge in `.env.test` sind
  nur für das Test-Setup da.

## Umgebung
- Headless Ubuntu-VM, kein Android Studio, kein Emulator, **keine Quest verbunden**.
- JDK 21 (Temurin), Android SDK unter `$ANDROID_HOME`.
- Gradle und Kotlin nur über den Wrapper (`./gradlew`), nichts systemweit installieren.
- Versionen von Abhängigkeiten und Plugins nur in `gradle/libs.versions.toml`.

## Build & Test
- Build: `./gradlew assembleDebug`
- Unit-Tests: `./gradlew testDebugUnitTest`
- Einzelner Test: `./gradlew :core:network:testDebugUnitTest --tests "*.TokenRefreshTest"`
- Screenshot-Tests prüfen: `./gradlew verifyRoborazziDebug`
- Screenshot-Referenzen neu aufnehmen: `./gradlew recordRoborazziDebug`
  (nur bei gewollten UI-Änderungen; im Commit begründen)
- Lint: `./gradlew lintDebug`

Diese Befehle beschreiben den Zielzustand. Solange das Projektgerüst noch nicht
steht, ist das Anlegen genau dieser Tasks Teil der Aufgabe. Ändern sich Task-Namen
oder Pfade, diese Datei im selben Commit anpassen.

Reine Kotlin-Module (`kotlin("jvm")`, z. B. `:core:model`, `:domain`) haben nur den
Task `test` und würden von `./gradlew testDebugUnitTest` übersprungen. Das Gerüst
muss deshalb in diesen Modulen einen Task `testDebugUnitTest` registrieren, der von
`test` abhängt (z. B. per Convention-Plugin), damit der Befehl oben alle Module erfasst.

Vor jedem Commit: Build, Unit-Tests und Screenshot-Tests grün, Lint ohne Errors.
Commit-Nachrichten nennen die Anforderungs-ID aus der Spec (FR-xx, R-xx, P-xx, NFR-xx),
sofern eine betroffen ist.

## Arbeiten ohne Quest
- Kein `adb`, keine Gerätetests. Alles, was ohne Gerät prüfbar ist, wird ohne Gerät geprüft.
- Die UI-Smoke-Tests im Simulator/Emulator aus NFR-08 ersetzen hier Screenshot-Tests
  mit Roborazzi.
- UI-Kontrolle über Screenshot-Tests: PNGs liegen unter
  `<modul>/build/outputs/roborazzi/` (z. B. `app/`, `core/designsystem/`, `feature/*/`).
  Nach UI-Änderungen die Bilder **ansehen** und auf Lesbarkeit in VR achten (große
  Schrift, ausreichender Kontrast, keine abgeschnittenen Texte, genug Abstand für
  Controller/Hand-Eingabe).
- Geräteabhängiges (Hintergrund-Audio, Fenster/Panels, räumlicher Klang,
  Eingabe per Controller/Hand) hinter Schnittstellen kapseln, sodass die Logik
  ohne Gerät testbar ist.
- Was nur auf der Brille geprüft werden kann, nie als erledigt melden. Stattdessen
  einen Eintrag in `docs/MANUAL_TESTS.md` anlegen: was zu testen ist, Schritte,
  erwartetes Ergebnis, zugehöriger Commit.

## ABS-Testserver (lokal, frei benutzbar)
- Läuft per Docker unter `~/abs-test`, erreichbar unter `http://localhost:13378`.
  Version: 2.37.1 (Stand 30.10.2026; maßgeblich ist `ABS_VERSION` in `.env.test`).
  Fixtures aufgezeichneter Antworten mit der Server-Version kennzeichnen.
- Zugangsdaten in `.env.test` (Vorlage: `.env.test.example`).
  In der Shell laden: `set -a && . ./.env.test && set +a`
- Integrationstests gegen diesen Server lesen `.env.test` über Gradle ein und
  werden übersprungen, wenn die Datei fehlt.
- Zurücksetzen auf den Ausgangszustand:
  `cd ~/abs-test && docker compose down && rm -rf config metadata && tar xzf ~/abs-test-baseline.tgz && docker compose up -d`
- Testdaten (LibriVox + Problemfälle wie M4B mit Kapiteln, Multi-MP3, Opus,
  Umlaute, 10 h) liegen unter `~/abs-test/audiobooks`. Neue Problemfälle in
  `~/abs-test/make-testdata.sh` ergänzen.
- **Niemals gegen einen anderen ABS-Server testen oder dessen Adresse ins Repo schreiben.**
  Spike S-3 (Test gegen Lorenz' echten Server, Spec §12) ist ein manueller Test auf der
  Quest: als Eintrag in `docs/MANUAL_TESTS.md` anlegen, ohne die Server-Adresse.

## Git
- Für jede Aufgabe einen eigenen Branch (`feature/…`, `fix/…`).
- Kleine, in sich lauffähige Commits mit aussagekräftiger Nachricht.
- Kein Push auf `main`, kein Force-Push, keine Änderung der Git-Historie.
- Keine Zugangsdaten, Tokens oder `.env.test` committen.

## Arbeitsweise
- Bei Unklarheiten in der Spec: nachfragen statt raten, wenn die Entscheidung
  Architektur oder Nutzerverhalten betrifft; sonst sinnvolle Annahme treffen und
  im Commit bzw. in der Zusammenfassung nennen.
- Neue Abhängigkeiten nur mit kurzer Begründung; bevorzugt AndroidX/Kotlin-Standard.
  Libraries mit großem Footprint, die nicht schon in ADR-005 stehen, vorher mit mir
  abstimmen (Spec §0 Regel 1).
- Am Ende jeder Aufgabe kurz zusammenfassen: was geändert wurde, wie es getestet
  wurde, was offen ist (inkl. neuer Einträge in `docs/MANUAL_TESTS.md`).
