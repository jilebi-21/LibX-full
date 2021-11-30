package org.freaky.lib.fragments.palette

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.freaky.lib.R

class PaletteAdapter(
    private val list: List<PaletteFragment.PaletteItem>
) : RecyclerView.Adapter<PaletteAdapter.ViewHolder>() {

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        val colorCodeView = itemView.findViewById<TextView>(R.id.color_code_view)
        val colorNameView = itemView.findViewById<TextView>(R.id.color_name_view)

        fun bind(item: PaletteFragment.PaletteItem) {
            itemView.rootView.setBackgroundColor(item.color)
            colorCodeView.text = String.format("#%06X", 0xFFFFFF and item.color)
            colorNameView.text = item.name
            colorNameView.setTextColor(item.textColor)
            colorCodeView.setTextColor(item.textColor)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.palette_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return list.size
    }

}