package com.gaborbiro.sharedexpenses.util

fun Pair<List<String>?, Int?>.notNull(action: (List<String>, Int) -> Unit) {
    if (this.first != null && this.second != null)
        action(this.first!!, this.second!!)
}