package com.gaborbiro.sharedexpenses.ui.model

import android.util.SparseIntArray

class Candidate(size: Int) {
    val data = Array(size, { -1 })
    private val occurrence = SparseIntArray()

    private fun mark(index: Int) {
        occurrence.put(data[index], occurrence[data[index], 0] + 1)
    }

    private fun unmark(index: Int) {
        occurrence.put(data[index], (occurrence[data[index], 0] - 1).let { if (it >= 0) it else 0 })
    }

    fun clear(index: Int) {
        unmark(index)
        data[index] = -1
    }

    fun inc(index: Int) {
        unmark(index)
        data[index]++
        mark(index)
    }

    fun longestRepeat(): Int {
        var max = 0
        (0 until occurrence.size())
                .asSequence()
                .filter { occurrence[it] > max }
                .forEach { max = occurrence[it] }
        return max
    }
}