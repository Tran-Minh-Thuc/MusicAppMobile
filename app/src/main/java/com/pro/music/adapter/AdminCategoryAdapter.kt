package com.pro.music.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pro.music.adapter.AdminCategoryAdapter.AdminCategoryViewHolder
import com.pro.music.databinding.ItemAdminCategoryBinding
import com.pro.music.listener.IOnAdminManagerCategoryListener
import com.pro.music.model.Category
import com.pro.music.utils.GlideUtils.loadUrl

class AdminCategoryAdapter(
        private val mListCategory: List<Category>?,
        private val mListener: IOnAdminManagerCategoryListener
        ) : RecyclerView.Adapter<AdminCategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminCategoryViewHolder {
        val binding = ItemAdminCategoryBinding.inflate(LayoutInflater.from(parent.context),
                parent, false)
        return AdminCategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdminCategoryViewHolder, position: Int) {
        val category = mListCategory!![position]
        loadUrl(category.image, holder.itemBinding.imgCategory)
        holder.itemBinding.tvName.text = category.name
        holder.itemBinding.imgEdit.setOnClickListener { mListener.onClickUpdateCategory(category) }
        holder.itemBinding.imgDelete.setOnClickListener { mListener.onClickDeleteCategory(category) }
        holder.itemBinding.layoutItem.setOnClickListener { mListener.onClickDetailCategory(category) }
    }

    override fun getItemCount(): Int {
        return mListCategory?.size ?: 0
    }

    class AdminCategoryViewHolder(
            val itemBinding: ItemAdminCategoryBinding
            ) : RecyclerView.ViewHolder(itemBinding.root)
}