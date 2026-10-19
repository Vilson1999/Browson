package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DnsDialog(
    currentDns: String,
    customDns: String = "",
    onSelectDns: (String) -> Unit,
    onSaveCustomDns: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    val providers = listOf(
        "Google (8.8.8.8)",
        "Cloudflare (1.1.1.1)",
        "Quad9 (9.9.9.9)",
        "Custom DNS",
        "Default (System DNS)"
    )

    var isCustomSelected by remember {
        mutableStateOf(currentDns.startsWith("Custom") || !providers.contains(currentDns))
    }
    var customInput by remember {
        mutableStateOf(
            if (currentDns.startsWith("Custom: ")) currentDns.removePrefix("Custom: ") else customDns
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Secure DNS Configuration") },
        text = {
            Column {
                Text(
                    text = "Select your preferred DNS provider for encrypted and secure hostname resolution:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                providers.forEach { provider ->
                    val isSelected = if (provider == "Custom DNS") {
                        isCustomSelected
                    } else {
                        !isCustomSelected && (provider == currentDns)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (provider == "Custom DNS") {
                                    isCustomSelected = true
                                } else {
                                    isCustomSelected = false
                                    onSelectDns(provider)
                                }
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                if (provider == "Custom DNS") {
                                    isCustomSelected = true
                                } else {
                                    isCustomSelected = false
                                    onSelectDns(provider)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = provider,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (isCustomSelected) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customInput,
                        onValueChange = { customInput = it },
                        label = { Text("Custom DNS IP / DoH URL") },
                        placeholder = { Text("e.g. 94.140.14.14 or dns.google") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val trimmed = customInput.trim()
                            if (trimmed.isNotEmpty()) {
                                onSaveCustomDns(trimmed)
                                onSelectDns("Custom: $trimmed")
                            }
                        },
                        enabled = customInput.isNotBlank(),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Apply Custom DNS")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
