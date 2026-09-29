# SHEN Hero

SHEN Hero is an Android assistant built with Kotlin and Jetpack Compose. The current release scope is Gemini 3.8 Flash chat with recent-turn context, optional Google Search grounding, image questions, and speech-to-text dictation.

## Requirements

- Android Studio with JDK 17
- Android SDK Platform 36, including extension level 1
- A Gemini API key from [Google AI Studio](https://aistudio.google.com/app/apikey)

On macOS or Linux, run `chmod +x gradlew` once if the executable bit was not preserved when downloading the source. Then open the project in Android Studio, let the checked-in Gradle 9.3.1 wrapper sync, install the app on an Android 7.0 (API 24) or newer device, and enter your key in Settings. On Windows, use `gradlew.bat`. The app asks for microphone access only when you tap the microphone. Speech recognition is provided by the Android device's installed recognition service.

## Included features

- Gemini chat with the latest 20 saved turns as context
- Optional Google Search grounding, enabled with the search button beside the message field
- Image understanding; images are resized on-device before upload
- Voice dictation into the message field; the user reviews and sends the recognized text. Audio is handled by the device's speech-recognition service and may be processed according to that provider's terms.
- Encrypted local storage for the Gemini key and chat text using an Android Keystore AES-GCM key
- Local chat database is excluded from Android backup

The other-provider keys, maps, autonomous actions, and speech-to-speech mode from the early prototype are not presented as working integrations. They need separate implementations and, for sensitive multi-user integrations, a backend.

## API key and privacy

Each installation supplies its own Gemini key. The key is encrypted at rest and sent in the HTTPS `x-goog-api-key` header; it is not compiled into the APK or written to logs. The app sends the current prompt and recent conversation turns to Google's Gemini API. Search mode also enables Google Search grounding. Image mode sends a resized JPEG to Gemini. Do not send sensitive data unless you are comfortable with the provider processing it.

Device-side encryption protects data at rest, but it cannot make a client API key impossible to extract from a compromised or instrumented device. For a public service that pays for all users, put model calls behind an authenticated, rate-limited backend and never distribute a shared server key in the app.

`PRIVACY.md` is a draft notice matching the current app behavior. The publisher must review it, add contact details, publish it at a stable public URL, and align the store's Data safety form before release.

## Checks and debug APKs

The `Android build and persistent-key release` GitHub Actions workflow runs `testDebugUnitTest` and builds unsigned release and debug APKs on branch pushes, pull requests, and manual runs. Successful runs publish the debug APKs as a downloadable workflow artifact.

## Signed releases

Create one permanent JKS keystore and keep a backup outside the repository. Never regenerate it for a later release, and never commit the keystore or its passwords. Gradle creates unsigned release APKs; the release workflow aligns each APK, signs it with the repository keystore, verifies v1/v2/v3 signatures, checks its SHA-256 certificate fingerprint against the pin in the workflow, and removes the temporary decoded keystore when signing ends.

Configure these repository Actions secrets:

- `SIGNING_KEY`: Base64-encoded permanent JKS file
- `KEYSTORE_PASSWORD`: master keystore password
- `KEY_ALIAS`: alias inside the keystore
- `KEY_PASSWORD`: key-specific password

Also configure the non-secret repository Actions variable `SIGNING_CERT_SHA256` with the key's SHA-256 certificate fingerprint (hexadecimal, colons optional). The workflow refuses to sign if either the keystore or any APK has a different fingerprint.

Add the secrets and variable under **Settings > Secrets and variables > Actions**. Push a version tag such as `v1.0.0`, or run the workflow manually and provide `version_tag`. For a non-publishing signing check, manually run the workflow with `signing_check` enabled. The release job only runs after tests and the debug build pass. It publishes aligned, verified universal and architecture-specific APKs to a GitHub Release. Always use this same keystore for direct APK updates.

The Gradle wrapper verifies the Gradle 9.3.1 distribution checksum before using it.
