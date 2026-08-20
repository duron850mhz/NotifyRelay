package com.example.notifyrelay

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notifyrelay.databinding.ItemAppBinding

data class AppEntry(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
    var checked: Boolean
)

class AppListAdapter(
    private val items: MutableList<AppEntry>
) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun getSelectedPackages(): Set<String> =
        items.filter { it.checked }.map { it.packageName }.toSet()

    inner class ViewHolder(private val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: AppEntry) {
            binding.imageIcon.setImageDrawable(entry.icon)
            binding.textLabel.text = entry.label
            binding.textPackage.text = entry.packageName

            binding.checkboxSelected.setOnCheckedChangeListener(null)
            binding.checkboxSelected.isChecked = entry.checked
            binding.checkboxSelected.setOnCheckedChangeListener { _, checked ->
                entry.checked = checked
            }

            binding.root.setOnClickListener {
                binding.checkboxSelected.isChecked = !binding.checkboxSelected.isChecked
            }
        }
    }
}
