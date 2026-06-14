package com.example.triplog.ui.edit

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.triplog.R
import com.example.triplog.data.TripPhoto

class PhotoAdapter(
    private val list: MutableList<TripPhoto>,
    private val onThumbnailSelected: (TripPhoto) -> Unit,
    private val onPhotoDeleted: (TripPhoto) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.ViewHolder>() {

    inner class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val ivPhoto: ImageView = view.findViewById(R.id.ivPhoto)
        val cbThumbnail: CheckBox = view.findViewById(R.id.cbThumbnail)
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val photo = list[position]
        try {
            holder.ivPhoto.setImageURI(Uri.parse(photo.photoUri))
        } catch (e: Exception) {
            holder.ivPhoto.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.cbThumbnail.isChecked = photo.isThumbnail
        holder.cbThumbnail.setOnClickListener {
            onThumbnailSelected(photo)
        }

        holder.btnDelete.setOnClickListener {
            onPhotoDeleted(photo)
        }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: MutableList<TripPhoto>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    fun updateThumbnail(selectedPhoto: TripPhoto) {
        list.forEachIndexed { index, photo ->
            list[index] = photo.copy(isThumbnail = photo.id == selectedPhoto.id)
        }
        notifyDataSetChanged()
    }
}