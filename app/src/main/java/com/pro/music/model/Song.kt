package com.pro.music.model

import java.io.Serializable

class Song : Serializable {
    var id: Long = 0
    var title: String? = null
    var image: String? = null
    var url: String? = null
    var artistId: Long = 0
    var artist: String? = null
    var categoryId: Long = 0
    var category: String? = null
    var isFeatured: Boolean? = false
    var count = 0
    var isPlaying = false
    var isPriority = false
    var favorite: HashMap<String, UserInfor>? = null

    constructor()

    constructor(id: Long, title: String?, image: String?, url: String?, artistId: Long, artist: String?,
                categoryId: Long, category: String?, featured: Boolean?) {
        this.id = id
        this.title = title
        this.image = image
        this.url = url
        this.artistId = artistId
        this.artist = artist
        this.categoryId = categoryId
        this.category = category
        isFeatured = featured
    }
}