package com.pro.music.listener

import com.pro.music.model.Artist

interface IOnAdminManagerArtistListener {
    fun onClickUpdateArtist(artist: Artist)
    fun onClickDeleteArtist(artist: Artist)
    fun onClickDetailArtist(artist: Artist)
}