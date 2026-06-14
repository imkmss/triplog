package com.example.triplog.ui.home

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.triplog.data.TripRecord
import com.example.triplog.databinding.RecordCardBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

            if (record.photoUri.isNotEmpty()) {
                binding.progressBar.visibility = View.VISIBLE
                binding.ivThumbnail.visibility = View.INVISIBLE

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val uri = Uri.parse(record.photoUri)
                        withContext(Dispatchers.Main) {
                            try {
                                binding.ivThumbnail.setImageURI(uri)
                            } catch (e: Exception) {
                                binding.ivThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
                            } finally {
                                binding.progressBar.visibility = View.GONE
                                binding.ivThumbnail.visibility = View.VISIBLE
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            binding.ivThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
                            binding.progressBar.visibility = View.GONE
                            binding.ivThumbnail.visibility = View.VISIBLE
                        }
                    }
                }
            } else {
                binding.progressBar.visibility = View.GONE
                binding.ivThumbnail.visibility = View.VISIBLE
                binding.ivThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
            }

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