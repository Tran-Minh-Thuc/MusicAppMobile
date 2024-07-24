package com.pro.music.listener

import com.pro.music.model.Song

interface IOnAdminManagerSongListener {
    fun onClickUpdateSong(song: Song)
    fun onClickDeleteSong(song: Song)
    fun onClickDetailSong(song: Song)
}