package com.pro.music.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pro.music.adapter.AdminArtistAdapter.AdminArtistViewHolder
import com.pro.music.databinding.ItemAdminArtistBinding
import com.pro.music.listener.IOnAdminManagerArtistListener
import com.pro.music.model.Artist
import com.pro.music.utils.GlideUtils.loadUrl

class AdminArtistAdapter(
        private val mListArtist: List<Artist>?,
        private val mListener: IOnAdminManagerArtistListener
        ) : RecyclerView.Adapter<AdminArtistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminArtistViewHolder {
        val binding = ItemAdminArtistBinding.inflate(LayoutInflater.from(parent.context),
                parent, false)
        return AdminArtistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdminArtistViewHolder, position: Int) {
        val artist = mListArtist!![position]
        loadUrl(artist.image, holder.itemBinding.imgArtist)
        holder.itemBinding.tvName.text = artist.name
        holder.itemBinding.imgEdit.setOnClickListener { mListener.onClickUpdateArtist(artist) }
        holder.itemBinding.imgDelete.setOnClickListener { mListener.onClickDeleteArtist(artist) }
        holder.itemBinding.layoutItem.setOnClickListener { mListener.onClickDetailArtist(artist) }
    }

    override fun getItemCount(): Int {
        return mListArtist?.size ?: 0
    }

    class AdminArtistViewHolder(
            val itemBinding: ItemAdminArtistBinding
            ) : RecyclerView.ViewHolder(itemBinding.root)
}