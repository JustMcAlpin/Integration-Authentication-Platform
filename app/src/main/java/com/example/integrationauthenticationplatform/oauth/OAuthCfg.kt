package com.example.integrationauthenticationplatform.oauth

import com.example.integrationauthenticationplatform.BuildConfig
import com.example.integrationauthenticationplatform.model.ProviderGroup

data class OAuthCfg(
    val group: ProviderGroup,
    val clientId: String,
    val redirectUri: String,
    val authEndpoint: String,
    val tokenEndpoint: String,
    val scopes: List<String>,
    val extraParams: Map<String, String> = emptyMap()
)

object OAuthConfigs {
    val google = OAuthCfg(
        group = ProviderGroup.Google,
        clientId = "990112477927-oe9qfesiink1jrdasu38h7jh12cck4m9.apps.googleusercontent.com",
        redirectUri = "com.googleusercontent.apps.990112477927-oe9qfesiink1jrdasu38h7jh12cck4m9:/oauth2redirect",
        authEndpoint = "https://accounts.google.com/o/oauth2/v2/auth",
        tokenEndpoint = "https://oauth2.googleapis.com/token",
        scopes = listOf(
            "https://www.googleapis.com/auth/calendar",
            "https://www.googleapis.com/auth/drive.readonly",
            "https://www.googleapis.com/auth/spreadsheets.readonly",
            "https://www.googleapis.com/auth/gmail.readonly"
        ),
        // ask for refresh token
        extraParams = mapOf("access_type" to "offline", "prompt" to "consent")
    )

    val microsoft = OAuthCfg(
        group = ProviderGroup.Microsoft,
        clientId = "76f2bdbf-de19-4844-be6a-1e53f2299a3d",
        redirectUri = "com.example.integrationauth://oauth2redirect",
        authEndpoint = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize",
        tokenEndpoint = "https://login.microsoftonline.com/common/oauth2/v2.0/token",
        scopes = listOf(
            "offline_access",
            "https://graph.microsoft.com/Files.Read",
            "https://graph.microsoft.com/Calendars.Read",
            "https://graph.microsoft.com/Mail.Read"
        ),
        extraParams = mapOf("prompt" to "select_account")
    )

    val linkedin = OAuthCfg(
        group = ProviderGroup.Social,
        clientId = BuildConfig.LINKEDIN_CLIENT_ID,
        redirectUri = "com.example.integrationauth://oauth2redirect",
        authEndpoint = "https://www.linkedin.com/oauth/v2/authorization",
        tokenEndpoint = "https://www.linkedin.com/oauth/v2/accessToken",
        scopes = listOf("r_liteprofile", "r_emailaddress", "offline_access")
        // LinkedIn PKCE for native apps is supported. :contentReference[oaicite:5]{index=5}
    )

    val x = OAuthCfg(
        group = ProviderGroup.Social,
        clientId = BuildConfig.TWITTER_CLIENT_ID,
        redirectUri = "com.example.integrationauth://oauth2redirect",
        authEndpoint = "https://twitter.com/i/oauth2/authorize",
        tokenEndpoint = "https://api.x.com/2/oauth2/token",
        scopes = listOf("users.read", "tweet.read", "offline.access")
        // X OAuth2 + PKCE & refresh/revoke endpoints. :contentReference[oaicite:6]{index=6}
    )
}
