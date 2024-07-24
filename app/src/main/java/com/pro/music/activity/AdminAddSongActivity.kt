package com.pro.music.activity

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.pro.music.MyApplication
import com.pro.music.R
import com.pro.music.adapter.AdminSelectAdapter
import com.pro.music.constant.Constant
import com.pro.music.constant.GlobalFunction.hideSoftKeyboard
import com.pro.music.databinding.ActivityAdminAddSongBinding
import com.pro.music.model.Artist
import com.pro.music.model.Category
import com.pro.music.model.SelectObject
import com.pro.music.model.Song
import com.pro.music.utils.StringUtil.isEmpty
import kotlin.collections.set

class AdminAddSongActivity : BaseActivity() {

    private var binding: ActivityAdminAddSongBinding? = null
    private var isUpdate = false
    private var mSong: Song? = null
    private var mCategorySelected: SelectObject? = null
    private var mArtistSelected: SelectObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminAddSongBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        loadDataIntent()
        initToolbar()
        initView()
        binding?.btnAddOrEdit?.setOnClickListener { addOrEditSong() }
    }

    private fun loadDataIntent() {
        val bundleReceived = intent.extras
        if (bundleReceived != null) {
            isUpdate = true
            mSong = bundleReceived[Constant.KEY_INTENT_SONG_OBJECT] as Song?
        }
    }

    private fun initToolbar() {
        binding?.toolbar?.imgLeft?.setImageResource(R.drawable.ic_back_white)
        binding?.toolbar?.layoutPlayAll?.visibility = View.GONE
        binding?.toolbar?.imgLeft?.setOnClickListener { onBackPressed() }
    }

    private fun initView() {
        if (isUpdate) {
            binding?.toolbar?.tvTitle?.text = getString(R.string.label_update_song)
            binding?.btnAddOrEdit?.text = getString(R.string.action_edit)
            binding?.edtName?.setText(mSong!!.title)
            binding?.edtImage?.setText(mSong!!.image)
            binding?.edtLink?.setText(mSong!!.url)
            binding?.chbFeatured?.isChecked = mSong!!.isFeatured == true
        } else {
            binding?.toolbar?.tvTitle?.text = getString(R.string.label_add_song)
            binding?.btnAddOrEdit?.text = getString(R.string.action_add)
        }
        loadListCategory()
        loadListArtist()
    }

    private fun loadListCategory() {
        MyApplication[this].categoryDatabaseReference()
                ?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val list: MutableList<SelectObject?> = ArrayList()
                        for (dataSnapshot in snapshot.children) {
                            val category = dataSnapshot.getValue(Category::class.java)
                                    ?: return
                            list.add(0, SelectObject(category.id, category.name))
                        }
                        val adapter = AdminSelectAdapter(this@AdminAddSongActivity,
                                R.layout.item_choose_option, list)
                        binding?.spnCategory?.adapter = adapter
                        binding?.spnCategory?.onItemSelectedListener = object : OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                                mCategorySelected = adapter.getItem(position)
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {}
                        }
                        if (mSong != null && mSong!!.categoryId > 0) {
                            binding?.spnCategory?.setSelection(getPositionSelected(list, mSong!!.categoryId))
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
    }

    private fun loadListArtist() {
        MyApplication[this].artistDatabaseReference()
                ?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val list: MutableList<SelectObject?> = ArrayList()
                        for (dataSnapshot in snapshot.children) {
                            val artist = dataSnapshot.getValue(Artist::class.java) ?: return
                            list.add(0, SelectObject(artist.id, artist.name))
                        }
                        val adapter = AdminSelectAdapter(this@AdminAddSongActivity,
                                R.layout.item_choose_option, list)
                        binding?.spnArtist?.adapter = adapter
                        binding?.spnArtist?.onItemSelectedListener = object : OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                                mArtistSelected = adapter.getItem(position)
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {}
                        }
                        if (mSong != null && mSong!!.artistId > 0) {
                            binding?.spnArtist?.setSelection(getPositionSelected(list, mSong!!.artistId))
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
    }

    private fun getPositionSelected(list: List<SelectObject?>, id: Long): Int {
        var position = 0
        for (i in list.indices) {
            if (id == list[i]!!.id) {
                position = i
                break
            }
        }
        return position
    }

    private fun addOrEditSong() {
        val strName = binding?.edtName?.text.toString().trim { it <= ' ' }
        val strImage = binding?.edtImage?.text.toString().trim { it <= ' ' }
        val strUrl = binding?.edtLink?.text.toString().trim { it <= ' ' }
        if (isEmpty(strName)) {
            Toast.makeText(this, getString(R.string.msg_name_require), Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmpty(strImage)) {
            Toast.makeText(this, getString(R.string.msg_image_require), Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmpty(strUrl)) {
            Toast.makeText(this, getString(R.string.msg_url_require), Toast.LENGTH_SHORT).show()
            return
        }

        // Update song
        if (isUpdate) {
            showProgressDialog(true)
            val map: MutableMap<String, Any?> = HashMap()
            map["title"] = strName
            map["image"] = strImage
            map["url"] = strUrl
            map["featured"] = binding?.chbFeatured?.isChecked
            map["categoryId"] = mCategorySelected!!.id
            map["category"] = mCategorySelected!!.name
            map["artistId"] = mArtistSelected!!.id
            map["artist"] = mArtistSelected!!.name
            MyApplication[this].songsDatabaseReference()
                    ?.child(mSong!!.id.toString())
                    ?.updateChildren(map) { _: DatabaseError?, _: DatabaseReference? ->
                        showProgressDialog(false)
                        Toast.makeText(this@AdminAddSongActivity,
                                getString(R.string.msg_edit_song_success), Toast.LENGTH_SHORT).show()
                        hideSoftKeyboard(this)
                    }
            return
        }

        // Add song
        showProgressDialog(true)
        val songId = System.currentTimeMillis()
        val song = Song(songId, strName, strImage, strUrl, mArtistSelected!!.id,
                mArtistSelected!!.name, mCategorySelected!!.id, mCategorySelected!!.name,
                binding?.chbFeatured?.isChecked)
        MyApplication[this].songsDatabaseReference()
                ?.child(songId.toString())
                ?.setValue(song) { _: DatabaseError?, _: DatabaseReference? ->
                    showProgressDialog(false)
                    binding?.edtName?.setText("")
                    binding?.edtImage?.setText("")
                    binding?.edtLink?.setText("")
                    binding?.chbFeatured?.isChecked = false
                    binding?.spnCategory?.setSelection(0)
                    binding?.spnArtist?.setSelection(0)
                    hideSoftKeyboard(this)
                    Toast.makeText(this, getString(R.string.msg_add_song_success), Toast.LENGTH_SHORT).show()
                }
    }
}