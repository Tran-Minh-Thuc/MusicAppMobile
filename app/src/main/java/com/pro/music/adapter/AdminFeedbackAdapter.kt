package com.pro.music.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pro.music.adapter.AdminFeedbackAdapter.AdminFeedbackViewHolder
import com.pro.music.databinding.ItemAdminFeedbackBinding
import com.pro.music.model.Feedback

class AdminFeedbackAdapter(
        private val mListFeedback: List<Feedback>?
        ) : RecyclerView.Adapter<AdminFeedbackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminFeedbackViewHolder {
        val binding = ItemAdminFeedbackBinding.inflate(LayoutInflater.from(parent.context),
                parent, false)
        return AdminFeedbackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdminFeedbackViewHolder, position: Int) {
        val feedback = mListFeedback!![position]
        holder.mItemAdminFeedbackBinding.tvEmail.text = feedback.email
        holder.mItemAdminFeedbackBinding.tvFeedback.text = feedback.comment
    }

    override fun getItemCount(): Int {
        return mListFeedback?.size ?: 0
    }

    class AdminFeedbackViewHolder(
            val mItemAdminFeedbackBinding: ItemAdminFeedbackBinding
            ) : RecyclerView.ViewHolder(mItemAdminFeedbackBinding.root)
}