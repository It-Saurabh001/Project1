package com.saurabh.sudoku.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun IntArray.copy2D(): Array<IntArray> {
    return Array(9) { row ->
        IntArray(9) { col ->
            this[row * 9 + col]
        }
    }
}
