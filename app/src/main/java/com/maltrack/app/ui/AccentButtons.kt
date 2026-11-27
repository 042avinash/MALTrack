package com.maltrack.app.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AccentTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ButtonColors? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
    content: @Composable RowScope.() -> Unit
) {
    androidx.compose.material3.TextButton(
        onClick = onClick,
        modifier = modifier,
        colors = colors ?: ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary),
        contentPadding = contentPadding,
        content = content
    )
}
