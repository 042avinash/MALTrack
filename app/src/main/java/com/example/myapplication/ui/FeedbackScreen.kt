package com.example.myapplication.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
    var email by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var screenshotUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                screenshotUri = uri
            }
        }
    )

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
                text = "We'd love to hear your feedback or any issues you've encountered!",
                style = MaterialTheme.typography.bodyLarge
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Your Email (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description of Issue / Feedback") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                ) {
                    Text(if (screenshotUri == null) "Attach Screenshot" else "Screenshot Attached ✓")
                }
                
                if (screenshotUri != null) {
                    TextButton(onClick = { screenshotUri = null }) {
                        Text("Remove")
                    }
                }
            }

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "message/rfc822"
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("042avinash@gmail.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "MALTrack Feedback")
                        val bodyText = if (email.isNotBlank()) "User Email: ${email}\n\nIssue Description:\n${description}" else "Issue Description:\n${description}"
                        putExtra(Intent.EXTRA_TEXT, bodyText)
                        screenshotUri?.let { uri ->
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    }
                    context.startActivity(Intent.createChooser(intent, "Send Feedback via..."))
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = description.isNotBlank()
            ) {
                Text("Send via Email Client")
            }
        }
    }
}
