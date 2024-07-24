package com.pro.music.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pro.music.constant.Constant
import com.pro.music.constant.GlobalFunction

class MusicReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.extras!!.getInt(Constant.MUSIC_ACTION)
        GlobalFunction.startMusicService(context, action, MusicService.mSongPosition)
    }
}