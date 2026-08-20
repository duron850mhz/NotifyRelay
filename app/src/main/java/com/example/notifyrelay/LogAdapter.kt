package com.example.notifyrelay

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notifyrelay.databinding.ItemLogBinding

class LogAdapter : RecyclerView.Adapter<LogAdapter.ViewHolder>() {

    private var items: List<LogEntry> = emptyList()

    fun submitList(newItems: List<LogEntry>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(private val binding: ItemLogBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: LogEntry) {
            val statusText = if (entry.success) "OK" else "NG"
            binding.textStatus.text = "[${entry.formattedTime()}] $statusText (${entry.statusOrError})"
            binding.textStatus.setTextColor(
                if (entry.success)
                    binding.root.context.getColor(android.R.color.holo_green_dark)
                else
                    binding.root.context.getColor(android.R.color.holo_red_dark)
            )
            binding.textDetail.text = "${entry.packageName}\n${entry.title} / ${entry.body}"
        }
    }
}
