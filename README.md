# SmartGuard (Kotlin + Jetpack Compose)

A lightweight, privacy‑friendly scam awareness app based on your SmartGuard FYP spec.

## Features
- Jetpack Compose UI: Home, Tips, Quiz, Scenarios, History
- Keyword‑based detection engine (on‑device)
- Notification Listener prototype to observe incoming notifications from messaging apps and create alerts
- DataStore (Preferences) to persist alert history (JSON‑encoded). **Note:** add an encryption layer later (e.g., EncryptedFile + DataStore) if required by policy.

## Build
1. Open the project in **Android Studio Jellyfish/Koala+**.
2. Let Gradle sync. If prompted, upgrade AGP/Compose versions.
3. Run on Android 8.0+ device/emulator.

## Enable Alerts
- Go to **Settings → Notifications → Notification access** and enable **SmartGuard Notification Listener**.
- Open the app → **View Potential Scam Messages** → tap **Seed Demo Alerts** to see sample data.

## Notes
- Intercepting SMS as the default SMS app is not included (modern Android restrictions). Using the notification listener is a practical, privacy‑friendly route for WhatsApp/Telegram/SMS notifications.
- For encrypted persistence, consider wrapping DataStore storage with a file‑level encryption layer (e.g., Jetpack Security's `EncryptedFile`) or move to `Room + SQLCipher` for larger datasets.

## Packages
- `com.smartguard.app.ui` — Compose screens
- `com.smartguard.app.data` — keyword repo, detection engine, history store
- `com.smartguard.app.notifications` — notification listener service
- `com.smartguard.app.viewmodel` — HistoryViewModel

---
© 2025 SmartGuard

## Upgrades added
- SMS BroadcastReceiver to capture incoming SMS (requires RECEIVE_SMS and runtime permission).
- Encrypted Room database using SQLCipher (net.zetetic) via SupportFactory. The DB passphrase is currently a fixed string in code `smartguard-secret-passphrase` — change to a secure runtime key management option for production (e.g., derive from user PIN and keystore).
- Keywords storage using EncryptedSharedPreferences.
- Branded Compose theme (purple primary color) matching the project PDF.

### Important security notes
- The SQLCipher passphrase should never be hard-coded for production. Use Android Keystore to protect keys or derive them at runtime.
- The SMS receiver requires user consent and runtime permission. On Android 4.4+ you can receive SMS broadcasts without being default SMS app, but some OEMs or Android versions may restrict behavior.
