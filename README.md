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

The `Android build and release` GitHub Actions workflow runs `testDebugUnitTest` and builds debug APKs on pushes to `main`, pull requests, and manual runs. Successful runs publish the debug APKs as a downloadable workflow artifact.

## Signed releases

Create one upload keystore and keep a secure backup outside the repository. Do not commit the keystore or its passwords. Configure these repository Actions secrets before publishing:

For example, create a keystore locally with `keytool -genkeypair -v -keystore shenhero-upload.jks -alias shenhero -keyalg RSA -keysize 3072 -validity 10000`, then securely Base64-encode that file for the repository secret. Keep the original keystore and passwords backed up separately.

- `ANDROID_KEYSTORE_BASE64`: Base64-encoded keystore file
- `ANDROID_KEYSTORE_PASSWORD`: keystore password
- `ANDROID_KEY_ALIAS`: alias inside the keystore
- `ANDROID_KEY_PASSWORD`: key password

Push a version tag such as `v1.0.0`, or run the workflow manually and provide `version_tag`. The release job only runs after tests and the debug build pass. It signs and uploads universal and architecture-specific APKs to a GitHub Release. Keep using the same keystore for future releases so users can install updates over the previous version.

The Gradle wrapper verifies the Gradle 9.3.1 distribution checksum before using it.
