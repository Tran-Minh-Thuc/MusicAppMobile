package com.pro.music.fragment

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.pro.music.MyApplication
import com.pro.music.R
import com.pro.music.activity.PlayMusicActivity
import com.pro.music.constant.Constant
import com.pro.music.constant.GlobalFunction
import com.pro.music.databinding.FragmentPlaySongBinding
import com.pro.music.model.Song
import com.pro.music.prefs.DataStoreManager.Companion.user
import com.pro.music.service.MusicService
import com.pro.music.utils.AppUtil.getTime
import com.pro.music.utils.GlideUtils.loadUrl
import java.util.Timer
import java.util.TimerTask

@SuppressLint("NonConstantResourceId")
class PlaySongFragment : Fragment(), View.OnClickListener {

    private var binding: FragmentPlaySongBinding? = null
    private var mTimer: Timer? = null
    private var mAction = 0
    private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            mAction = intent.getIntExtra(Constant.MUSIC_ACTION, 0)
            handleMusicAction()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentPlaySongBinding.inflate(inflater, container, false)
        if (activity != null) {
            LocalBroadcastManager.getInstance(activity!!).registerReceiver(mBroadcastReceiver,
                    IntentFilter(Constant.CHANGE_LISTENER))
        }
        initControl()
        showInfoSong()
        updateStatusShuffleButton()
        updateStatusRepeatButton()
        mAction = MusicService.mAction
        handleMusicAction()
        return binding?.root
    }

    private fun initControl() {
        mTimer = Timer()
        binding?.imgShuffle?.setOnClickListener(this)
        binding?.imgRepeat?.setOnClickListener(this)
        binding?.imgPrevious?.setOnClickListener(this)
        binding?.imgPlay?.setOnClickListener(this)
        binding?.imgNext?.setOnClickListener(this)
        binding?.imgDownload?.setOnClickListener(this)
        binding?.seekbar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                MusicService.mPlayer!!.seekTo(seekBar.progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
        })
    }

    private fun showInfoSong() {
        if (MusicService.mListSongPlaying == null || MusicService.mListSongPlaying!!.isEmpty()) {
            return
        }
        val currentSong = MusicService.mListSongPlaying!![MusicService.mSongPosition]
        binding?.tvSongName?.text = currentSong.title
        binding?.tvArtist?.text = currentSong.artist
        binding?.imgSong?.let { loadUrl(currentSong.image, it) }
        if (user!!.isAdmin) {
            binding?.layoutCountView?.visibility = View.GONE
            binding?.imgDownload?.visibility = View.GONE
            binding?.imgFavorite?.visibility = View.GONE
            binding?.imgRepeat?.visibility = View.GONE
            binding?.imgShuffle?.visibility = View.GONE
        } else {
            binding?.layoutCountView?.visibility = View.VISIBLE
            binding?.imgDownload?.visibility = View.VISIBLE
            binding?.imgFavorite?.visibility = View.VISIBLE
            binding?.imgRepeat?.visibility = View.VISIBLE
            binding?.imgShuffle?.visibility = View.VISIBLE
            listenerCountViewSong(currentSong.id)
            listenerFavoriteSong(currentSong.id)
        }
    }

    private fun handleMusicAction() {
        if (Constant.CANCEL_NOTIFICATION == mAction) {
            if (activity != null) {
                activity!!.onBackPressed()
            }
            return
        }
        when (mAction) {
            Constant.PREVIOUS, Constant.NEXT -> {
                stopAnimationPlayMusic()
                showInfoSong()
            }

            Constant.PLAY -> {
                showInfoSong()
                if (MusicService.isPlaying) {
                    startAnimationPlayMusic()
                }
                showSeekBar()
                showStatusButtonPlay()
            }

            Constant.PAUSE -> {
                stopAnimationPlayMusic()
                showSeekBar()
                showStatusButtonPlay()
            }

            Constant.RESUME -> {
                startAnimationPlayMusic()
                showSeekBar()
                showStatusButtonPlay()
            }
        }
    }

    private fun startAnimationPlayMusic() {
        val runnable: Runnable = object : Runnable {
            override fun run() {
                binding?.imgSong?.animate()?.rotationBy(360f)?.withEndAction(this)?.setDuration(15000)
                        ?.setInterpolator(LinearInterpolator())?.start()
            }
        }
        binding?.imgSong?.animate()?.rotationBy(360f)?.withEndAction(runnable)?.setDuration(15000)
                ?.setInterpolator(LinearInterpolator())?.start()
    }

    private fun stopAnimationPlayMusic() {
        binding?.imgSong?.animate()?.cancel()
    }

    private fun showSeekBar() {
        mTimer!!.schedule(object : TimerTask() {
            override fun run() {
                if (activity == null) {
                    return
                }
                activity!!.runOnUiThread {
                    if (MusicService.mPlayer == null) {
                        return@runOnUiThread
                    }
                    binding?.tvTimeCurrent?.text = getTime(MusicService.mPlayer!!.currentPosition)
                    binding?.tvTimeMax?.text = getTime(MusicService.mLengthSong)
                    binding?.seekbar?.max = MusicService.mLengthSong
                    binding?.seekbar?.progress = MusicService.mPlayer!!.currentPosition
                }
            }
        }, 0, 1000)
    }

    private fun showStatusButtonPlay() {
        if (MusicService.isPlaying) {
            binding?.imgPlay?.setImageResource(R.drawable.ic_pause_black)
        } else {
            binding?.imgPlay?.setImageResource(R.drawable.ic_play_black)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mTimer != null) {
            mTimer!!.cancel()
            mTimer = null
        }
        if (activity != null) {
            LocalBroadcastManager.getInstance(activity!!).unregisterReceiver(mBroadcastReceiver)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.img_previous -> clickOnPrevButton()
            R.id.img_play -> clickOnPlayButton()
            R.id.img_next -> clickOnNextButton()
            R.id.img_shuffle -> clickOnShuffleButton()
            R.id.img_repeat -> clickOnRepeatButton()
            R.id.img_download -> clickOnDownloadSong()
            else -> {}
        }
    }

    private fun clickOnShuffleButton() {
        if (!MusicService.isShuffle) {
            MusicService.isShuffle = true
            MusicService.isRepeat = false
        } else {
            MusicService.isShuffle = false
        }
        updateStatusShuffleButton()
        updateStatusRepeatButton()
    }

    private fun clickOnRepeatButton() {
        if (!MusicService.isRepeat) {
            MusicService.isRepeat = true
            MusicService.isShuffle = false
        } else {
            MusicService.isRepeat = false
        }
        updateStatusShuffleButton()
        updateStatusRepeatButton()
    }

    private fun updateStatusShuffleButton() {
        if (MusicService.isShuffle) {
            binding?.imgShuffle?.setImageResource(R.drawable.ic_shuffle_enable)
        } else {
            binding?.imgShuffle?.setImageResource(R.drawable.ic_shuffle_disable)
        }
    }

    private fun updateStatusRepeatButton() {
        if (MusicService.isRepeat) {
            binding?.imgRepeat?.setImageResource(R.drawable.ic_repeat_one_enable)
        } else {
            binding?.imgRepeat?.setImageResource(R.drawable.ic_repeat_disable)
        }
    }

    private fun clickOnPrevButton() {
        GlobalFunction.startMusicService(activity, Constant.PREVIOUS, MusicService.mSongPosition)
    }

    private fun clickOnNextButton() {
        GlobalFunction.startMusicService(activity, Constant.NEXT, MusicService.mSongPosition)
    }

    private fun clickOnPlayButton() {
        if (MusicService.isPlaying) {
            GlobalFunction.startMusicService(activity, Constant.PAUSE, MusicService.mSongPosition)
        } else {
            GlobalFunction.startMusicService(activity, Constant.RESUME, MusicService.mSongPosition)
        }
    }

    private fun listenerCountViewSong(songId: Long) {
        if (activity == null) return
        val countViewSongEventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentCount = snapshot.getValue(Int::class.java)
                if (currentCount != null) {
                    binding?.tvCountView?.text = currentCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        MyApplication[activity!!].getCountViewDatabaseReference(songId)
                ?.addValueEventListener(countViewSongEventListener)
    }

    private fun listenerFavoriteSong(songId: Long) {
        if (activity == null) return
        MyApplication[activity!!].getSongDetailDatabaseReference(songId)
                ?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val song = snapshot.getValue(Song::class.java) ?: return
                        val isFavorite = GlobalFunction.isFavoriteSong(song)
                        if (isFavorite) {
                            binding?.imgFavorite?.setImageResource(R.drawable.ic_favorite)
                        } else {
                            binding?.imgFavorite?.setImageResource(R.drawable.ic_unfavorite)
                        }
                        binding?.imgFavorite?.setOnClickListener { GlobalFunction.onClickFavoriteSong(activity, song, !isFavorite) }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
    }

    private fun clickOnDownloadSong() {
        val currentSong = MusicService.mListSongPlaying!![MusicService.mSongPosition]
        val activity = activity as PlayMusicActivity? ?: return
        activity.downloadSong(currentSong)
    }
}