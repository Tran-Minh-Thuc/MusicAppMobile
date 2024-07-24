package com.pro.music

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.pro.music.prefs.DataStoreManager

class MyApplication : Application() {

    private var mFirebaseDatabase: FirebaseDatabase? = null

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        mFirebaseDatabase = FirebaseDatabase.getInstance(FIREBASE_URL)
        createChannelNotification()
        DataStoreManager.init(applicationContext)
    }

    private fun createChannelNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_MIN)
            channel.setSound(null, null)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
//test
    fun categoryDatabaseReference(): DatabaseReference? {
        return mFirebaseDatabase?.getReference("/category")
    }

    fun artistDatabaseReference(): DatabaseReference? {
        return mFirebaseDatabase?.getReference("/artist")
    }

    fun songsDatabaseReference(): DatabaseReference? {
        return mFirebaseDatabase?.getReference("/songs")
    }

    fun feedbackDatabaseReference(): DatabaseReference? {
        return mFirebaseDatabase?.getReference("/feedback")
    }

    fun getCountViewDatabaseReference(songId: Long): DatabaseReference? {
        return mFirebaseDatabase?.getReference("/songs/$songId/count")
    }

    fun getSongDetailDatabaseReference(songId: Long): DatabaseReference? {
        return mFirebaseDatabase?.getReference("/songs/$songId")
    }

    companion object {
        // Firebase url
        const val FIREBASE_URL = "https://musicapp-5c78a-default-rtdb.firebaseio.com"
        const val CHANNEL_ID = "channel_music_basic_id"
        private const val CHANNEL_NAME = "channel_music_basic_name"
        @JvmStatic
        operator fun get(context: Context): MyApplication {
            return context.applicationContext as MyApplication
        }
    }
}