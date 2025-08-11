package com.example.integrationauthenticationplatform.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

enum class AuthType { OAuth, ApiKey }
enum class ProviderGroup { Google, Microsoft, ApiKeyOnly, Social }

data class ServiceDef(
    val id: String,
    val displayName: String,
    val authType: AuthType,
    val group: ProviderGroup,
    val icon: ImageVector,
    val requiresApproval: Boolean = false
)

val SERVICES: List<ServiceDef> = listOf(
    // Google family
    ServiceDef("google_calendar","Google Calendar", AuthType.OAuth, ProviderGroup.Google, Icons.Filled.Event),
    ServiceDef("google_drive","Google Drive", AuthType.OAuth, ProviderGroup.Google, Icons.Filled.Folder),
    ServiceDef("google_sheets","Google Sheets", AuthType.OAuth, ProviderGroup.Google, Icons.Filled.InsertDriveFile),
    ServiceDef("gmail","Gmail", AuthType.OAuth, ProviderGroup.Google, Icons.Filled.Email),
    ServiceDef("youtube","YouTube", AuthType.OAuth, ProviderGroup.Google, Icons.Filled.VideoLibrary),

    // Microsoft family
    ServiceDef("ms_calendar","Office 365 Calendar", AuthType.OAuth, ProviderGroup.Microsoft, Icons.Filled.Event),
    ServiceDef("outlook","Office 365 Mail (Outlook)", AuthType.OAuth, ProviderGroup.Microsoft, Icons.Filled.Email),
    ServiceDef("onedrive","OneDrive", AuthType.OAuth, ProviderGroup.Microsoft, Icons.Filled.Cloud),

    // API-key services
    ServiceDef("sendgrid","SendGrid", AuthType.ApiKey, ProviderGroup.ApiKeyOnly, Icons.Filled.Link),
    ServiceDef("twilio","Twilio", AuthType.ApiKey, ProviderGroup.ApiKeyOnly, Icons.Filled.Link),

    // Socials (stubbed; need app approval)
    ServiceDef("instagram","Instagram (Meta)", AuthType.OAuth, ProviderGroup.Social, Icons.Filled.Link, requiresApproval = true),
    ServiceDef("tiktok","TikTok", AuthType.OAuth, ProviderGroup.Social, Icons.Filled.Link, requiresApproval = true),
    ServiceDef("x","X (Twitter)", AuthType.OAuth, ProviderGroup.Social, Icons.Filled.Link, requiresApproval = true),
    ServiceDef("facebook","Facebook", AuthType.OAuth, ProviderGroup.Social, Icons.Filled.Link, requiresApproval = true),
    ServiceDef("linkedin","LinkedIn", AuthType.OAuth, ProviderGroup.Social, Icons.Filled.Link, requiresApproval = true),
    ServiceDef("snapchat","Snapchat", AuthType.OAuth, ProviderGroup.Social, Icons.Filled.Link, requiresApproval = true)
)
