package com.example.triplog.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.triplog.data.TripRecord
import com.example.triplog.databinding.RecordCardBinding

class TripAdapter(
    private val list: MutableList<TripRecord>
) : RecyclerView.Adapter<TripAdapter.ViewHolder>() {

    var onItemClick: ((TripRecord) -> Unit)? = null
    var onItemLongClick: ((TripRecord) -> Unit)? = null

    inner class ViewHolder(val binding: RecordCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(record: TripRecord) {
            binding.tvPlace.text = record.place
            binding.tvDate.text = record.visitDate
            binding.root.setOnClickListener {
                onItemClick?.invoke(record)
            }
            binding.root.setOnLongClickListener {
                onItemLongClick?.invoke(record)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecordCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size

    fun updateList(newList: MutableList<TripRecord>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}