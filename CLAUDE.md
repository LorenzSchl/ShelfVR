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
- Einzelner Test: `./gradlew :core:network:testDebugUnitTest --tests "*.AuthTest"`
- Screenshot-Tests prüfen: `./gradlew verifyRoborazziDebug`
- Screenshot-Referenzen neu aufnehmen: `./gradlew recordRoborazziDebug`
  (nur bei gewollten UI-Änderungen; im Commit begründen)
- Lint: `./gradlew lintDebug`
- Alles wie in CI: `./gradlew assembleDebug testDebugUnitTest verifyRoborazziDebug lintDebug`

Ändern sich Task-Namen oder Pfade, diese Datei im selben Commit anpassen.

Reine Kotlin-Module (`:core:model`, `:core:network`, `:domain`) haben eigentlich nur
`test` und `lint`. Das Convention-Plugin `shelfvr.jvm.library` registriert dort
`testDebugUnitTest` und `lintDebug` als Aliase, damit die Befehle oben alle Module erfassen.
Neue reine Kotlin-Module deshalb immer mit diesem Plugin anlegen.

Vor jedem Commit: Build, Unit-Tests und Screenshot-Tests grün, Lint ohne Errors.
Commit-Nachrichten nennen die Anforderungs-ID aus der Spec (FR-xx, R-xx, P-xx, NFR-xx),
sofern eine betroffen ist.

## Projektaufbau
- Modulschnitt nach Spec §5.1. Gemeinsame Build-Konfiguration steckt in
  Convention-Plugins unter `build-logic/` (`shelfvr.android.library`, `…compose`,
  `…roborazzi`, `shelfvr.hilt`, `shelfvr.jvm.library`). Namespaces von
  Android-Libraries werden aus dem Modulpfad abgeleitet (`:core:network` →
  `de.lorenzschl.shelfvr.core.network`).
- SDK-Level stehen im Version Catalog: compileSdk 37 (von der Compose-BOM verlangt),
  targetSdk 34 (Android-Basis von Horizon OS), minSdk 32.
- Persistenz: Room in `:core:database` (Schemas versioniert unter `core/database/schemas/`;
  jede Schemaänderung = neue Version + Migration + Test in `MigrationTest`, nie destruktiv),
  Einstellungen und HTTP-Zustimmungen per DataStore in `:core:datastore`, Tokens verschlüsselt
  (Tink + Android Keystore) in `:data`. Hilt-Module liegen im jeweiligen Modul.
- `app/src/main/kotlin/…/spike/` ist Wegwerf-Code für Phase 0 (Spikes S-1 bis S-3),
  ohne String-Ressourcen. Nach Abschluss der Spikes entfernen, nicht ausbauen.

## Arbeiten ohne Quest
- Kein `adb`, keine Gerätetests. Alles, was ohne Gerät prüfbar ist, wird ohne Gerät geprüft.
- Die UI-Smoke-Tests im Simulator/Emulator aus NFR-08 ersetzen hier Screenshot-Tests
  mit Roborazzi.
- UI-Kontrolle über Screenshot-Tests (Roborazzi + Robolectric): Referenzbilder liegen
  versioniert unter `<modul>/src/test/screenshots/`, Vergleichsbilder und Diffs nach
  `verifyRoborazziDebug` unter `<modul>/build/outputs/roborazzi/`. Screenshot-Tests mit
  deutschem Locale (`@Config(qualifiers = "de-…")`) und realistischer Fenstergröße.
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
- Integrationstests gegen diesen Server (`:core:network`, `AbsServerIntegrationTest`)
  lesen `.env.test` selbst ein (`TestEnv`; Gradle reicht nur den Pfad durch) und werden
  übersprungen, wenn die Datei fehlt – so auch in CI. Sie verändern den Hörfortschritt
  des Test-Users; bei Bedarf den Server zurücksetzen.
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
