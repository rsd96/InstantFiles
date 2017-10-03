package com.rsd96.instantfiles

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

/**
 * Created by Ramshad on 8/23/17.
 */

class ListAdapter constructor(context: Context, list: MutableList<Any>): BaseAdapter() {

    val VIEW_HEADER = 0
    val VIEW_ITEM   = 1

    var list : MutableList<Any>
    var inflater : LayoutInflater

    init {
        this.inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        this.list = list
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View {

        return view as View
    }

    override fun getItem(position: Int): Any = list[position]

    override fun getItemId(position: Int): Long = position as Long

    override fun getCount(): Int = list.size
}