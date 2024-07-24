package com.pro.music.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.pro.music.MyApplication
import com.pro.music.R
import com.pro.music.constant.Constant
import com.pro.music.constant.GlobalFunction.hideSoftKeyboard
import com.pro.music.databinding.ActivityAdminAddArtistBinding
import com.pro.music.model.Artist
import com.pro.music.utils.StringUtil.isEmpty
import kotlin.collections.set

class AdminAddArtistActivity : BaseActivity() {

    private var binding: ActivityAdminAddArtistBinding? = null
    private var isUpdate = false
    private var mArtist: Artist? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminAddArtistBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        loadDataIntent()
        initToolbar()
        initView()
        binding?.btnAddOrEdit?.setOnClickListener { addOrEditArtist() }
    }

    private fun loadDataIntent() {
        val bundleReceived = intent.extras
        if (bundleReceived != null) {
            isUpdate = true
            mArtist = bundleReceived[Constant.KEY_INTENT_ARTIST_OBJECT] as Artist?
        }
    }

    private fun initToolbar() {
        binding?.toolbar?.imgLeft?.setImageResource(R.drawable.ic_back_white)
        binding?.toolbar?.layoutPlayAll?.visibility = View.GONE
        binding?.toolbar?.imgLeft?.setOnClickListener { onBackPressed() }
    }

    private fun initView() {
        if (isUpdate) {
            binding?.toolbar?.tvTitle?.text = getString(R.string.label_update_artist)
            binding?.btnAddOrEdit?.text = getString(R.string.action_edit)
            binding?.edtName?.setText(mArtist!!.name)
            binding?.edtImage?.setText(mArtist!!.image)
        } else {
            binding?.toolbar?.tvTitle?.text = getString(R.string.label_add_artist)
            binding?.btnAddOrEdit?.text = getString(R.string.action_add)
        }
    }

    private fun addOrEditArtist() {
        val strName = binding?.edtName?.text.toString().trim { it <= ' ' }
        val strImage = binding?.edtImage?.text.toString().trim { it <= ' ' }
        if (isEmpty(strName)) {
            Toast.makeText(this, getString(R.string.msg_name_require), Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmpty(strImage)) {
            Toast.makeText(this, getString(R.string.msg_image_require), Toast.LENGTH_SHORT).show()
            return
        }

        // Update artist
        if (isUpdate) {
            showProgressDialog(true)
            val map: MutableMap<String, Any> = HashMap()
            map["name"] = strName
            map["image"] = strImage
            MyApplication[this].artistDatabaseReference()
                    ?.child(mArtist!!.id.toString())
                    ?.updateChildren(map) { _: DatabaseError?, _: DatabaseReference? ->
                        showProgressDialog(false)
                        Toast.makeText(this@AdminAddArtistActivity,
                                getString(R.string.msg_edit_artist_success), Toast.LENGTH_SHORT).show()
                        hideSoftKeyboard(this)
                    }
            return
        }

        // Add artist
        showProgressDialog(true)
        val artistId = System.currentTimeMillis()
        val artist = Artist(artistId, strName, strImage)
        MyApplication[this].artistDatabaseReference()
                ?.child(artistId.toString())
                ?.setValue(artist) { _: DatabaseError?, _: DatabaseReference? ->
                    showProgressDialog(false)
                    binding?.edtName?.setText("")
                    binding?.edtImage?.setText("")
                    hideSoftKeyboard(this)
                    Toast.makeText(this, getString(R.string.msg_add_artist_success), Toast.LENGTH_SHORT).show()
                }
    }
}