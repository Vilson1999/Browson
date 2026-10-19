package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.dns.DnsTestResult

@Composable
fun DnsDialog(
    currentDns: String,
    customDns: String = "",
    dnsTestResult: DnsTestResult? = null,
    onTestDns: () -> Unit = {},
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
                "Secure DNS Configuration",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column {
                Text(
                    text = "Encrypted DNS-over-HTTPS resolves domain names securely and bypasses ISP tampering:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Live DNS Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (dnsTestResult?.isSuccess == true) {
                            Color(0xFF10B981).copy(alpha = 0.12f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (dnsTestResult?.isSuccess == true) Color(0xFF10B981).copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (dnsTestResult?.isSuccess == true) Icons.Default.CheckCircle else Icons.Default.Speed,
                                contentDescription = null,
                                tint = if (dnsTestResult?.isSuccess == true) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (dnsTestResult?.isSuccess == true) "DNS Active & Working" else "Active: $currentDns",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (dnsTestResult?.isSuccess == true) Color(0xFF047857) else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (dnsTestResult != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Latency: ${dnsTestResult.latencyMs} ms • IP: ${dnsTestResult.resolvedIp}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onTestDns,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test DNS Connection")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Provider List
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
                            .padding(vertical = 4.dp),
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
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }

                AnimatedVisibility(visible = isCustomSelected) {
                    Column {
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
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
