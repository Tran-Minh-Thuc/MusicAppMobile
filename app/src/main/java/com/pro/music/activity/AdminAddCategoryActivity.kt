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
import com.pro.music.databinding.ActivityAdminAddCategoryBinding
import com.pro.music.model.Category
import com.pro.music.utils.StringUtil.isEmpty
import kotlin.collections.set

class AdminAddCategoryActivity : BaseActivity() {

    private var binding: ActivityAdminAddCategoryBinding? = null
    private var isUpdate = false
    private var mCategory: Category? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        loadDataIntent()
        initToolbar()
        initView()
        binding?.btnAddOrEdit?.setOnClickListener { addOrEditCategory() }
    }

    private fun loadDataIntent() {
        val bundleReceived = intent.extras
        if (bundleReceived != null) {
            isUpdate = true
            mCategory = bundleReceived[Constant.KEY_INTENT_CATEGORY_OBJECT] as Category?
        }
    }

    private fun initToolbar() {
        binding?.toolbar?.imgLeft?.setImageResource(R.drawable.ic_back_white)
        binding?.toolbar?.layoutPlayAll?.visibility = View.GONE
        binding?.toolbar?.imgLeft?.setOnClickListener { onBackPressed() }
    }

    private fun initView() {
        if (isUpdate) {
            binding?.toolbar?.tvTitle?.text = getString(R.string.label_update_category)
            binding?.btnAddOrEdit?.text = getString(R.string.action_edit)
            binding?.edtName?.setText(mCategory!!.name)
            binding?.edtImage?.setText(mCategory!!.image)
        } else {
            binding?.toolbar?.tvTitle?.text = getString(R.string.label_add_category)
            binding?.btnAddOrEdit?.text = getString(R.string.action_add)
        }
    }

    private fun addOrEditCategory() {
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

        // Update category
        if (isUpdate) {
            showProgressDialog(true)
            val map: MutableMap<String, Any> = HashMap()
            map["name"] = strName
            map["image"] = strImage
            MyApplication[this].categoryDatabaseReference()
                    ?.child(mCategory!!.id.toString())
                    ?.updateChildren(map) { _: DatabaseError?, _: DatabaseReference? ->
                        showProgressDialog(false)
                        Toast.makeText(this@AdminAddCategoryActivity,
                                getString(R.string.msg_edit_category_success), Toast.LENGTH_SHORT).show()
                        hideSoftKeyboard(this)
                    }
            return
        }

        // Add category
        showProgressDialog(true)
        val categoryId = System.currentTimeMillis()
        val category = Category(categoryId, strName, strImage)
        MyApplication[this].categoryDatabaseReference()
                ?.child(categoryId.toString())
                ?.setValue(category) { _: DatabaseError?, _: DatabaseReference? ->
                    showProgressDialog(false)
                    binding?.edtName?.setText("")
                    binding?.edtImage?.setText("")
                    hideSoftKeyboard(this)
                    Toast.makeText(this, getString(R.string.msg_add_category_success), Toast.LENGTH_SHORT).show()
                }
    }
}