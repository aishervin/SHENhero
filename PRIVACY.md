# SHEN Hero Privacy Notice

**Status:** Draft for the publisher to review and publish before distributing the app.

SHEN Hero is an Android assistant. This notice describes the current app implementation; it should be checked against the final app, Google Play Data safety answers, and the publisher's legal obligations.

## Information handled by the app

- Your Gemini API key is stored encrypted on your device using a key protected by Android Keystore. It is sent to Google's Gemini API in an HTTPS request header when you send a prompt.
- Your prompts and the latest 20 saved chat turns are sent to Google's Gemini API to generate a response. If web search is enabled, the request also asks Gemini to use Google Search grounding.
- When you choose and send a photo, the app resizes and converts it on your device, then sends it with the prompt to Gemini. The app does not save the original photo.
- If you use dictation, the Android speech-recognition service processes microphone input and returns recognized text to the app. SHEN Hero does not save the audio recording. The service provider's privacy terms may apply to speech processing.
- Chat text is encrypted in the app's local database. Android app backup is disabled. Use **Settings → Clear chat history** to remove saved conversations from this device. Clearing app storage or uninstalling also removes app data.

## Sharing and retention

The app publisher does not operate a server or receive app conversations in the current version. Google processes prompts, conversation context, submitted images, and Search grounding requests to provide the requested Gemini features. The Android speech-recognition provider may process audio used for dictation. Their handling and retention are governed by their own terms and privacy notices.

Chat history remains on the device until you clear it or remove app data. The app does not currently include advertising, analytics, user accounts, or a remote chat-sync service.

## Security and choices

Encryption protects saved key and chat text at rest on a normally operating device. It cannot protect data on a compromised device or prevent the selected service provider from processing information you submit. Avoid sending passwords, payment details, or other sensitive information. You can decline microphone access and still use typed chat and image questions. You can clear saved chat history in Settings.

## Children and contact

The app is not designed for children. Before publication, the publisher must add an appropriate contact address and a stable public URL for this notice, and must ensure this text matches actual provider settings and applicable law.
