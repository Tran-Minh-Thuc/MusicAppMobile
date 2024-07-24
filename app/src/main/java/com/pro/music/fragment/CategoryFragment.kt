package com.pro.music.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.pro.music.MyApplication
import com.pro.music.R
import com.pro.music.activity.MainActivity
import com.pro.music.adapter.CategoryAdapter
import com.pro.music.constant.Constant
import com.pro.music.constant.GlobalFunction
import com.pro.music.databinding.FragmentCategoryBinding
import com.pro.music.listener.IOnClickCategoryItemListener
import com.pro.music.model.Category

class CategoryFragment : Fragment() {

    private var binding: FragmentCategoryBinding? = null
    private var mListCategory: MutableList<Category>? = null
    private var mCategoryAdapter: CategoryAdapter? = null
    @JvmField
    var mIsFromMenuLeft = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentCategoryBinding.inflate(inflater, container, false)
        loadDataIntent()
        initUi()
        loadListAllCategory()
        return binding?.root
    }

    private fun loadDataIntent() {
        val bundle = arguments ?: return
        mIsFromMenuLeft = bundle.getBoolean(Constant.IS_FROM_MENU_LEFT)
    }

    private fun initUi() {
        if (activity == null) return
        val gridLayoutManager = GridLayoutManager(activity, 2)
        binding?.rcvData?.layoutManager = gridLayoutManager
        mListCategory = ArrayList()
        mCategoryAdapter = CategoryAdapter(mListCategory, object : IOnClickCategoryItemListener {
            override fun onClickItemCategory(category: Category) {
                val mainActivity = activity as MainActivity?
                mainActivity?.clickOpenSongsByCategory(category)
            }
        })
        binding?.rcvData?.adapter = mCategoryAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadListAllCategory() {
        if (activity == null) return
        MyApplication[activity!!].categoryDatabaseReference()
                ?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (mListCategory == null) {
                            mListCategory = ArrayList()
                        } else {
                            mListCategory!!.clear()
                        }
                        for (dataSnapshot in snapshot.children) {
                            val category = dataSnapshot.getValue(Category::class.java)
                                    ?: return
                            mListCategory!!.add(0, category)
                        }
                        if (mCategoryAdapter != null) mCategoryAdapter!!.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        GlobalFunction.showToastMessage(activity, getString(R.string.msg_get_date_error))
                    }
                })
    }

    companion object {
        @JvmStatic
        fun newInstance(isFromMenuLeft: Boolean): CategoryFragment {
            val fragment = CategoryFragment()
            val bundle = Bundle()
            bundle.putBoolean(Constant.IS_FROM_MENU_LEFT, isFromMenuLeft)
            fragment.arguments = bundle
            return fragment
        }
    }
}