package com.example.myapplication.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.data.remote.JikanReviewData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllReviewsScreen(
    viewModel: AllReviewsViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Reviews") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is AllReviewsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is AllReviewsUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is AllReviewsUiState.Success -> {
                    if (state.reviews.isEmpty()) {
                        Text(
                            text = "No reviews available.",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.reviews) { review ->
                                ReviewItemCard(review = review)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewItemCard(review: JikanReviewData) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp).animateContentSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = review.user.images.jpg?.image_url,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(review.user.username, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(review.date.take(10), style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val tag = review.tags.firstOrNull() ?: ""
                    if (tag.isNotEmpty()) {
                        val tagColor = when {
                            tag.contains("Not", ignoreCase = true) -> Color.Red
                            tag.contains("Mixed", ignoreCase = true) -> Color.Gray
                            else -> Color(0xFF4CAF50) // Green
                        }
                        Text(
                            text = tag, 
                            color = tagColor, 
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    Icon(Icons.Default.Star, contentDescription = "Score", tint = Color.Yellow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${review.score}", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (review.is_spoiler && !expanded) "[Contains Spoilers] Click to read" else review.review,
                style = MaterialTheme.typography.bodySmall,
                maxLines = if (expanded) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (expanded) "Show Less" else "Read More",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(top = 4.dp)
            )
        }
    }
}
