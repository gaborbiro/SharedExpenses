package com.gaborbiro.sharedexpenses.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatCheckBox
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.util.SparseIntArray
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import com.gaborbiro.sharedexpenses.R
import com.gaborbiro.sharedexpenses.util.Lce
import com.gaborbiro.sharedexpenses.util.TextWatcherAdapter
import com.gaborbiro.sharedexpenses.util.hide
import com.gaborbiro.sharedexpenses.util.show
import com.google.common.collect.ImmutableList
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.activity_skyrim.*
import kotlinx.android.synthetic.main.content_skyrim.*


class SkyrimActivity : AppCompatActivity() {

    companion object {
        @JvmStatic
        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, SkyrimActivity::class.java))
        }
    }

    private val adapter: SkyrimAdapter = SkyrimAdapter()

    private var subscription: Disposable? = null
    private var currentThings: List<String>? = null
    private var currentSize: Int? = null
    private var lastProgress: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skyrim)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        things_input.addTextChangedListener(object : TextWatcherAdapter() {
            override fun afterTextChanged(editable: Editable?) {
                generate()
            }
        })
        size_input.addTextChangedListener(object : TextWatcherAdapter() {
            override fun afterTextChanged(editable: Editable?) {
                generate()
            }
        })
        list.adapter = adapter
    }

    private fun generate() {
        val things = things_input.text.toString()
        val size = size_input.text.toString()

        if (things.isNotEmpty() && size.isNotEmpty()) {
            val thingsArray = things.split("[,;\\s]+".toRegex()).map { it.toLowerCase().capitalize().trim() }.filter { it.isNotBlank() }
            val sizeInt = size.toInt()

            if (currentThings?.equals(thingsArray) == false || currentSize != sizeInt) {
                subscription?.dispose()
                currentThings = thingsArray
                currentSize = sizeInt

                subscription = Observable.create<Lce<Array<Permutation>>> { emitter ->
                    emitter.onNext(Lce.loading(0))
                    val collector = PublishSubject.create<Candidate>()
                    val result = mutableListOf<Permutation>()
                    Pair(currentThings, currentSize).notNull { things, size ->
                        val target = Math.pow(things.size.toDouble(), size.toDouble()).toInt()
                        collector
                                .doOnComplete {
                                    result.sortBy { it.duplicateCount }
                                    emitter.onNext(Lce.content(result.toTypedArray()))
                                    subscription = null
                                }
                                .subscribe {
                                    val data: ImmutableList<String> = ImmutableList.copyOf(it.data.map { things[it] })
                                    result.add(Permutation(false, data, it.longestRepeat()))

                                    ((result.size / target.toDouble()) * 100).toInt().let {
                                        if (it != lastProgress) {
                                            lastProgress = it
                                            emitter.onNext(Lce.loading(it))
                                        }
                                    }
                                }
                        permutate(Candidate(Array(size, { -1 })) , things.size, 0, collector)
                        collector.onComplete()
                    }
                }
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            if (it.loading) {
                                progress.show()
                                list.hide()
                                progress.text = "${it.progress}%"
                            } else {
                                progress.hide()
                                list.show()
                                adapter.data = it.content?.toList()
                            }
                        }
            }
        }
    }

    class Candidate(val data: Array<Int>) {

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
            for (i in 0 until occurrence.size()) {
                if (occurrence[i] > max) {
                    max = occurrence[i]
                }
            }
            return max
        }
    }

    private fun permutate(c: Candidate, count: Int, index: Int, emitter: Subject<Candidate>) {
        while (c.data[index] < count - 1) {
            c.inc(index)
            if (index < c.data.size - 1) {
                permutate(c, count, index + 1, emitter)
            } else {
                emitter.onNext(c)
            }
        }
        c.clear(index)
    }

    private fun longestRepeat(sortedArray: Array<String>): Int {
        var current: Any? = null
        var maxRunLength = 0
        var runLength = 0

        sortedArray.forEach { item ->
            current?.let {
                if (it == item) {
                    runLength++
                    if (runLength > maxRunLength) {
                        maxRunLength = runLength
                    }
                } else {
                    runLength = 0
                }
            }
            current = item
        }
        return maxRunLength
    }

    class SkyrimAdapter : RecyclerView.Adapter<PermutationVH>() {
        var data: List<Permutation>? = null
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun onBindViewHolder(holder: PermutationVH, position: Int) {
            data?.let {
                holder.bind(position + 1, it[position],
                        CompoundButton.OnCheckedChangeListener { _, isChecked ->
                            it[position].checked = isChecked
                        })
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): PermutationVH {
            val inflater = LayoutInflater.from(parent?.context)
            val view = inflater.inflate(R.layout.list_item_skyrim, parent, false)
            return PermutationVH(view)
        }

        override fun getItemCount(): Int {
            return data?.size ?: 0
        }
    }

    class PermutationVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val container = itemView as LinearLayout
        private val index: TextView = itemView.findViewById(R.id.index) as TextView
        private val checked: CheckBox = itemView.findViewById(R.id.checked) as AppCompatCheckBox

        fun bind(position: Int, data: Permutation, checkedChangeListener: CompoundButton.OnCheckedChangeListener) {
            checked.setOnCheckedChangeListener(null)
            checked.isChecked = data.checked
            checked.setOnCheckedChangeListener(checkedChangeListener)
            index.text = "$position."
            container.removeViews(2, container.childCount - 2)
            val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.marginStart = container.context.resources.getDimensionPixelSize(R.dimen.margin_normal)

            data.things.forEachIndexed { index, item ->
                val thing = TextView(container.context)
                thing.gravity = Gravity.CENTER_HORIZONTAL
                thing.id = index
                thing.text = item
                container.addView(thing, layoutParams)
            }
        }
    }

    class Permutation(var checked: Boolean, val things: ImmutableList<String>, val duplicateCount: Int) {
        override fun toString(): String {
            return "$checked, $things"
        }
    }

    fun Pair<List<String>?, Int?>.notNull(action: (List<String>, Int) -> Unit) {
        if (this.first != null && this.second != null)
            action(this.first!!, this.second!!)
    }
}