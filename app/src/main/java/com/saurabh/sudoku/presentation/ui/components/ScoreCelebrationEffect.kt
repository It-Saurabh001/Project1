package com.saurabh.sudoku.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.saurabh.sudoku.R

/**
 * Plays the trophy-confetti Lottie animation.
 *
 * @param modifier   Size/placement — caller decides the exact bounds.
 * @param iterations How many times to play. [LottieConstants.IterateForever] loops indefinitely.
 *                   Pass `1` for a single celebratory burst.
 */
@Composable
fun ScoreCelebrationEffect(
    modifier: Modifier = Modifier,
    iterations: Int = LottieConstants.IterateForever
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.trophy_confetti)
    )

    LottieAnimation(
        composition = composition,
        iterations   = iterations,
        modifier     = modifier
    )
}

@Preview(showBackground = true, widthDp = 320, heightDp = 200)
@Composable
private fun ScoreCelebrationEffectPreview() {
    ScoreCelebrationEffect()
}