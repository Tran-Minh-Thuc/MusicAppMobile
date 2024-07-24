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
import com.pro.music.activity.AdminAddArtistActivity
import com.pro.music.activity.AdminArtistSongActivity
import com.pro.music.adapter.AdminArtistAdapter
import com.pro.music.constant.Constant
import com.pro.music.constant.GlobalFunction
import com.pro.music.databinding.FragmentAdminArtistBinding
import com.pro.music.listener.IOnAdminManagerArtistListener
import com.pro.music.model.Artist
import com.pro.music.utils.StringUtil.isEmpty
import java.util.Locale

class AdminArtistFragment : Fragment() {

    private var binding: FragmentAdminArtistBinding? = null
    private var mListArtist: MutableList<Artist>? = null
    private var mAdminArtistAdapter: AdminArtistAdapter? = null
    private var mChildEventListener: ChildEventListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentAdminArtistBinding.inflate(inflater, container, false)
        initView()
        initListener()
        loadListArtist("")
        return binding?.root
    }

    private fun initView() {
        val linearLayoutManager = LinearLayoutManager(activity)
        binding?.rcvArtist?.layoutManager = linearLayoutManager
        mListArtist = ArrayList()
        mAdminArtistAdapter = AdminArtistAdapter(mListArtist, object : IOnAdminManagerArtistListener {
            override fun onClickUpdateArtist(artist: Artist) {
                onClickEditArtist(artist)
            }

            override fun onClickDeleteArtist(artist: Artist) {
                deleteArtistItem(artist)
            }

            override fun onClickDetailArtist(artist: Artist) {
                val bundle = Bundle()
                bundle.putSerializable(Constant.KEY_INTENT_ARTIST_OBJECT, artist)
                GlobalFunction.startActivity(activity, AdminArtistSongActivity::class.java, bundle)
            }
        })
        binding?.rcvArtist?.adapter = mAdminArtistAdapter
        binding?.rcvArtist?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    binding?.btnAddArtist?.hide()
                } else {
                    binding?.btnAddArtist?.show()
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }

    private fun initListener() {
        binding?.btnAddArtist?.setOnClickListener { onClickAddArtist() }
        binding?.imgSearch?.setOnClickListener { searchArtist() }
        binding?.edtSearchName?.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchArtist()
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
                    searchArtist()
                }
            }
        })
    }

    private fun onClickAddArtist() {
        GlobalFunction.startActivity(activity, AdminAddArtistActivity::class.java)
    }

    private fun onClickEditArtist(artist: Artist?) {
        val bundle = Bundle()
        bundle.putSerializable(Constant.KEY_INTENT_ARTIST_OBJECT, artist)
        GlobalFunction.startActivity(activity, AdminAddArtistActivity::class.java, bundle)
    }

    private fun deleteArtistItem(artist: Artist?) {
        AlertDialog.Builder(activity)
                .setTitle(getString(R.string.msg_delete_title))
                .setMessage(getString(R.string.msg_confirm_delete))
                .setPositiveButton(getString(R.string.action_ok)) { _: DialogInterface?, _: Int ->
                    if (activity == null) return@setPositiveButton
                    MyApplication[activity!!].artistDatabaseReference()
                            ?.child(artist!!.id.toString())
                            ?.removeValue { _: DatabaseError?, _: DatabaseReference? ->
                                Toast.makeText(activity,
                                        getString(R.string.msg_delete_artist_successfully),
                                        Toast.LENGTH_SHORT).show()
                            }
                }
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show()
    }

    private fun searchArtist() {
        val strKey = binding?.edtSearchName?.text.toString().trim { it <= ' ' }
        resetListArtist()
        if (activity != null) {
            MyApplication[activity!!].artistDatabaseReference()
                    ?.removeEventListener(mChildEventListener!!)
        }
        loadListArtist(strKey)
        GlobalFunction.hideSoftKeyboard(activity)
    }

    private fun resetListArtist() {
        if (mListArtist != null) {
            mListArtist!!.clear()
        } else {
            mListArtist = ArrayList()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun loadListArtist(keyword: String?) {
        if (activity == null) return
        mChildEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val artist = dataSnapshot.getValue(Artist::class.java)
                if (artist == null || mListArtist == null) return
                if (isEmpty(keyword)) {
                    mListArtist!!.add(0, artist)
                } else {
                    if (GlobalFunction.getTextSearch(artist.name).toLowerCase(Locale.getDefault()).trim { it <= ' ' }
                                    .contains(GlobalFunction.getTextSearch(keyword).toLowerCase(Locale.getDefault()).trim { it <= ' ' })) {
                        mListArtist!!.add(0, artist)
                    }
                }
                if (mAdminArtistAdapter != null) mAdminArtistAdapter!!.notifyDataSetChanged()
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                val artist = dataSnapshot.getValue(Artist::class.java)
                if (artist == null || mListArtist == null || mListArtist!!.isEmpty()) return
                for (i in mListArtist!!.indices) {
                    if (artist.id == mListArtist!![i].id) {
                        mListArtist!![i] = artist
                        break
                    }
                }
                if (mAdminArtistAdapter != null) mAdminArtistAdapter!!.notifyDataSetChanged()
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val artist = dataSnapshot.getValue(Artist::class.java)
                if (artist == null || mListArtist == null || mListArtist!!.isEmpty()) return
                for (artistObject in mListArtist!!) {
                    if (artist.id == artistObject.id) {
                        mListArtist!!.remove(artistObject)
                        break
                    }
                }
                if (mAdminArtistAdapter != null) mAdminArtistAdapter!!.notifyDataSetChanged()
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        MyApplication[activity!!].artistDatabaseReference()
                ?.addChildEventListener(mChildEventListener!!)
    }
}