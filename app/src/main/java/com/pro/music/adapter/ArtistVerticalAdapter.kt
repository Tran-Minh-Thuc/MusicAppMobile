package com.pro.music.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pro.music.adapter.ArtistVerticalAdapter.ArtistVerticalViewHolder
import com.pro.music.databinding.ItemArtistVerticalBinding
import com.pro.music.listener.IOnClickArtistItemListener
import com.pro.music.model.Artist
import com.pro.music.utils.GlideUtils.loadUrl

class ArtistVerticalAdapter(
        private val mListArtist: List<Artist>?,
        private val iOnClickArtistItemListener: IOnClickArtistItemListener
        ) : RecyclerView.Adapter<ArtistVerticalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistVerticalViewHolder {
        val itemArtistVerticalBinding = ItemArtistVerticalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtistVerticalViewHolder(itemArtistVerticalBinding)
    }

    override fun onBindViewHolder(holder: ArtistVerticalViewHolder, position: Int) {
        val artist = mListArtist!![position]
        loadUrl(artist.image, holder.mItemArtistVerticalBinding.imgArtist)
        holder.mItemArtistVerticalBinding.tvArtist.text = artist.name
        holder.mItemArtistVerticalBinding.layoutItem.setOnClickListener { iOnClickArtistItemListener.onClickItemArtist(artist) }
    }

    override fun getItemCount(): Int {
        return mListArtist?.size ?: 0
    }

    class ArtistVerticalViewHolder(
            val mItemArtistVerticalBinding: ItemArtistVerticalBinding
            ) : RecyclerView.ViewHolder(mItemArtistVerticalBinding.root)
}