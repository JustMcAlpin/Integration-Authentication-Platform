Integration Authentication Platform (Android, Jetpack Compose)
Lightweight demo app that authenticates with multiple third-party services and securely stores credentials. It’s built for capability demo purposes—simple UI, strong security basics.

What it does (MVP)
Dashboard of “cards” for required services

Two auth modes:

OAuth 2.0: Google (Calendar, Drive, Sheets, Gmail) — one successful Google auth marks all Google cards connected

API Key: SendGrid and Twilio — enter a key and we store it securely

Credentials are encrypted with AES-256-GCM and saved in a local Room DB

“Disconnect” removes credentials (and best-effort revoke for Google)

State persists across app restarts

Quick Start
Prereqs
Android Studio (Giraffe+), JDK 11

Android emulator or device with internet

1) Generate an encryption key (32 bytes, base64)
macOS/Linux

openssl rand -base64 32
Windows PowerShell

$b = New-Object 'System.Byte[]' 32
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b)
[Convert]::ToBase64String($b)
2) Provide the key to Gradle
Add to your user Gradle properties (recommended):

# ~/.gradle/gradle.properties  (Windows: %USERPROFILE%\.gradle\gradle.properties)
ENCRYPTION_KEY=PASTE_BASE64_STRING_HERE
We do not commit secrets. The app reads this into BuildConfig.ENCRYPTION_KEY_B64.

3) Configure Google OAuth (one time)
Create an OAuth 2.0 client in Google Cloud (Installed app – Android / or generic OAuth client).

Note the Client ID (looks like ...apps.googleusercontent.com).

Compute the redirect scheme:
com.googleusercontent.apps.<client-id-without-the-domain>
Example:
Client ID: 990112477927-xxxx.apps.googleusercontent.com
Scheme: com.googleusercontent.apps.990112477927-xxxx

Update the project:

In app/build.gradle.kts → defaultConfig:

manifestPlaceholders["appAuthRedirectScheme"] =
    "com.googleusercontent.apps.990112477927-xxxx"
In OAuthConfigs.kt:

clientId    = "990112477927-xxxx.apps.googleusercontent.com",
redirectUri = "com.googleusercontent.apps.990112477927-xxxx://oauth2redirect",
That’s it—no SHA-1 needed for the custom scheme redirect.

4) Build & run
Sync Gradle, run the app

Tap Connect on a Google card → complete consent

Tap SendGrid or Twilio → paste a dummy key → Save

How to Use It (Demo Script)
Before: open app → all cards are Disconnected

API Key path:

Tap SendGrid → paste a key (can be dummy for demo) → Save

Card flips to Connected

Kill the app and reopen → still Connected (persistence ✅)

Tap Disconnect → back to Disconnected

OAuth path (Google):

Tap any Google card → OAuth consent → approve

On success, all Google cards switch to Connected

Tap Disconnect on any one of them → we remove stored creds and best-effort revoke

Social (Instagram/TikTok/X/Facebook/LinkedIn/Snapchat) cards are shown for architecture completeness and are marked Requires approval; they’re disabled in the MVP.

Architecture
UI: Jetpack Compose (Material 3)

DashboardScreen → grid of IntegrationCards

Small dialogs for API keys (ApiKeyDialog)

State: DashboardViewModel

Persists/loads via CredentialRepo

onOAuthSuccess(group, json) marks all services in a provider group as connected

OAuth: AppAuth (AuthActivity)

Builds the auth request, handles redirect, exchanges code for tokens, returns JSON payload to MainActivity

Storage: Room (AppDb)
Table (conceptual):

bash
Copy
Edit
id | service | auth_type | encrypted_data | iv | created_at
Crypto: AES-256-GCM

Key: from ENCRYPTION_KEY (base64)

Unique IV per record (stored alongside ciphertext)

Data stored as JSON then encrypted

Security Details
Algorithm: AES-256-GCM

Per-record IV randomly generated

Key handling: 32-byte key provided via environment/gradle properties; never committed

What we store (examples, before encryption):

// OAuth
{
  "access_token": "ya29....",
  "refresh_token": "1//0g...",
  "expires_at": 1735689600,
  "scope": "https://www.googleapis.com/auth/calendar ..."
}
// API Key (SendGrid)
{ "api_key": "SG.***" }
Key Rotation (notes)
Generate a new key and update ENCRYPTION_KEY

Existing rows won’t decrypt with the new key; simplest path in MVP is to Disconnect and Reconnect each service to re-encrypt with the new key

A full migration tool can be added later to re-encrypt in place

Disconnect Behavior
API Keys: delete stored credentials, flip card to Disconnected

Google OAuth: same as above + best-effort token revoke

Reconnect works immediately

Implementation Status
Google OAuth: ✅ end-to-end working; one auth lights multiple Google services

SendGrid: ✅ API Key flow working (stores key securely)

Twilio: ✅ API Key flow working (single field; can be extended to SID + Secret)

Microsoft (Calendar/Mail/OneDrive): UI wired; config stubbed for future

Socials: UI only, marked “Requires approval”

Known Limits (MVP)
We don’t validate API keys against external APIs in the MVP (keeps the demo self-contained)

No background token refresh; tokens are stored (with refresh_token when consented) but not auto-refreshed yet

Credentials are stored locally only (no backend)

Social providers typically require app approval; we’ve left them disabled as examples

Dev Notes
Compose: Kotlin 2.0+ requires the Compose Compiler plugin (already configured)

Redirect: we use the scheme com.googleusercontent.apps.<client-id-no-domain>://oauth2redirect

Manifest placeholder appAuthRedirectScheme must match the scheme above

.gitignore excludes build outputs and local property files

Screenshots (optional)
