# ShelfVR

Audiobookshelf-Client für Meta Quest (Horizon OS). Hörbücher von einem eigenen
[Audiobookshelf](https://www.audiobookshelf.org/)-Server in VR/MR hören – als normales Fenster,
als kompakter Mini-Player und später als Regal- und Plattenspieler-Fenster an der Wand.

**Status:** Phase 0 (Projektgerüst und Spikes). Die App zeigt derzeit nur eine Test-Oberfläche
für die Spikes S-1 bis S-3. Details zu Zielen, Architektur und Planung: [`PROJECT_SPEC.md`](PROJECT_SPEC.md).

## Voraussetzungen

| | Minimum | Empfohlen |
|---|---|---|
| Brille | Meta Quest 2 | Quest 3/3S |
| Horizon OS | noch offen (wird mit Spike S-1 festgelegt) | |
| Audiobookshelf | 2.26 (JWT-Auth) | ≥ 2.36 (Fix für CVE-2026-73085) |

Hinweis: Je nach Server-Konfiguration (`REFRESH_TOKEN_EXPIRY`) ist nach längerer Nichtnutzung
eine erneute Anmeldung nötig.

## Bauen

JDK 21 und Android SDK vorausgesetzt; Gradle nur über den Wrapper.

```bash
./gradlew assembleDebug                      # APK: app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest                  # Unit-Tests aller Module
./gradlew verifyRoborazziDebug               # Screenshot-Tests
./gradlew lintDebug
```

Jeder Push und jeder Pull Request baut über GitHub Actions; das Debug-APK hängt als Artefakt
`shelfvr-debug-apk` am jeweiligen Workflow-Lauf.

## Installieren (Sideload)

Quest im Entwicklermodus, dann per USB:

```bash
adb install -r app-debug.apk
```

Alternativ über SideQuest. Debug-APKs sind mit dem Debug-Schlüssel der jeweiligen Build-Maschine
signiert, und jeder CI-Lauf hat einen eigenen. Schlägt `adb install -r` mit
`INSTALL_FAILED_UPDATE_INCOMPATIBLE` fehl, vorher `adb uninstall de.lorenzschl.shelfvr` ausführen
(löscht die App-Daten).

## Lizenz

[MIT](LICENSE). Übernommener Fremdcode: [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md).
