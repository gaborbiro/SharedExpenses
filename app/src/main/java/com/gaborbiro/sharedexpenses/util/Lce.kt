package com.gaborbiro.sharedexpenses.util

open class Lce<T> {

    var progress: Int? = null
    var content: T? = null

    private constructor()

    val loading
        get() = progress != null

    private constructor(progress: Int) {
        this.progress = progress
    }

    private constructor(data: T) {
        this.content = data
    }

    companion object {
        fun <T> loading(progress: Int): Lce<T> {
            return Lce<T>(progress)
        }

        fun <T> content(content: T): Lce<T> {
            return Lce(content)
        }
    }
}
