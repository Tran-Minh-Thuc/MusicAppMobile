package com.pro.music.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.pro.music.MyApplication
import com.pro.music.R
import com.pro.music.adapter.AdminSongAdapter
import com.pro.music.constant.Constant
import com.pro.music.constant.GlobalFunction.startActivity
import com.pro.music.constant.GlobalFunction.startMusicService
import com.pro.music.databinding.ActivityAdminArtistSongBinding
import com.pro.music.listener.IOnAdminManagerSongListener
import com.pro.music.model.Artist
import com.pro.music.model.Song
import com.pro.music.service.MusicService
import com.pro.music.service.MusicService.Companion.clearListSongPlaying

class AdminArtistSongActivity : BaseActivity() {

    private var binding: ActivityAdminArtistSongBinding? = null
    private var mListSong: MutableList<Song>? = null
    private var mAdminSongAdapter: AdminSongAdapter? = null
    private var mArtist: Artist? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminArtistSongBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        loadDataIntent()
        initToolbar()
        initView()
        loadListSong()
    }

    private fun loadDataIntent() {
        val bundleReceived = intent.extras
        if (bundleReceived != null) {
            mArtist = bundleReceived[Constant.KEY_INTENT_ARTIST_OBJECT] as Artist?
        }
    }

    private fun initToolbar() {
        binding?.toolbar?.imgLeft?.setImageResource(R.drawable.ic_back_white)
        binding?.toolbar?.layoutPlayAll?.visibility = View.GONE
        binding?.toolbar?.imgLeft?.setOnClickListener { onBackPressed() }
        binding?.toolbar?.tvTitle?.text = mArtist!!.name
    }

    private fun initView() {
        val linearLayoutManager = LinearLayoutManager(this)
        binding?.rcvSong?.layoutManager = linearLayoutManager
        mListSong = ArrayList()
        mAdminSongAdapter = AdminSongAdapter(mListSong, object : IOnAdminManagerSongListener {
            override fun onClickUpdateSong(song: Song) {
                onClickEditSong(song)
            }

            override fun onClickDeleteSong(song: Song) {
                deleteSongItem(song)
            }

            override fun onClickDetailSong(song: Song) {
                goToSongDetail(song)
            }
        })
        binding?.rcvSong?.adapter = mAdminSongAdapter
    }

    private fun goToSongDetail(song: Song) {
        clearListSongPlaying()
        MusicService.mListSongPlaying!!.add(song)
        MusicService.isPlaying = false
        startMusicService(this, Constant.PLAY, 0)
        startActivity(this, PlayMusicActivity::class.java)
    }

    private fun onClickEditSong(song: Song?) {
        val bundle = Bundle()
        bundle.putSerializable(Constant.KEY_INTENT_SONG_OBJECT, song)
        startActivity(this, AdminAddSongActivity::class.java, bundle)
    }

    private fun deleteSongItem(song: Song?) {
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.msg_delete_title))
                .setMessage(getString(R.string.msg_confirm_delete))
                .setPositiveButton(getString(R.string.action_ok)) { _: DialogInterface?, _: Int ->
                    MyApplication[this].songsDatabaseReference()
                            ?.child(song!!.id.toString())
                            ?.removeValue { _: DatabaseError?, _: DatabaseReference? ->
                                Toast.makeText(this,
                                        getString(R.string.msg_delete_song_successfully),
                                        Toast.LENGTH_SHORT).show()
                            }
                }
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show()
    }

    private fun resetListSong() {
        if (mListSong != null) {
            mListSong!!.clear()
        } else {
            mListSong = ArrayList()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun loadListSong() {
        MyApplication[this].songsDatabaseReference()
                ?.orderByChild("artistId")
                ?.equalTo(mArtist!!.id.toDouble())
                ?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        resetListSong()
                        for (dataSnapshot in snapshot.children) {
                            val song = dataSnapshot.getValue(Song::class.java) ?: return
                            mListSong!!.add(0, song)
                        }
                        if (mAdminSongAdapter != null) mAdminSongAdapter!!.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
    }
}