package com.pro.music.fragment

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.pro.music.R
import com.pro.music.adapter.SongPlayingAdapter
import com.pro.music.constant.Constant
import com.pro.music.constant.GlobalFunction
import com.pro.music.databinding.FragmentListSongPlayingBinding
import com.pro.music.listener.IOnClickSongPlayingItemListener
import com.pro.music.service.MusicService
import com.pro.music.service.MusicService.Companion.deleteSongFromPlaylist
import com.pro.music.service.MusicService.Companion.isSongPlaying

class ListSongPlayingFragment : Fragment() {

    private var binding: FragmentListSongPlayingBinding? = null
    private var mSongPlayingAdapter: SongPlayingAdapter? = null
    private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateStatusListSongPlaying()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentListSongPlayingBinding.inflate(inflater, container, false)
        if (activity != null) {
            LocalBroadcastManager.getInstance(activity!!).registerReceiver(mBroadcastReceiver,
                    IntentFilter(Constant.CHANGE_LISTENER))
        }
        displayListSongPlaying()
        return binding?.root
    }

    private fun displayListSongPlaying() {
        if (activity == null || MusicService.mListSongPlaying == null) {
            return
        }
        val linearLayoutManager = LinearLayoutManager(activity)
        binding?.rcvData?.layoutManager = linearLayoutManager
        mSongPlayingAdapter = SongPlayingAdapter(MusicService.mListSongPlaying,
                object : IOnClickSongPlayingItemListener {
                    override fun onClickItemSongPlaying(position: Int) {
                        clickItemSongPlaying(position)
                    }

                    override fun onClickRemoveFromPlaylist(position: Int) {
                        deleteSongFromPlaylist(position)
                    }
                })
        binding?.rcvData?.adapter = mSongPlayingAdapter
        updateStatusListSongPlaying()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateStatusListSongPlaying() {
        if (activity == null || MusicService.mListSongPlaying == null || MusicService.mListSongPlaying!!.isEmpty()) {
            return
        }
        for (i in MusicService.mListSongPlaying!!.indices) {
            MusicService.mListSongPlaying!![i].isPlaying = i == MusicService.mSongPosition
        }
        mSongPlayingAdapter!!.notifyDataSetChanged()
    }

    private fun clickItemSongPlaying(position: Int) {
        MusicService.isPlaying = false
        GlobalFunction.startMusicService(activity, Constant.PLAY, position)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteSongFromPlaylist(position: Int) {
        if (activity == null) return
        if (MusicService.mListSongPlaying == null || MusicService.mListSongPlaying!!.isEmpty()) {
            return
        }
        val songDelete = MusicService.mListSongPlaying!![position]
        AlertDialog.Builder(activity!!)
                .setTitle(songDelete.title)
                .setMessage(getString(R.string.msg_confirm_delete))
                .setPositiveButton(getString(R.string.action_ok)) { _: DialogInterface?, _: Int ->
                    if (isSongPlaying(songDelete.id)) {
                        GlobalFunction.showToastMessage(activity,
                                activity!!.getString(R.string.msg_cannot_delete_song))
                    } else {
                        deleteSongFromPlaylist(songDelete.id)
                        if (mSongPlayingAdapter != null) mSongPlayingAdapter!!.notifyDataSetChanged()
                        GlobalFunction.showToastMessage(activity,
                                activity!!.getString(R.string.msg_delete_song_from_playlist_success))
                    }
                }
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activity != null) {
            LocalBroadcastManager.getInstance(activity!!).unregisterReceiver(mBroadcastReceiver)
        }
    }
}