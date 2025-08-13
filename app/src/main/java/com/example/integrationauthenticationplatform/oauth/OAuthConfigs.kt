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

    val x = OAuthCfg(
        group = ProviderGroup.Social,
        clientId = BuildConfig.TWITTER_CLIENT_ID,
        redirectUri = "com.example.integrationauth://oauth2redirect",
        authEndpoint = "https://x.com/i/oauth2/authorize",
        tokenEndpoint = "https://api.x.com/2/oauth2/token",
        scopes = listOf(
            "tweet.read",
            "users.read",
            "offline.access" // needed for refresh_token
        )
    )

    val linkedin = OAuthCfg(
        group = ProviderGroup.Social,
        clientId = BuildConfig.LINKEDIN_CLIENT_ID,
        redirectUri = "http://127.0.0.1/callback",
        authEndpoint = "https://www.linkedin.com/oauth/native-pkce/authorization",
        tokenEndpoint = "https://www.linkedin.com/oauth/v2/accessToken",
        scopes = listOf("r_liteprofile")
    )

    val facebook = OAuthCfg(
        group = ProviderGroup.Social,
        clientId = BuildConfig.FACEBOOK_CLIENT_ID,
        redirectUri = BuildConfig.FACEBOOK_REDIRECT_URI,
        authEndpoint  = "https://www.facebook.com/${BuildConfig.FACEBOOK_API_VERSION}/dialog/oauth",
        tokenEndpoint = "https://graph.facebook.com/${BuildConfig.FACEBOOK_API_VERSION}/oauth/access_token",
        scopes = listOf("public_profile", "email")
    )

    val instagram = OAuthCfg(
        group = ProviderGroup.Social,
        clientId = BuildConfig.FACEBOOK_CLIENT_ID,
        redirectUri = BuildConfig.FACEBOOK_REDIRECT_URI,
        authEndpoint  = "https://www.facebook.com/${BuildConfig.FACEBOOK_API_VERSION}/dialog/oauth",
        tokenEndpoint = "https://graph.facebook.com/${BuildConfig.FACEBOOK_API_VERSION}/oauth/access_token",
        scopes = listOf("instagram_basic", "pages_show_list")
    )

    val snapchat = OAuthCfg(
        group = ProviderGroup.Social,
        clientId = BuildConfig.SNAPCHAT_CLIENT_ID,
        redirectUri = BuildConfig.SNAPCHAT_REDIRECT_URI, // HTTPS app link
        authEndpoint  = "https://accounts.snapchat.com/login/oauth2/authorize",
        tokenEndpoint = "https://accounts.snapchat.com/login/oauth2/token",
        scopes = listOf("user.display_name", "user.bitmoji.avatar")
    )

    val tiktok = OAuthCfg(
        group = ProviderGroup.Social,
        clientId = BuildConfig.TIKTOK_CLIENT_KEY,
        redirectUri = BuildConfig.TIKTOK_REDIRECT_URI,
        authEndpoint  = "https://www.tiktok.com/v2/auth/authorize/",
        tokenEndpoint = "https://open.tiktokapis.com/v2/oauth/token/",
        scopes = listOf("user.info.basic"),
        extraParams = mapOf("client_key" to BuildConfig.TIKTOK_CLIENT_KEY)
    )


    val demo = OAuthCfg(
        group = ProviderGroup.Social,
        clientId = "", // intentionally blank; demo path triggers when blank
        redirectUri = "com.example.integrationauth://oauth2redirect",
        authEndpoint = "https://example.com/oauth/authorize",
        tokenEndpoint = "https://example.com/oauth/token",
        scopes = listOf("basic")
    )
}
