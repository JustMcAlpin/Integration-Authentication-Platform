Integration Authentication Platform (Android · Jetpack Compose)
Lightweight Android demo that authenticates with multiple third-party services and securely stores credentials. It’s built to show capability — simple UI, strong security basics.

What it does (MVP)
• Dashboard of cards for the required services
• Two auth modes
– OAuth 2.0: Google (Calendar, Drive, Sheets, Gmail). One successful Google sign-in marks all Google cards “Connected.”
– API Key: SendGrid and Twilio. Enter a key; we encrypt and store it.
• AES-256-GCM encryption for all credentials (unique IV per record)
• Room database for persistence
• “Disconnect” removes credentials (and best-effort revoke for Google)
• State persists across app restarts

Quick start
Prereqs
• Android Studio (Giraffe or newer), JDK 11
• Emulator or device with internet

Generate a 32-byte base64 encryption key
• macOS/Linux: run “openssl rand -base64 32” in a terminal
• Windows PowerShell: create a 32-byte random array and convert to base64 (any standard guide works)

Provide the key to Gradle (do not commit it)
• Add to your user Gradle properties file
– macOS/Linux: ~/.gradle/gradle.properties
– Windows: %USERPROFILE%.gradle\gradle.properties
• Add a line: ENCRYPTION_KEY=your_base64_value
• The app reads this into BuildConfig.ENCRYPTION_KEY_B64.

Configure Google OAuth (one-time)
• Create an OAuth 2.0 client in Google Cloud (Installed App or generic OAuth client).
• Note the Client ID (looks like: 123456…-xxxx.apps.googleusercontent.com).
• Compute the redirect scheme: com.googleusercontent.apps.<client-id without “.apps.googleusercontent.com”>
Example:
– Client ID: 990112477927-xxxx.apps.googleusercontent.com
– Scheme: com.googleusercontent.apps.990112477927-xxxx
• Update the project in two places:
– Manifest placeholder: set appAuthRedirectScheme to the scheme above.
– OAuth configs file: set clientId to your full client ID, and redirectUri to “<your scheme>://oauth2redirect”.

Build & run
• Sync Gradle and run the app from Android Studio.
• Tap “Connect” on a Google card to complete consent.
• Tap “SendGrid” or “Twilio” to enter a key and save.

Demo script (what to show)
API Key path

Tap SendGrid, paste a key (can be dummy for demo), Save.

Card flips to Connected.

Kill the app and reopen — still Connected (persistence).

Tap Disconnect — back to Disconnected.

OAuth path (Google)

Tap any Google card.

Complete consent.

On success, all Google cards switch to Connected.

Tap Disconnect on any Google card — we remove creds and attempt token revoke.

Note: Social cards (Instagram, TikTok, X, Facebook, LinkedIn, Snapchat) are present for architecture completeness and are marked “Requires approval” in this MVP.

Architecture
• UI: Jetpack Compose (Material 3)
– DashboardScreen renders a grid of IntegrationCards
– ApiKeyDialog for simple key entry

• State: DashboardViewModel
– Persists/loads via CredentialRepo
– onOAuthSuccess(group, json) marks all services in a provider group as connected

• OAuth: AppAuth (AuthActivity)
– Builds the authorization request, handles the custom-scheme redirect, exchanges code for tokens, returns a JSON payload to MainActivity

• Storage: Room (AppDb)
– Table columns: id, service, auth_type, encrypted_data, iv, created_at

Security details
• Algorithm: AES-256-GCM
• Per-record IV randomly generated and stored alongside ciphertext
• Key handling: 32-byte key supplied via environment/Gradle properties; never committed to source control
• We encrypt a small JSON blob per service (for OAuth: access token, refresh token when present, expiry epoch, scope; for API keys: the key)

Key rotation (MVP approach)
• Generate a new base64 key and update ENCRYPTION_KEY in your Gradle properties.
• Existing rows won’t decrypt with a new key. In this MVP, the simplest path is: Disconnect each service and reconnect to re-encrypt with the new key.
• A future migration tool could re-encrypt in place.

Disconnect behavior
• API keys: delete stored credentials and flip card to Disconnected.
• Google OAuth: delete creds and try to revoke the token.
• Reconnect works immediately.

Implementation status
• Google OAuth: end-to-end working; one auth lights multiple Google services.
• SendGrid: API-key flow working (secure storage).
• Twilio: API-key flow working (single-field MVP; easily extend to SID + Secret).
• Microsoft (Calendar/Mail/OneDrive): UI wired; config stubbed for later.
• Social providers: UI only, marked “Requires approval”.

Known limits (MVP)
• No external validation of API keys (keeps the demo self-contained).
• No background token refresh yet; tokens are stored (with refresh_token when consented) but not auto-refreshed.
• Credentials are local only; there’s no backend in this MVP.
• Social providers typically require app approval, so they’re intentionally disabled here.

Dev notes
• Kotlin 2.x with Compose requires the Compose Compiler Gradle plugin (already configured).
• The custom redirect scheme must match your Google client — example pattern: com.googleusercontent.apps.<client-id-without-domain>://oauth2redirect.
• The manifest’s appAuthRedirectScheme placeholder must equal that scheme.
• Do not commit secrets: keep ENCRYPTION_KEY and any API keys out of the repo.
• .gitignore excludes build outputs and local config.
