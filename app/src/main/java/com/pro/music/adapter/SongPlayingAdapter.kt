package com.pro.music.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pro.music.R
import com.pro.music.adapter.SongPlayingAdapter.SongPlayingViewHolder
import com.pro.music.databinding.ItemSongPlayingBinding
import com.pro.music.listener.IOnClickSongPlayingItemListener
import com.pro.music.model.Song
import com.pro.music.utils.GlideUtils.loadUrl

class SongPlayingAdapter(
        private val mListSongs: List<Song>?,
        private val iOnClickSongPlayingItemListener: IOnClickSongPlayingItemListener
        ) : RecyclerView.Adapter<SongPlayingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongPlayingViewHolder {
        val itemSongPlayingBinding = ItemSongPlayingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongPlayingViewHolder(itemSongPlayingBinding)
    }

    override fun onBindViewHolder(holder: SongPlayingViewHolder, position: Int) {
        val song = mListSongs!![position]
        if (song.isPlaying) {
            holder.mItemSongPlayingBinding.layoutItem.setBackgroundResource(R.color.background_bottom)
            holder.mItemSongPlayingBinding.imgAction.setImageResource(R.drawable.ic_play_black)
            holder.mItemSongPlayingBinding.imgAction.setOnClickListener(null)
        } else {
            holder.mItemSongPlayingBinding.layoutItem.setBackgroundResource(R.color.white)
            holder.mItemSongPlayingBinding.imgAction.setImageResource(R.drawable.ic_delete_black)
            holder.mItemSongPlayingBinding.imgAction.setOnClickListener { iOnClickSongPlayingItemListener.onClickRemoveFromPlaylist(holder.adapterPosition) }
        }
        if (song.isPriority) {
            holder.mItemSongPlayingBinding.tvPrioritized.visibility = View.VISIBLE
        } else {
            holder.mItemSongPlayingBinding.tvPrioritized.visibility = View.GONE
        }
        loadUrl(song.image, holder.mItemSongPlayingBinding.imgSong)
        holder.mItemSongPlayingBinding.tvSongName.text = song.title
        holder.mItemSongPlayingBinding.tvArtist.text = song.artist
        holder.mItemSongPlayingBinding.layoutSong.setOnClickListener { iOnClickSongPlayingItemListener.onClickItemSongPlaying(holder.adapterPosition) }
    }

    override fun getItemCount(): Int {
        return mListSongs?.size ?: 0
    }

    class SongPlayingViewHolder(
            val mItemSongPlayingBinding: ItemSongPlayingBinding
            ) : RecyclerView.ViewHolder(mItemSongPlayingBinding.root)
}