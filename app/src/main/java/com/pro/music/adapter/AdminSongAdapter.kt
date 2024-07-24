package com.pro.music.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pro.music.adapter.AdminSongAdapter.AdminSongViewHolder
import com.pro.music.databinding.ItemAdminSongBinding
import com.pro.music.listener.IOnAdminManagerSongListener
import com.pro.music.model.Song
import com.pro.music.utils.GlideUtils.loadUrl

class AdminSongAdapter(
        private val mListSongs: List<Song>?,
        private val mListener: IOnAdminManagerSongListener
        ) : RecyclerView.Adapter<AdminSongViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminSongViewHolder {
        val itemAdminSongBinding = ItemAdminSongBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return AdminSongViewHolder(itemAdminSongBinding)
    }

    override fun onBindViewHolder(holder: AdminSongViewHolder, position: Int) {
        val song = mListSongs!![position]
        loadUrl(song.image, holder.mItemAdminSongBinding.imgSong)
        holder.mItemAdminSongBinding.tvName.text = song.title
        holder.mItemAdminSongBinding.tvCategory.text = song.category
        holder.mItemAdminSongBinding.tvArtist.text = song.artist
        if (song.isFeatured == true) {
            holder.mItemAdminSongBinding.tvFeatured.text = "Yes"
        } else {
            holder.mItemAdminSongBinding.tvFeatured.text = "No"
        }
        holder.mItemAdminSongBinding.imgEdit.setOnClickListener { mListener.onClickUpdateSong(song) }
        holder.mItemAdminSongBinding.imgDelete.setOnClickListener { mListener.onClickDeleteSong(song) }
        holder.mItemAdminSongBinding.layoutItem.setOnClickListener { mListener.onClickDetailSong(song) }
    }

    override fun getItemCount(): Int {
        return mListSongs?.size ?: 0
    }

    class AdminSongViewHolder(
            val mItemAdminSongBinding: ItemAdminSongBinding
            ) : RecyclerView.ViewHolder(mItemAdminSongBinding.root)
}