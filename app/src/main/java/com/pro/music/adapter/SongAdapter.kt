package com.pro.music.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pro.music.R
import com.pro.music.adapter.SongAdapter.SongViewHolder
import com.pro.music.constant.GlobalFunction.isFavoriteSong
import com.pro.music.databinding.ItemSongBinding
import com.pro.music.listener.IOnClickSongItemListener
import com.pro.music.model.Song
import com.pro.music.utils.GlideUtils.loadUrl

class SongAdapter(
        private val mListSongs: List<Song>?,
        private val iOnClickSongItemListener: IOnClickSongItemListener
        ) : RecyclerView.Adapter<SongViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val itemSongBinding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(itemSongBinding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = mListSongs!![position]
        loadUrl(song.image, holder.mItemSongBinding.imgSong)
        holder.mItemSongBinding.tvSongName.text = song.title
        holder.mItemSongBinding.tvArtist.text = song.artist
        holder.mItemSongBinding.tvCountListen.text = song.count.toString()
        val isFavorite = isFavoriteSong(song)
        if (isFavorite) {
            holder.mItemSongBinding.imgFavorite.setImageResource(R.drawable.ic_favorite)
        } else {
            holder.mItemSongBinding.imgFavorite.setImageResource(R.drawable.ic_unfavorite)
        }
        holder.mItemSongBinding.imgFavorite.setOnClickListener { iOnClickSongItemListener.onClickFavoriteSong(song, !isFavorite) }
        holder.mItemSongBinding.imgMoreOption.setOnClickListener { iOnClickSongItemListener.onClickMoreOptions(song) }
        holder.mItemSongBinding.layoutSongInfo.setOnClickListener { iOnClickSongItemListener.onClickItemSong(song) }
    }

    override fun getItemCount(): Int {
        return mListSongs?.size ?: 0
    }

    class SongViewHolder(
            val mItemSongBinding: ItemSongBinding
            ) : RecyclerView.ViewHolder(mItemSongBinding.root)
}