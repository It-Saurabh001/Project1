package com.saurabh.sudoku.presentation.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
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
import com.saurabh.sudoku.presentation.ui.theme.ButtonPrimary

@Composable
fun NumberButton(
    number: Int,
    count: Int,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isEnabled) ButtonPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val contentColor = if (isEnabled) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1.6f) // Adjusted for better layout with two text elements
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = number.toString(),
                color = contentColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "$count/9",
                color = contentColor.copy(alpha = if (isEnabled) 0.7f else 0.5f),
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
