package dev.naijun.dununlocker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import dev.naijun.dununlocker.R

@Composable
fun ShizukuStatusCard(
    isGranted: Boolean,
    isRunning: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    val cardColor = when {
        isGranted -> MaterialTheme.colorScheme.primaryContainer
        isRunning -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.errorContainer
    }

    val contentColor = when {
        isGranted -> MaterialTheme.colorScheme.onPrimaryContainer
        isRunning -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onErrorContainer
    }

    val icon = when {
        isGranted -> Icons.Filled.CheckCircle
        isRunning -> Icons.Filled.Info
        else -> Icons.Filled.Warning
    }

    val statusText = when {
        isGranted -> stringResource(R.string.shizuku_granted_message)
        isRunning -> stringResource(R.string.shizuku_running_message)
        else -> stringResource(R.string.shizuku_not_running_message)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(R.string.shizuku_status_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )

            if (isRunning && !isGranted) {
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Filled.Security, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.shizuku_request_button))
                }
            } else if (!isRunning) {
                FilledTonalButton(
                    onClick = { uriHandler.openUri("https://github.com/RikkaApps/Shizuku/releases") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.shizuku_install_button))
                }
            }
        }
    }
}
