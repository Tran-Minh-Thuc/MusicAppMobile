package com.pro.music.fragment.admin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.pro.music.MyApplication
import com.pro.music.R
import com.pro.music.activity.AdminAddSongActivity
import com.pro.music.activity.PlayMusicActivity
import com.pro.music.adapter.AdminSongAdapter
import com.pro.music.constant.Constant
import com.pro.music.constant.GlobalFunction
import com.pro.music.databinding.FragmentAdminSongBinding
import com.pro.music.listener.IOnAdminManagerSongListener
import com.pro.music.model.Song
import com.pro.music.service.MusicService
import com.pro.music.service.MusicService.Companion.clearListSongPlaying
import com.pro.music.utils.StringUtil.isEmpty
import java.util.Locale

class AdminSongFragment : Fragment() {

    private var binding: FragmentAdminSongBinding? = null
    private var mListSong: MutableList<Song>? = null
    private var mAdminSongAdapter: AdminSongAdapter? = null
    private var mChildEventListener: ChildEventListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentAdminSongBinding.inflate(inflater, container, false)
        initView()
        initListener()
        loadListSong("")
        return binding?.root
    }

    private fun initView() {
        val linearLayoutManager = LinearLayoutManager(activity)
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
        binding?.rcvSong?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    binding?.btnAddSong?.hide()
                } else {
                    binding?.btnAddSong?.show()
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }

    private fun initListener() {
        binding?.btnAddSong?.setOnClickListener { onClickAddSong() }
        binding?.imgSearch?.setOnClickListener { searchSong() }
        binding?.edtSearchName?.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchSong()
                return@setOnEditorActionListener true
            }
            false
        }
        binding?.edtSearchName?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val strKey = s.toString().trim { it <= ' ' }
                if (strKey == "" || strKey.isEmpty()) {
                    searchSong()
                }
            }
        })
    }

    private fun goToSongDetail(song: Song) {
        clearListSongPlaying()
        MusicService.mListSongPlaying!!.add(song)
        MusicService.isPlaying = false
        GlobalFunction.startMusicService(activity, Constant.PLAY, 0)
        GlobalFunction.startActivity(activity, PlayMusicActivity::class.java)
    }

    private fun onClickAddSong() {
        GlobalFunction.startActivity(activity, AdminAddSongActivity::class.java)
    }

    private fun onClickEditSong(song: Song?) {
        val bundle = Bundle()
        bundle.putSerializable(Constant.KEY_INTENT_SONG_OBJECT, song)
        GlobalFunction.startActivity(activity, AdminAddSongActivity::class.java, bundle)
    }

    private fun deleteSongItem(song: Song?) {
        AlertDialog.Builder(activity)
                .setTitle(getString(R.string.msg_delete_title))
                .setMessage(getString(R.string.msg_confirm_delete))
                .setPositiveButton(getString(R.string.action_ok)) { _: DialogInterface?, _: Int ->
                    if (activity == null) return@setPositiveButton
                    MyApplication[activity!!].songsDatabaseReference()
                            ?.child(song!!.id.toString())
                            ?.removeValue { _: DatabaseError?, _: DatabaseReference? ->
                                Toast.makeText(activity,
                                        getString(R.string.msg_delete_song_successfully),
                                        Toast.LENGTH_SHORT).show()
                            }
                }
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show()
    }

    private fun searchSong() {
        val strKey = binding?.edtSearchName?.text.toString().trim { it <= ' ' }
        resetListSong()
        if (activity != null) {
            MyApplication[activity!!].songsDatabaseReference()
                    ?.removeEventListener(mChildEventListener!!)
        }
        loadListSong(strKey)
        GlobalFunction.hideSoftKeyboard(activity)
    }

    private fun resetListSong() {
        if (mListSong != null) {
            mListSong!!.clear()
        } else {
            mListSong = ArrayList()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadListSong(keyword: String?) {
        if (activity == null) return
        mChildEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val song = dataSnapshot.getValue(Song::class.java)
                if (song == null || mListSong == null) return
                if (isEmpty(keyword)) {
                    mListSong!!.add(0, song)
                } else {
                    if (GlobalFunction.getTextSearch(song.title).toLowerCase(Locale.getDefault()).trim { it <= ' ' }
                                    .contains(GlobalFunction.getTextSearch(keyword).toLowerCase(Locale.getDefault()).trim { it <= ' ' })) {
                        mListSong!!.add(0, song)
                    }
                }
                if (mAdminSongAdapter != null) mAdminSongAdapter!!.notifyDataSetChanged()
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                val song = dataSnapshot.getValue(Song::class.java)
                if (song == null || mListSong == null || mListSong!!.isEmpty()) return
                for (i in mListSong!!.indices) {
                    if (song.id == mListSong!![i].id) {
                        mListSong!![i] = song
                        break
                    }
                }
                if (mAdminSongAdapter != null) mAdminSongAdapter!!.notifyDataSetChanged()
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val song = dataSnapshot.getValue(Song::class.java)
                if (song == null || mListSong == null || mListSong!!.isEmpty()) return
                for (songObject in mListSong!!) {
                    if (song.id == songObject.id) {
                        mListSong!!.remove(songObject)
                        break
                    }
                }
                if (mAdminSongAdapter != null) mAdminSongAdapter!!.notifyDataSetChanged()
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        MyApplication[activity!!].songsDatabaseReference()
                ?.addChildEventListener(mChildEventListener!!)
    }
}