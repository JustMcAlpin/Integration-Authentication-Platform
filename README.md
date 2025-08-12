https://github.com/user-attachments/assets/2064b254-ddfc-4851-a7c4-5e39d2582c8b


Integration Authentication Platform
This app connects Google, Microsoft, Twilio, and SendGrid accounts. It uses OAuth 2.0 or API keys, stores credentials securely, and gives users a simple way to see what’s connected.

What’s Working
Google (Calendar, Drive, Sheets, Gmail)

Microsoft (Mail, Calendar, OneDrive)

Twilio (SID + Auth Token)

SendGrid (API Key)

Credentials are encrypted with AES-256-GCM, and the encryption key is provided via local gradle.properties or env vars. Nothing is hardcoded.

Excluded Platforms
Instagram, TikTok, Facebook, LinkedIn, and X weren’t included due to requirements like third-party app approval, backend token handling, or non-standard auth flows. Given the scope and time constraints, I focused on platforms that allowed full integration within the demo’s limits.

Setup
Clone the repo

Copy gradle.properties.example → gradle.properties

Fill in the real values (don’t commit them)

Build and run

Notes
Some earlier commits included placeholder values that looked like real credentials. They weren’t. GitHub flagged them anyway — they’ve since been removed or whitelisted.
