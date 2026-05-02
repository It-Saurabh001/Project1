package com.saurabh.sudoku.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SudokuCell(
    value: Int,
    isInitial: Boolean,
    isSelected: Boolean,
    isHighlighted: Boolean,     // same row/col/box as selected
    isSameNumber: Boolean,      // same value as selected cell
    hasConflict: Boolean,
    notes: Set<Int> = emptySet(),
    isNotesMode: Boolean = false,
    justPlaced: Boolean = false,   // triggers scale pop animation
    justMistake: Boolean = false,  // triggers shake animation
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ── Background color with smooth transition ──────────────────────────────
    val targetBg = when {
        hasConflict  -> MaterialTheme.colorScheme.errorContainer
        isSelected   -> MaterialTheme.colorScheme.primaryContainer
        isSameNumber -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        isHighlighted -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        isInitial    -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        else         -> MaterialTheme.colorScheme.surface
    }
    val animatedBg by animateColorAsState(
        targetValue = targetBg,
        animationSpec = tween(durationMillis = 160),
        label = "cellBg"
    )

    // ── Text color ────────────────────────────────────────────────────────────
    val textColor = when {
        hasConflict  -> MaterialTheme.colorScheme.onErrorContainer
        isInitial    -> MaterialTheme.colorScheme.onSurface
        else         -> MaterialTheme.colorScheme.primary
    }

    // ── Border ───────────────────────────────────────────────────────────────
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(160),
        label = "cellBorder"
    )
    val borderWidth = if (isSelected) 2.dp else 0.dp

    // ── Scale pop animation on correct placement ──────────────────────────────
    var triggerScale by remember { mutableStateOf(false) }
    LaunchedEffect(justPlaced) {
        if (justPlaced) {
            triggerScale = true
        }
    }
    val scale by animateFloatAsState(
        targetValue = if (triggerScale) 1.12f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        finishedListener = { triggerScale = false },
        label = "cellScale"
    )

    // ── Shake animation on mistake ────────────────────────────────────────────
    val shakeOffset by remember { mutableStateOf(Animatable(0f)) }
    LaunchedEffect(justMistake) {
        if (justMistake) {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 300
                    6f at 50
                    -6f at 100
                    4f at 150
                    -4f at 200
                    2f at 250
                    0f at 300
                }
            )
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale)
            .offset(x = shakeOffset.value.dp)
            .background(animatedBg, RoundedCornerShape(3.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(3.dp))
            .clickable(onClick = onClick)
            .padding(1.dp)
    ) {
        when {
            value != 0 -> {
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 20.sp,
                        color = textColor,
                        fontWeight = if (isInitial) FontWeight.Bold else FontWeight.SemiBold
                    ),
                    textAlign = TextAlign.Center
                )
            }
            notes.isNotEmpty() -> {
                NoteGrid(notes = notes, isNotesMode = isNotesMode)
            }
        }
    }
}

@Composable
private fun NoteGrid(notes: Set<Int>, isNotesMode: Boolean) {
    val noteColor = if (isNotesMode)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)

    Column(
        modifier = Modifier.fillMaxSize().padding(1.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (noteRow in 0..2) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (noteCol in 0..2) {
                    val n = noteRow * 3 + noteCol + 1
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        if (notes.contains(n)) {
                            Text(
                                text = n.toString(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = noteColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
