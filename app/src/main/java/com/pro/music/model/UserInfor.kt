package com.pro.music.model

import java.io.Serializable

class UserInfor : Serializable {
    var id: Long = 0
    var emailUser: String? = null

    constructor()

    constructor(id: Long, emailUser: String?) {
        this.id = id
        this.emailUser = emailUser
    }
}