package com.gaborbiro.sharedexpenses.ui.adapter

import android.support.v7.widget.AppCompatCheckBox
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import com.gaborbiro.sharedexpenses.R
import com.gaborbiro.sharedexpenses.ui.model.Permutation

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermutationVH {
        val inflater = LayoutInflater.from(parent.context)
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