package com.saurabh.sudoku.presentation.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NumberButton(
    number: Int,
    count: Int,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

    Box(
        modifier = modifier
            .aspectRatio(0.5f) // Changed to 1f for a square shape
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(enabled = isEnabled, onClick = onClick)
    ) {
        // Main number, centered
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                color = contentColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        // Count text, in the top-left corner
        Text(
            text = (9 - count).toString(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 4.dp, top = 2.dp),
            color = contentColor.copy(alpha = if (isEnabled) 0.8f else 0.6f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
