package com.ifmo.balda.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.ifmo.balda.R

private typealias PlayerStat = Pair<String, Int> // Pair of name and score

class StatListAdapter(context: Context, private val data: List<PlayerStat>) : BaseAdapter() {
  private val layoutInflater: LayoutInflater =
    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

  override fun getCount(): Int = data.size

  override fun getItem(position: Int): PlayerStat = data[position]

  override fun getItemId(position: Int): Long = position.toLong()

  override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
    val view = convertView ?: layoutInflater.inflate(R.layout.layout_stat_list_row, parent, false)
    val (name, score) = getItem(position)
    view.findViewById<TextView>(R.id.name).text = name
    view.findViewById<TextView>(R.id.score).text = score.toString()

    return view
  }
}
