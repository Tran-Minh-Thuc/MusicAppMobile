package com.pro.music.model

import java.io.Serializable

class SelectObject : Serializable {
    var id: Long = 0
    var name: String? = null

    constructor()

    constructor(id: Long, name: String?) {
        this.id = id
        this.name = name
    }
}