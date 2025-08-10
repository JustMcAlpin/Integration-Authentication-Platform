package com.example.integrationauthenticationplatform.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun IntegrationCard(
    title: String,
    authLabel: String,
    icon: ImageVector,
    connected: Boolean,
    requiresApproval: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = { },
                    label = { Text(authLabel) },
                    enabled = false
                )
                SuggestionChip(
                    onClick = { },
                    label = {
                        Text(if (connected) "Connected" else if (requiresApproval) "Needs approval" else "Disconnected")
                    },
                    enabled = false
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (connected) {
                    Button(onClick = onDisconnect) { Text("Disconnect") }
                } else {
                    Button(
                        onClick = onConnect,
                        enabled = !requiresApproval
                    ) { Text("Connect") }
                    if (requiresApproval) {
                        Text(
                            "Third‑party app approval required",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
