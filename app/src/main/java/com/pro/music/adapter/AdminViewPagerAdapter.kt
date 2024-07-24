package com.pro.music.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pro.music.fragment.admin.AdminAccountFragment
import com.pro.music.fragment.admin.AdminArtistFragment
import com.pro.music.fragment.admin.AdminCategoryFragment
import com.pro.music.fragment.admin.AdminFeedbackFragment
import com.pro.music.fragment.admin.AdminSongFragment

class AdminViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> AdminArtistFragment()
            2 -> AdminSongFragment()
            3 -> AdminFeedbackFragment()
            4 -> AdminAccountFragment()
            else -> AdminCategoryFragment()
        }
    }

    override fun getItemCount(): Int {
        return 5
    }
}