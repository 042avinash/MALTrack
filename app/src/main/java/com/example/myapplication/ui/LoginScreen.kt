package com.example.myapplication.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        
        // IMPORTANT: Ensure the maltrack_logo.png file is placed in res/drawable/ folder.
        Image(
            painter = painterResource(id = R.drawable.maltrack_logo),
            contentDescription = "MALTrack Logo",
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .heightIn(max = 120.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))

        Text(text = "MyAnimeList Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login with MyAnimeList")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Logging in will allow you to see your personal profile and statistics.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}
