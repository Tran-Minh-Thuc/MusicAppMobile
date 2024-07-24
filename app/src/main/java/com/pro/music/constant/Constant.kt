package com.pro.music.constant

object Constant {
    // Max count
    const val MAX_COUNT_BANNER = 5
    const val MAX_COUNT_POPULAR = 5
    const val MAX_COUNT_FAVORITE = 5
    const val MAX_COUNT_CATEGORY = 4
    const val MAX_COUNT_ARTIST = 6

    // Music actions
    const val PLAY = 0
    const val PAUSE = 1
    const val NEXT = 2
    const val PREVIOUS = 3
    const val RESUME = 4
    const val CANCEL_NOTIFICATION = 5
    const val MUSIC_ACTION = "musicAction"
    const val SONG_POSITION = "songPosition"
    const val CHANGE_LISTENER = "change_listener"

    // Key intent
    const val CATEGORY_ID = "category_id"
    const val ARTIST_ID = "artist_id"
    const val IS_FROM_MENU_LEFT = "is_from_menu_left"
    const val KEY_INTENT_CATEGORY_OBJECT = "category_object"
    const val KEY_INTENT_ARTIST_OBJECT = "artist_object"
    const val KEY_INTENT_SONG_OBJECT = "song_object"
    const val ADMIN_EMAIL_FORMAT = "@admin.com"
}