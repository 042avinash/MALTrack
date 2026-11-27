package com.maltrack.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var description by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Send Feedback") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Share feedback or report a problem. Your draft will open as a GitHub issue for you to review and submit.",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "GitHub issues are public. Sign in to GitHub to submit, and avoid including private information. You can attach screenshots directly on the GitHub page.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description of Issue / Feedback") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )

            Button(
                onClick = {
                    val body = """### Feedback or issue
$description

### App version
${com.maltrack.app.BuildConfig.VERSION_NAME}
""".trimIndent()
                    val issueUri = Uri.parse("https://github.com/042avinash/MALTrack/issues/new")
                        .buildUpon()
                        .appendQueryParameter("title", "Feedback from MALTrack Android app")
                        .appendQueryParameter("body", body)
                        .build()
                    context.startActivity(Intent(Intent.ACTION_VIEW, issueUri))
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = description.isNotBlank()
            ) {
                Text("Continue on GitHub")
            }
        }
    }
}
