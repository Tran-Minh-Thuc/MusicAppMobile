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
import com.pro.music.activity.AdminAddCategoryActivity
import com.pro.music.activity.AdminCategorySongActivity
import com.pro.music.adapter.AdminCategoryAdapter
import com.pro.music.constant.Constant
import com.pro.music.constant.GlobalFunction
import com.pro.music.databinding.FragmentAdminCategoryBinding
import com.pro.music.listener.IOnAdminManagerCategoryListener
import com.pro.music.model.Category
import com.pro.music.utils.StringUtil.isEmpty
import java.util.Locale

class AdminCategoryFragment : Fragment() {

    private var binding: FragmentAdminCategoryBinding? = null
    private var mListCategory: MutableList<Category>? = null
    private var mAdminCategoryAdapter: AdminCategoryAdapter? = null
    private var mChildEventListener: ChildEventListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentAdminCategoryBinding.inflate(inflater, container, false)
        initView()
        initListener()
        loadListCategory("")
        return binding?.root
    }

    private fun initView() {
        val linearLayoutManager = LinearLayoutManager(activity)
        binding?.rcvCategory?.layoutManager = linearLayoutManager
        mListCategory = ArrayList()
        mAdminCategoryAdapter = AdminCategoryAdapter(mListCategory, object : IOnAdminManagerCategoryListener {
            override fun onClickUpdateCategory(category: Category) {
                onClickEditCategory(category)
            }

            override fun onClickDeleteCategory(category: Category) {
                deleteCategoryItem(category)
            }

            override fun onClickDetailCategory(category: Category) {
                val bundle = Bundle()
                bundle.putSerializable(Constant.KEY_INTENT_CATEGORY_OBJECT, category)
                GlobalFunction.startActivity(activity, AdminCategorySongActivity::class.java, bundle)
            }
        })
        binding?.rcvCategory?.adapter = mAdminCategoryAdapter
        binding?.rcvCategory?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    binding?.btnAddCategory?.hide()
                } else {
                    binding?.btnAddCategory?.show()
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }

    private fun initListener() {
        binding?.btnAddCategory?.setOnClickListener { onClickAddCategory() }
        binding?.imgSearch?.setOnClickListener { searchCategory() }
        binding?.edtSearchName?.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchCategory()
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
                    searchCategory()
                }
            }
        })
    }

    private fun onClickAddCategory() {
        GlobalFunction.startActivity(activity, AdminAddCategoryActivity::class.java)
    }

    private fun onClickEditCategory(category: Category?) {
        val bundle = Bundle()
        bundle.putSerializable(Constant.KEY_INTENT_CATEGORY_OBJECT, category)
        GlobalFunction.startActivity(activity, AdminAddCategoryActivity::class.java, bundle)
    }

    private fun deleteCategoryItem(category: Category?) {
        AlertDialog.Builder(activity)
                .setTitle(getString(R.string.msg_delete_title))
                .setMessage(getString(R.string.msg_confirm_delete))
                .setPositiveButton(getString(R.string.action_ok)) { _: DialogInterface?, _: Int ->
                    if (activity == null) {
                        return@setPositiveButton
                    }
                    MyApplication[activity!!].categoryDatabaseReference()
                            ?.child(category!!.id.toString())
                            ?.removeValue { _: DatabaseError?, _: DatabaseReference? ->
                                Toast.makeText(activity,
                                        getString(R.string.msg_delete_category_successfully),
                                        Toast.LENGTH_SHORT).show()
                            }
                }
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show()
    }

    private fun searchCategory() {
        val strKey = binding?.edtSearchName?.text.toString().trim { it <= ' ' }
        resetListCategory()
        if (activity != null) {
            MyApplication[activity!!].categoryDatabaseReference()
                    ?.removeEventListener(mChildEventListener!!)
        }
        loadListCategory(strKey)
        GlobalFunction.hideSoftKeyboard(activity)
    }

    private fun resetListCategory() {
        if (mListCategory != null) {
            mListCategory!!.clear()
        } else {
            mListCategory = ArrayList()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun loadListCategory(keyword: String?) {
        if (activity == null) return
        mChildEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val category = dataSnapshot.getValue(Category::class.java)
                if (category == null || mListCategory == null) return
                if (isEmpty(keyword)) {
                    mListCategory!!.add(0, category)
                } else {
                    if (GlobalFunction.getTextSearch(category.name).toLowerCase(Locale.getDefault()).trim { it <= ' ' }
                                    .contains(GlobalFunction.getTextSearch(keyword).toLowerCase(Locale.getDefault()).trim { it <= ' ' })) {
                        mListCategory!!.add(0, category)
                    }
                }
                if (mAdminCategoryAdapter != null) mAdminCategoryAdapter!!.notifyDataSetChanged()
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                val category = dataSnapshot.getValue(Category::class.java)
                if (category == null || mListCategory == null || mListCategory!!.isEmpty()) return
                for (i in mListCategory!!.indices) {
                    if (category.id == mListCategory!![i].id) {
                        mListCategory!![i] = category
                        break
                    }
                }
                if (mAdminCategoryAdapter != null) mAdminCategoryAdapter!!.notifyDataSetChanged()
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val category = dataSnapshot.getValue(Category::class.java)
                if (category == null || mListCategory == null || mListCategory!!.isEmpty()) return
                for (categoryObject in mListCategory!!) {
                    if (category.id == categoryObject.id) {
                        mListCategory!!.remove(categoryObject)
                        break
                    }
                }
                if (mAdminCategoryAdapter != null) mAdminCategoryAdapter!!.notifyDataSetChanged()
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        MyApplication[activity!!].categoryDatabaseReference()?.addChildEventListener(mChildEventListener!!)
    }
}