package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.data.download.DetectedVideoStream

@Composable
fun VideoGrabberDialog(
    streams: List<DetectedVideoStream>,
    onOpenStream: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detected video streams") },
        text = {
            if (streams.isEmpty()) {
                Text("No HLS/DASH stream was detected on this page yet.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    streams.take(8).forEach { stream ->
                        TextButton(onClick = { onOpenStream(stream.sourceUrl) }) {
                            Text(stream.displayLabel)
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
