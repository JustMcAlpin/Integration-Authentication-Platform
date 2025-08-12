package com.example.integrationauthenticationplatform.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.PasswordVisualTransformation

@Composable
fun ApiKeyDialog(
    serviceName: String,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
    defaultValue: String = ""     // NEW
) {
    var value by remember { mutableStateOf(defaultValue) }  // NEW

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Connect $serviceName") },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("API key") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(value) }, enabled = value.isNotBlank()) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}