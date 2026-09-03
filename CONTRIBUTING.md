# Contributing to FeelS

FeelS is free software under [GPL-3.0-or-later](LICENSE). Contributions must
be compatible with that license. Open an issue or pull request on GitHub.

## Development setup

### Requirements

- Android Studio Ladybug or newer (or compatible IDE)
- **JDK 17** for Gradle (Gradle 8.11.1 does not run on JDK 25)
- Android SDK with API 35 platform tools
- Device or emulator on API 26+

### Clone and run (debug)

```bash
git clone https://github.com/z3itt/FeelS.git
cd FeelS
```

Open the project in Android Studio, set **Gradle JDK** to 17, sync, then Run.

Or from the terminal:

```bash
export JAVA_HOME="$HOME/.jdks/temurin-17.0.20.1"   # or your JDK 17 path
./gradlew :app:installDebug
```

### Tests

```bash
./gradlew :core:domain:testDebugUnitTest :core:data:testDebugUnitTest
```

## Release APK (signed)

Signing uses environment variables. Never commit the keystore or passwords.

```bash
export FEELS_STORE_FILE="$HOME/feels-release.jks"
export FEELS_STORE_PASSWORD="your-store-password"
export FEELS_KEY_ALIAS="feels"
export FEELS_KEY_PASSWORD="your-key-password"

./gradlew :app:assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

Attach that APK to a GitHub Release tagged with the matching version (for example
`v1.0.0`).

## Distribution

| Channel | Notes |
|---------|-------|
| GitHub Releases | Signed APK from the maintainer keystore |
| F-Droid | Builds from source and signs with the F-Droid key |
| Google Play | Not published by default |

F-Droid does not use your keystore. Store listing assets live under
[`fastlane/metadata/android/en-US/`](fastlane/metadata/android/en-US/).

## Pull requests

- Keep changes focused and match existing code style.
- Run unit tests before opening a PR.
- Update `fastlane/metadata/android/en-US/changelogs/` when shipping a user-facing release.
