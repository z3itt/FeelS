# Third-Party Notices and Attributions

FeelS (`com.z3itt.feels`) is Copyright (C) 2026 z3itt and licensed under the
[GNU General Public License v3.0 or later](LICENSE).

This file lists major third-party components used by FeelS and their licenses.
Dependency versions match `gradle/libs.versions.toml` at release time.

## Runtime libraries

| Component | Version | License | Notes |
|-----------|---------|---------|-------|
| [AndroidX Core KTX](https://developer.android.com/jetpack/androidx) | 1.15.0 | Apache-2.0 | Android Jetpack |
| [AndroidX AppCompat](https://developer.android.com/jetpack/androidx) | 1.7.0 | Apache-2.0 | Android Jetpack |
| [AndroidX Activity Compose](https://developer.android.com/jetpack/androidx) | 1.9.3 | Apache-2.0 | Android Jetpack |
| [AndroidX Lifecycle](https://developer.android.com/jetpack/androidx) | 2.8.7 | Apache-2.0 | Android Jetpack |
| [AndroidX Navigation Compose](https://developer.android.com/jetpack/androidx) | 2.8.5 | Apache-2.0 | Android Jetpack |
| [AndroidX DataStore Preferences](https://developer.android.com/jetpack/androidx) | 1.1.1 | Apache-2.0 | Local preferences |
| [AndroidX Room](https://developer.android.com/jetpack/androidx) | 2.6.1 | Apache-2.0 | Local database layer |
| [AndroidX SQLite KTX](https://developer.android.com/jetpack/androidx) | 2.4.0 | Apache-2.0 | SQLite helpers |
| [AndroidX Security Crypto](https://developer.android.com/jetpack/androidx) | 1.1.0-alpha06 | Apache-2.0 | Key handling |
| [AndroidX WorkManager](https://developer.android.com/jetpack/androidx) | 2.10.0 | Apache-2.0 | On-device reminders |
| [AndroidX Glance AppWidget](https://developer.android.com/jetpack/androidx/releases/glance) | 1.2.0-rc01 | Apache-2.0 | Home-screen widgets |
| [Jetpack Compose BOM](https://developer.android.com/jetpack/compose) | 2024.12.01 | Apache-2.0 | UI toolkit |
| [Material 3 for Compose](https://developer.android.com/jetpack/androidx/releases/compose-material3) | via BOM | Apache-2.0 | UI components |
| [Material Icons Extended](https://developer.android.com/jetpack/androidx/releases/compose-material) | via BOM | Apache-2.0 | Icons |
| [Dagger Hilt](https://dagger.dev/hilt/) | 2.52 | Apache-2.0 | Dependency injection |
| [AndroidX Hilt Navigation Compose](https://developer.android.com/jetpack/androidx) | 1.2.0 | Apache-2.0 | Compose + Hilt |
| [AndroidX Hilt Work](https://developer.android.com/jetpack/androidx) | 1.2.0 | Apache-2.0 | WorkManager + Hilt |
| [Kotlin Coroutines](https://github.com/Kotlin/kotlinx.coroutines) | 1.9.0 | Apache-2.0 | Async flows |
| [Kotlin Serialization JSON](https://github.com/Kotlin/kotlinx.serialization) | 1.7.3 | Apache-2.0 | Backup JSON |
| [SQLCipher for Android](https://www.zetetic.net/sqlcipher/) | 4.5.4 | BSD-style (Zetetic) | Encrypted local database |
| [Google Tink Android](https://github.com/tink-crypto/tink) | 1.8.0 (transitive) | Apache-2.0 | Via Security Crypto |

## Build-time tools

| Component | Version | License |
|-----------|---------|---------|
| [Kotlin](https://kotlinlang.org/) | 2.0.21 | Apache-2.0 |
| [Android Gradle Plugin](https://developer.android.com/studio/releases/gradle-plugin) | 8.7.3 | Apache-2.0 |
| [KSP](https://github.com/google/ksp) | 2.0.21-1.0.28 | Apache-2.0 |
| [JUnit](https://junit.org/) | 4.13.2 | EPL-1.0 |

## Fonts and visual assets

- UI typography uses the Android system sans-serif stack (Roboto on most devices).
  No bundled font files ship with FeelS.
- Launcher icon, splash artwork, and widget previews are original FeelS assets
  created for this project unless noted otherwise in the repository.

## Transitive permissions

WorkManager may merge standard Android permissions such as
`RECEIVE_BOOT_COMPLETED`, `WAKE_LOCK`, and `ACCESS_NETWORK_STATE` so scheduled
check-in reminders can resume after reboot. FeelS does not declare `INTERNET`
and does not transmit check-in data off the device.

## License texts

- GNU GPL v3: [LICENSE](LICENSE) and [COPYING](COPYING)
- Apache-2.0: https://www.apache.org/licenses/LICENSE-2.0
- SQLCipher: https://www.zetetic.net/sqlcipher/license/

For questions about licensing or attribution, contact info@z3itt.com.
