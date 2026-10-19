package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DnsDialog(
    isSecureDnsEnabled: Boolean,
    currentDns: String,
    customDns: String = "",
    onToggleSecureDns: (Boolean) -> Unit,
    onSelectDns: (String) -> Unit,
    onSaveCustomDns: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    val providers = listOf(
        "Cloudflare (1.1.1.1)" to "1.1.1.1 / 1.0.0.1 (Fast & Private)",
        "Google (8.8.8.8)" to "8.8.8.8 / 8.8.4.4",
        "Quad9 (9.9.9.9)" to "9.9.9.9 (Malware blocking)",
        "AdGuard DNS" to "dns.adguard-dns.com (Ad-blocking DNS)",
        "Custom DNS" to "Custom DoH URL or IP"
    )

    var isCustomSelected by remember(currentDns) {
        mutableStateOf(currentDns.startsWith("Custom") || !providers.any { it.first == currentDns })
    }
    var customInput by remember(customDns, currentDns) {
        mutableStateOf(
            if (currentDns.startsWith("Custom: ")) currentDns.removePrefix("Custom: ") else customDns
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Dns,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                "Secure DNS Provider",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column {
                // Secure DNS ON / OFF Toggle
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSecureDnsEnabled) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Secure DNS",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isSecureDnsEnabled) "Encrypted DNS-over-HTTPS active" else "Turned off (Using system DNS)",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSecureDnsEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isSecureDnsEnabled,
                            onCheckedChange = { onToggleSecureDns(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Move all DNS options INSIDE this option (visible when toggle is ON)
                AnimatedVisibility(visible = isSecureDnsEnabled) {
                    Column {
                        Text(
                            text = "Choose Secure DNS provider:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        providers.forEach { (name, desc) ->
                            val isSelected = if (name == "Custom DNS") {
                                isCustomSelected
                            } else {
                                !isCustomSelected && currentDns == name
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (name == "Custom DNS") {
                                            isCustomSelected = true
                                        } else {
                                            isCustomSelected = false
                                            onSelectDns(name)
                                        }
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        if (name == "Custom DNS") {
                                            isCustomSelected = true
                                        } else {
                                            isCustomSelected = false
                                            onSelectDns(name)
                                        }
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (isCustomSelected) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = customInput,
                                onValueChange = {
                                    customInput = it
                                    onSaveCustomDns(it)
                                    if (it.isNotBlank()) onSelectDns("Custom: $it")
                                },
                                label = { Text("Custom DNS IP or DoH URL") },
                                placeholder = { Text("e.g. 1.1.1.1 or https://...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
