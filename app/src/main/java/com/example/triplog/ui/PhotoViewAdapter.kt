package com.example.triplog.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.triplog.R
import com.example.triplog.data.TripPhoto

class PhotoViewAdapter(
    private val list: MutableList<TripPhoto>
) : RecyclerView.Adapter<PhotoViewAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPhoto: ImageView = view.findViewById(R.id.ivPhotoView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val photo = list[position]
        try {
            holder.ivPhoto.setImageURI(Uri.parse(photo.photoUri))
        } catch (e: Exception) {
            holder.ivPhoto.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: MutableList<TripPhoto>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}