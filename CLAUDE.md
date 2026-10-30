# ShelfVR

Audiobookshelf-Client für Meta Quest (Horizon OS, Android-14-Basis).
Kotlin, Jetpack Compose, Gradle (Kotlin DSL).

**Vor jeder größeren Änderung `PROJECT_SPEC.md` lesen.** Sie ist die verbindliche
Quelle für Funktionsumfang, Architektur und Prioritäten. Widerspricht etwas hier
der Spec, gilt die Spec – und weise mich auf den Widerspruch hin.

## Umgebung
- Headless Ubuntu-VM, kein Android Studio, kein Emulator, **keine Quest verbunden**.
- JDK 21 (Temurin), Android SDK unter `$ANDROID_HOME`.
- Gradle und Kotlin nur über den Wrapper (`./gradlew`), nichts systemweit installieren.
- Versionen von Abhängigkeiten und Plugins nur in `gradle/libs.versions.toml`.

## Build & Test
- Build: `./gradlew assembleDebug`
- Unit-Tests: `./gradlew testDebugUnitTest`
- Screenshot-Tests prüfen: `./gradlew verifyRoborazziDebug`
- Screenshot-Referenzen neu aufnehmen: `./gradlew recordRoborazziDebug`
  (nur bei gewollten UI-Änderungen; im Commit begründen)
- Lint: `./gradlew lintDebug`

Diese Befehle beschreiben den Zielzustand. Solange das Projektgerüst noch nicht
steht, ist das Anlegen genau dieser Tasks Teil der Aufgabe. Ändern sich Task-Namen
oder Pfade, diese Datei im selben Commit anpassen.

Vor jedem Commit müssen Build, Unit-Tests und Screenshot-Tests grün sein.

## Arbeiten ohne Quest
- Kein `adb`, keine Gerätetests. Alles, was ohne Gerät prüfbar ist, wird ohne Gerät geprüft.
- UI-Kontrolle über Screenshot-Tests: PNGs liegen unter
  `app/build/outputs/roborazzi/`. Nach UI-Änderungen die Bilder **ansehen** und auf
  Lesbarkeit in VR achten (große Schrift, ausreichender Kontrast, keine
  abgeschnittenen Texte, genug Abstand für Controller/Hand-Eingabe).
- Geräteabhängiges (Hintergrund-Audio, Fenster/Panels, räumlicher Klang,
  Eingabe per Controller/Hand) hinter Schnittstellen kapseln, sodass die Logik
  ohne Gerät testbar ist.
- Was nur auf der Brille geprüft werden kann, nie als erledigt melden. Stattdessen
  einen Eintrag in `docs/MANUAL_TESTS.md` anlegen: was zu testen ist, Schritte,
  erwartetes Ergebnis, zugehöriger Commit.

## ABS-Testserver (lokal, frei benutzbar)
- Läuft per Docker unter `~/abs-test`, erreichbar unter `http://localhost:13378`.
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
- Am Ende jeder Aufgabe kurz zusammenfassen: was geändert wurde, wie es getestet
  wurde, was offen ist (inkl. neuer Einträge in `docs/MANUAL_TESTS.md`).
