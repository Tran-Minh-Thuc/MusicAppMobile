package com.pro.music.fragment.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.pro.music.activity.AdminChangePasswordActivity
import com.pro.music.activity.SignInActivity
import com.pro.music.constant.GlobalFunction
import com.pro.music.databinding.FragmentAdminAccountBinding
import com.pro.music.prefs.DataStoreManager.Companion.user

class AdminAccountFragment : Fragment() {

    private var binding: FragmentAdminAccountBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentAdminAccountBinding.inflate(inflater, container, false)
        initUi()
        return binding?.root
    }

    private fun initUi() {
        binding?.tvEmail?.text = user?.email
        binding?.tvChangePassword?.setOnClickListener { onClickChangePassword() }
        binding?.tvSignOut?.setOnClickListener { onClickSignOut() }
    }

    private fun onClickChangePassword() {
        GlobalFunction.startActivity(activity, AdminChangePasswordActivity::class.java)
    }

    private fun onClickSignOut() {
        if (activity == null) return
        FirebaseAuth.getInstance().signOut()
        user = null
        GlobalFunction.startActivity(activity, SignInActivity::class.java)
        activity!!.finishAffinity()
    }
}