package com.pro.music.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pro.music.adapter.ArtistHorizontalAdapter.ArtistHorizontalViewHolder
import com.pro.music.databinding.ItemArtistHorizontalBinding
import com.pro.music.listener.IOnClickArtistItemListener
import com.pro.music.model.Artist
import com.pro.music.utils.GlideUtils.loadUrl

class ArtistHorizontalAdapter(
        private val mListArtist: List<Artist>?,
        private val iOnClickArtistItemListener: IOnClickArtistItemListener
        ) : RecyclerView.Adapter<ArtistHorizontalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistHorizontalViewHolder {
        val itemArtistHorizontalBinding = ItemArtistHorizontalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtistHorizontalViewHolder(itemArtistHorizontalBinding)
    }

    override fun onBindViewHolder(holder: ArtistHorizontalViewHolder, position: Int) {
        val artist = mListArtist!![position]
        loadUrl(artist.image, holder.mItemArtistHorizontalBinding.imgArtist)
        holder.mItemArtistHorizontalBinding.tvArtist.text = artist.name
        holder.mItemArtistHorizontalBinding.layoutItem.setOnClickListener { iOnClickArtistItemListener.onClickItemArtist(artist) }
    }

    override fun getItemCount(): Int {
        return mListArtist?.size ?: 0
    }

    class ArtistHorizontalViewHolder(
            val mItemArtistHorizontalBinding: ItemArtistHorizontalBinding
            ) : RecyclerView.ViewHolder(mItemArtistHorizontalBinding.root)
}