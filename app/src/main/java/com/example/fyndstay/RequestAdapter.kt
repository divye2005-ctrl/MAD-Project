package com.example.fyndstay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RequestAdapter(private val items: List<RequestItem>) : RecyclerView.Adapter<RequestAdapter.VH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_request, parent, false))
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.student.text = item.studentName
        holder.pg.text = "Requested to view ${item.pgName}"
        holder.time.text = if (item.timestamp == 0L) "" else
            SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(item.timestamp))
    }
    override fun getItemCount() = items.size
    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val student: TextView = itemView.findViewById(R.id.tvRequestStudent)
        val pg: TextView = itemView.findViewById(R.id.tvRequestPGName)
        val time: TextView = itemView.findViewById(R.id.tvRequestTime)
    }
}
