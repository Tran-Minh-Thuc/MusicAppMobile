package com.pro.music.service

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.pro.music.MyApplication
import com.pro.music.R
import com.pro.music.activity.MainActivity
import com.pro.music.constant.Constant
import com.pro.music.constant.GlobalFunction
import com.pro.music.model.Song
import com.pro.music.prefs.DataStoreManager
import com.pro.music.utils.StringUtil.isEmpty
import java.util.Random

class MusicService : Service(), OnPreparedListener, OnCompletionListener {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        if (mPlayer == null) {
            mPlayer = MediaPlayer()
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(Constant.MUSIC_ACTION)) {
                mAction = bundle.getInt(Constant.MUSIC_ACTION)
            }
            if (bundle.containsKey(Constant.SONG_POSITION)) {
                mSongPosition = bundle.getInt(Constant.SONG_POSITION)
            }
            handleActionMusic(mAction)
        }
        return START_NOT_STICKY
    }

    private fun handleActionMusic(action: Int) {
        when (action) {
            Constant.PLAY -> playSong()
            Constant.PREVIOUS -> prevSong()
            Constant.NEXT -> nextSong()
            Constant.PAUSE -> pauseSong()
            Constant.RESUME -> resumeSong()
            Constant.CANCEL_NOTIFICATION -> cancelNotification()
            else -> {}
        }
    }

    private fun playSong() {
        val songUrl = mListSongPlaying!![mSongPosition].url
        if (!isEmpty(songUrl)) {
            playMediaPlayer(songUrl)
        }
        mListSongPlaying!![mSongPosition].isPriority = false
    }

    private fun pauseSong() {
        if (mPlayer != null && mPlayer!!.isPlaying) {
            mPlayer!!.pause()
            isPlaying = false
            sendMusicNotification()
            sendBroadcastChangeListener()
        }
    }

    private fun cancelNotification() {
        if (mPlayer != null && mPlayer!!.isPlaying) {
            mPlayer!!.pause()
            isPlaying = false
        }
        clearListSongPlaying()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
        sendBroadcastChangeListener()
        stopSelf()
    }

    private fun resumeSong() {
        if (mPlayer != null) {
            mPlayer!!.start()
            isPlaying = true
            sendMusicNotification()
            sendBroadcastChangeListener()
        }
    }

    private fun prevSong() {
        val newPosition: Int = if (positionPriority > 0) {
            positionPriority
        } else {
            if (mListSongPlaying!!.size > 1) {
                if (isShuffle) {
                    Random().nextInt(mListSongPlaying!!.size)
                } else {
                    if (isRepeat) mSongPosition else if (mSongPosition > 0) {
                        mSongPosition - 1
                    } else {
                        mListSongPlaying!!.size - 1
                    }
                }
            } else {
                0
            }
        }
        mSongPosition = newPosition
        sendMusicNotification()
        sendBroadcastChangeListener()
        playSong()
    }

    private fun nextSong() {
        val newPosition: Int = if (positionPriority > 0) {
            positionPriority
        } else {
            if (mListSongPlaying!!.size > 1) {
                if (isShuffle) {
                    Random().nextInt(mListSongPlaying!!.size)
                } else {
                    if (isRepeat) mSongPosition else if (mSongPosition < mListSongPlaying!!.size - 1) {
                        mSongPosition + 1
                    } else {
                        0
                    }
                }
            } else {
                0
            }
        }
        mSongPosition = newPosition
        sendMusicNotification()
        sendBroadcastChangeListener()
        playSong()
    }

    private fun playMediaPlayer(songUrl: String?) {
        try {
            if (mPlayer!!.isPlaying) {
                mPlayer!!.stop()
            }
            mPlayer!!.reset()
            mPlayer!!.setDataSource(songUrl)
            mPlayer!!.prepareAsync()
            initControl()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initControl() {
        mPlayer!!.setOnPreparedListener(this)
        mPlayer!!.setOnCompletionListener(this)
    }

    private fun sendMusicNotification() {
        if (DataStoreManager.user?.isAdmin == true) return
        val song = mListSongPlaying!![mSongPosition]
        val pendingFlag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val intent = Intent(this, MainActivity::class.java)
        @SuppressLint("UnspecifiedImmutableFlag") val pendingIntent = PendingIntent.getActivity(this, 0, intent, pendingFlag)
        val remoteViews = RemoteViews(packageName, R.layout.layout_push_notification_music)
        remoteViews.setTextViewText(R.id.tv_song_name, song.title)

        // Set listener
        remoteViews.setOnClickPendingIntent(R.id.img_previous, GlobalFunction.openMusicReceiver(this, Constant.PREVIOUS))
        remoteViews.setOnClickPendingIntent(R.id.img_next, GlobalFunction.openMusicReceiver(this, Constant.NEXT))
        if (isPlaying) {
            remoteViews.setImageViewResource(R.id.img_play, R.drawable.ic_pause_gray)
            remoteViews.setOnClickPendingIntent(R.id.img_play, GlobalFunction.openMusicReceiver(this, Constant.PAUSE))
        } else {
            remoteViews.setImageViewResource(R.id.img_play, R.drawable.ic_play_gray)
            remoteViews.setOnClickPendingIntent(R.id.img_play, GlobalFunction.openMusicReceiver(this, Constant.RESUME))
        }
        remoteViews.setOnClickPendingIntent(R.id.img_close, GlobalFunction.openMusicReceiver(this, Constant.CANCEL_NOTIFICATION))
        val builder = NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_small_push_notification)
                .setContentIntent(pendingIntent)
                .setSound(null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            builder.setCustomBigContentView(remoteViews)
        } else {
            builder.setCustomContentView(remoteViews)
        }
        val notification = builder.build()
        startForeground(1, notification)
    }

    override fun onCompletion(mp: MediaPlayer) {
        mAction = Constant.NEXT
        nextSong()
    }

    override fun onPrepared(mp: MediaPlayer) {
        mLengthSong = mPlayer!!.duration
        mp.start()
        isPlaying = true
        mAction = Constant.PLAY
        sendMusicNotification()
        sendBroadcastChangeListener()
        changeCountViewSong()
    }

    private fun sendBroadcastChangeListener() {
        val intent = Intent(Constant.CHANGE_LISTENER)
        intent.putExtra(Constant.MUSIC_ACTION, mAction)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun changeCountViewSong() {
        if (DataStoreManager.user?.isAdmin == true) return
        val songId = mListSongPlaying!![mSongPosition].id
        MyApplication[this].getCountViewDatabaseReference(songId)
                ?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val currentCount = snapshot.getValue(Int::class.java)
                        if (currentCount != null) {
                            val newCount = currentCount + 1
                            MyApplication[this@MusicService].getCountViewDatabaseReference(songId)?.removeEventListener(this)
                            MyApplication[this@MusicService].getCountViewDatabaseReference(songId)?.setValue(newCount)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mPlayer != null) {
            mPlayer!!.release()
            mPlayer = null
        }
    }

    companion object {
        @JvmField
        var isPlaying = false
        @JvmField
        var mListSongPlaying: MutableList<Song>? = null
        @JvmField
        var mSongPosition = 0
        @JvmField
        var mPlayer: MediaPlayer? = null
        @JvmField
        var mLengthSong = 0
        @JvmField
        var mAction = -1
        @JvmField
        var isShuffle = false
        @JvmField
        var isRepeat = false
        @JvmStatic
        fun clearListSongPlaying() {
            if (mListSongPlaying != null) {
                mListSongPlaying!!.clear()
            } else {
                mListSongPlaying = ArrayList()
            }
        }

        @JvmStatic
        fun isSongExist(songId: Long): Boolean {
            if (mListSongPlaying == null || mListSongPlaying!!.isEmpty()) return false
            var isExist = false
            for (song in mListSongPlaying!!) {
                if (songId == song.id) {
                    isExist = true
                    break
                }
            }
            return isExist
        }

        @JvmStatic
        fun isSongPlaying(songId: Long): Boolean {
            if (mListSongPlaying == null || mListSongPlaying!!.isEmpty()) return false
            val currentSong = mListSongPlaying!![mSongPosition]
            return songId == currentSong.id
        }

        val positionPriority: Int
            get() {
                if (mListSongPlaying == null || mListSongPlaying!!.isEmpty()) return 0
                var position = 0
                for (i in mListSongPlaying!!.indices) {
                    if (mListSongPlaying!![i].isPriority) {
                        position = i
                        break
                    }
                }
                return position
            }

        @JvmStatic
        fun deleteSongFromPlaylist(songId: Long) {
            if (mListSongPlaying == null || mListSongPlaying!!.isEmpty()) return
            var songPosition = 0
            for (i in mListSongPlaying!!.indices) {
                if (songId == mListSongPlaying!![i].id) {
                    songPosition = i
                    break
                }
            }
            for (song in mListSongPlaying!!) {
                if (songId == song.id) {
                    mListSongPlaying!!.remove(song)
                    if (mSongPosition > songPosition) {
                        mSongPosition -= 1
                    }
                    break
                }
            }
        }
    }
}