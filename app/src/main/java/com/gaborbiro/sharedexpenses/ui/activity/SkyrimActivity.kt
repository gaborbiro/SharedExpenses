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
import com.gaborbiro.sharedexpenses.util.notNull
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
                onInputUpdated()
            }
        })
        size_input.addTextChangedListener(object : TextWatcherAdapter() {
            override fun afterTextChanged(editable: Editable?) {
                onInputUpdated()
            }
        })
        list.adapter = adapter
    }

    private fun onInputUpdated() {
        val things = things_input.text.toString()
        val size = size_input.text.toString()

        if (things.isNotEmpty() && size.isNotEmpty()) {
            val thingsArray = things.split("[,;\\s]+".toRegex()).map { it.toLowerCase().capitalize().trim() }.filter { it.isNotBlank() }
            val sizeInt = size.toInt()

            if (currentThings?.equals(thingsArray) == false || currentSize != sizeInt) {
                subscription?.dispose()
                currentThings = thingsArray
                currentSize = sizeInt
                subscription = generate(thingsArray, sizeInt)
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

    private fun generate(things: List<String>, size: Int): Observable<Lce<Array<Permutation>>> {
        return Observable.create<Lce<Array<Permutation>>> { emitter ->
            emitter.onNext(Lce.loading(0))
            val collector = PublishSubject.create<Candidate>()
            val result = mutableListOf<Permutation>()
            Pair(things, size).notNull { things, size ->
                val target = Math.pow(things.size.toDouble(), size.toDouble()).toInt()
                collector
                        .doOnComplete {
                            result.sortBy { it.duplicateCount }
                            emitter.onNext(Lce.content(result.toTypedArray()))
                            subscription = null
                        }
                        .subscribe {
                            val data: ImmutableList<String> = ImmutableList.copyOf(it.data.map { things[it] })
                            result.add(Permutation(things = data, duplicateCount = it.longestRepeat()))

                            ((result.size / target.toDouble()) * 100).toInt().let {
                                if (it != lastProgress) {
                                    lastProgress = it
                                    emitter.onNext(Lce.loading(it))
                                }
                            }
                        }
                permutate(Candidate(size), things.size, collector)
                collector.onComplete()
            }
        }
    }

    private fun permutate(candidate: Candidate, count: Int, emitter: Subject<Candidate>, index: Int = 0) {
        while (candidate.data[index] < count - 1) {
            candidate.inc(index)
            if (index < candidate.data.size - 1) {
                permutate(candidate, count, emitter, index + 1)
            } else {
                emitter.onNext(candidate)
            }
        }
        candidate.clear(index)
    }

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
        private val index: TextView = itemView.findViewById<TextView>(R.id.index)
        private val checked: CheckBox = itemView.findViewById<AppCompatCheckBox>(R.id.checked)

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

    class Permutation(var checked: Boolean = false, val things: ImmutableList<String>, val duplicateCount: Int) {
        override fun toString(): String {
            return "$checked, $things"
        }
    }
}