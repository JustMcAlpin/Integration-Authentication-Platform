Integration Authentication Platform
This app connects Google, Microsoft, Twilio, and SendGrid accounts using OAuth 2.0 or API keys.
It stores credentials securely (AES-256-GCM), shows what’s connected in a clean UI, and works with minimal config.

✅ What's Working

Google:
Calendar, Drive, Sheets, Gmail

Microsoft:
Outlook Mail, Calendar, OneDrive

Twilio:
Account SID + Auth Token

SendGrid:
API Key only

TikTok: 
v2 Login Kit with PKCE and App Link redirect

X (Twitter):
Account SID

All credentials are encrypted at rest.
The encryption key is provided via gradle.properties or environment variables—nothing is hardcoded.

⚠️ What's Implemented but Not Live

The following platforms are fully wired up (UI, auth flows, token exchange, storage), but not verified end-to-end due to missing dev accounts:

Instagram

Facebook

LinkedIn



These require extra setup like partner approval, backend flows, or production apps.
They're all integrated but not fully connected.

🧪 Tests

Includes core unit tests for the DashboardViewModel covering:

OAuth success for one service

OAuth success across a group (e.g. Google)

API key storage

In a production app, I'd cover disconnect logic, error handling, and persistence edge cases—but time.

🚀 Setup

Clone this repo

Copy gradle.properties.example → gradle.properties

Add your client IDs and keys

Build & run in Android Studio

Demo mode is enabled in debug builds.
If any service is missing config, the app fakes credentials for testing purposes.

🧱 Tech Stack

Jetpack Compose

Room

AppAuth (OAuth 2.0)

Ktor (API validation)

AES‑256‑GCM (local encryption)

🫠 Final Note

Yeah, this was a big take-home. I focused on real integrations over dummy mocks, with actual token flows and storage.
Let me know if you want a walkthrough or deeper dive.
