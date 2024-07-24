package com.pro.music.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pro.music.R
import com.pro.music.adapter.SongPopularAdapter.SongPopularViewHolder
import com.pro.music.constant.GlobalFunction.isFavoriteSong
import com.pro.music.databinding.ItemSongPopularBinding
import com.pro.music.listener.IOnClickSongItemListener
import com.pro.music.model.Song
import com.pro.music.utils.GlideUtils.loadUrl

class SongPopularAdapter(
        private var mContext: Context?,
        private val mListSongs: List<Song>?,
        private val iOnClickSongItemListener: IOnClickSongItemListener
        ) : RecyclerView.Adapter<SongPopularViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongPopularViewHolder {
        val itemSongPopularBinding = ItemSongPopularBinding.inflate(LayoutInflater
                .from(parent.context), parent, false)
        return SongPopularViewHolder(itemSongPopularBinding)
    }

    override fun onBindViewHolder(holder: SongPopularViewHolder, position: Int) {
        val song = mListSongs!![position]
        loadUrl(song.image, holder.mItemSongPopularBinding.imgSong)
        holder.mItemSongPopularBinding.tvSongName.text = song.title
        holder.mItemSongPopularBinding.tvArtist.text = song.artist
        var strListen = mContext!!.getString(R.string.label_listen)
        if (song.count > 1) {
            strListen = mContext!!.getString(R.string.label_listens)
        }
        val strCountListen = song.count.toString() + " " + strListen
        holder.mItemSongPopularBinding.tvCountListen.text = strCountListen
        val isFavorite = isFavoriteSong(song)
        if (isFavorite) {
            holder.mItemSongPopularBinding.imgFavorite.setImageResource(R.drawable.ic_favorite)
        } else {
            holder.mItemSongPopularBinding.imgFavorite.setImageResource(R.drawable.ic_unfavorite)
        }
        holder.mItemSongPopularBinding.imgFavorite.setOnClickListener { iOnClickSongItemListener.onClickFavoriteSong(song, !isFavorite) }
        holder.mItemSongPopularBinding.imgMoreOption.setOnClickListener { iOnClickSongItemListener.onClickMoreOptions(song) }
        holder.mItemSongPopularBinding.layoutSongInfo.setOnClickListener { iOnClickSongItemListener.onClickItemSong(song) }
    }

    override fun getItemCount(): Int {
        return mListSongs?.size ?: 0
    }

    fun release() {
        if (mContext != null) {
            mContext = null
        }
    }

    class SongPopularViewHolder(
            val mItemSongPopularBinding: ItemSongPopularBinding
            ) : RecyclerView.ViewHolder(mItemSongPopularBinding.root)
}