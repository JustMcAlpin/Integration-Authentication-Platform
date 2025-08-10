package com.example.integrationauthenticationplatform.ui.oauth

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
        clientId = "<GOOGLE_CLIENT_ID>",
        redirectUri = "com.example.integrationauth:/oauth2redirect",
        authEndpoint = "https://accounts.google.com/o/oauth2/v2/auth",
        tokenEndpoint = "https://oauth2.googleapis.com/token",
        scopes = listOf(
            "openid","email","profile",
            "https://www.googleapis.com/auth/calendar",
            "https://www.googleapis.com/auth/drive.readonly",
            "https://www.googleapis.com/auth/spreadsheets.readonly",
            "https://www.googleapis.com/auth/gmail.readonly"
        ),
        extraParams = mapOf("access_type" to "offline", "prompt" to "consent")
    )

    val microsoft = OAuthCfg(
        group = ProviderGroup.Microsoft,
        clientId = "<MICROSOFT_CLIENT_ID>",
        redirectUri = "com.example.integrationauth:/oauth2redirect",
        authEndpoint = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize",
        tokenEndpoint = "https://login.microsoftonline.com/common/oauth2/v2.0/token",
        scopes = listOf(
            "offline_access","openid","profile",
            "https://graph.microsoft.com/Files.Read",
            "https://graph.microsoft.com/Calendars.Read",
            "https://graph.microsoft.com/Mail.Read"
        )
    )
}
