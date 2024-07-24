package com.pro.music.listener

interface IOnClickSongPlayingItemListener {
    fun onClickItemSongPlaying(position: Int)
    fun onClickRemoveFromPlaylist(position: Int)
}