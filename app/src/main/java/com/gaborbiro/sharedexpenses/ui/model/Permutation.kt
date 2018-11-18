package com.gaborbiro.sharedexpenses.ui.model

import com.google.common.collect.ImmutableList

class Permutation(var checked: Boolean = false, val things: ImmutableList<String>, val duplicateCount: Int) {
    override fun toString(): String {
        return "$checked, $things"
    }
}