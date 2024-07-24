package com.pro.music.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.pro.music.R
import com.pro.music.adapter.AdminViewPagerAdapter
import com.pro.music.databinding.ActivityAdminMainBinding

class AdminMainActivity : BaseActivity() {

    private var binding: ActivityAdminMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setToolBar()
        binding?.viewpager2?.isUserInputEnabled = false
        binding?.viewpager2?.offscreenPageLimit = 5
        val adminViewPagerAdapter = AdminViewPagerAdapter(this)
        binding?.viewpager2?.adapter = adminViewPagerAdapter
        binding?.viewpager2?.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> binding?.bottomNavigation?.menu?.findItem(R.id.nav_category)?.isChecked = true
                    1 -> binding?.bottomNavigation?.menu?.findItem(R.id.nav_artist)?.isChecked = true
                    2 -> binding?.bottomNavigation?.menu?.findItem(R.id.nav_song)?.isChecked = true
                    3 -> binding?.bottomNavigation?.menu?.findItem(R.id.nav_feedback)?.isChecked = true
                    4 -> binding?.bottomNavigation?.menu?.findItem(R.id.nav_account)?.isChecked = true
                }
            }
        })
        binding?.bottomNavigation?.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_category -> {
                    binding?.viewpager2?.currentItem = 0
                }
                R.id.nav_artist -> {
                    binding?.viewpager2?.currentItem = 1
                }
                R.id.nav_song -> {
                    binding?.viewpager2?.currentItem = 2
                }
                R.id.nav_feedback -> {
                    binding?.viewpager2?.currentItem = 3
                }
                R.id.nav_account -> {
                    binding?.viewpager2?.currentItem = 4
                }
            }
            true
        }
    }

    override fun onBackPressed() {
        showConfirmExitApp()
    }

    private fun showConfirmExitApp() {
        MaterialDialog.Builder(this)
                .title(getString(R.string.app_name))
                .content(getString(R.string.msg_exit_app))
                .positiveText(getString(R.string.action_ok))
                .onPositive { _: MaterialDialog?, _: DialogAction? -> finishAffinity() }
                .negativeText(getString(R.string.action_cancel))
                .cancelable(false)
                .show()
    }

    private fun setToolBar() {
        binding?.header?.imgLeft?.visibility = View.GONE
        binding?.header?.tvTitle?.text = getString(R.string.app_name)
    }
}