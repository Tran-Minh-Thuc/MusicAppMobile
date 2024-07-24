package com.pro.music.listener

import com.pro.music.model.Song

interface IOnClickSongItemListener {
    fun onClickItemSong(song: Song)
    fun onClickFavoriteSong(song: Song, favorite: Boolean)
    fun onClickMoreOptions(song: Song)
}