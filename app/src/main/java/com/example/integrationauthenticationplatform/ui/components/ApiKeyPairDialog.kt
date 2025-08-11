package com.example.integrationauthenticationplatform.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.PasswordVisualTransformation

@Composable
fun ApiKeyPairDialog(
    serviceName: String,
    firstLabel: String,
    secondLabel: String,
    onDismiss: () -> Unit,
    onSubmit: (first: String, second: String) -> Unit
) {
    var first by remember { mutableStateOf("") }
    var second by remember { mutableStateOf("") }

    val canSave = first.isNotBlank() && second.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Connect $serviceName") },
        text = {
            Column {
                OutlinedTextField(
                    value = first, onValueChange = { first = it },
                    label = { Text(firstLabel) }, singleLine = true
                )
                OutlinedTextField(
                    value = second, onValueChange = { second = it },
                    label = { Text(secondLabel) }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(first, second) }, enabled = canSave) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
