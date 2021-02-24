package me.fernandesleite.alagou.ui.mainmap

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.fernandesleite.alagou.databinding.ListItemPoiBinding
import me.fernandesleite.alagou.persistence.Poi

class PoiAdapter(private val listener: OnClickListener) :
    ListAdapter<Poi, PoiAdapter.PoiViewHolder>(DiffCallback()) {
    interface OnClickListener {
        fun onPoiClick(poi: Poi)
        fun onPoiLongClick(poi: Poi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoiViewHolder {
        return PoiViewHolder(
            ListItemPoiBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PoiViewHolder, position: Int) {
        val poi = getItem(position)
        holder.bind(poi)
        holder.itemView.setOnClickListener {
            listener.onPoiClick(poi)
        }
        holder.itemView.setOnLongClickListener {
            listener.onPoiLongClick(poi)
            true
        }
    }


    class PoiViewHolder(private val binding: ListItemPoiBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Poi) {
            binding.poi = item
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Poi>() {
        override fun areItemsTheSame(oldItem: Poi, newItem: Poi): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Poi, newItem: Poi): Boolean {
            return oldItem == newItem
        }
    }
}