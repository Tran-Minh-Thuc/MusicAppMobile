package com.pro.music.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.pro.music.R
import com.pro.music.activity.MainActivity
import com.pro.music.databinding.FragmentChangePasswordBinding
import com.pro.music.prefs.DataStoreManager
import com.pro.music.prefs.DataStoreManager.Companion.user
import com.pro.music.utils.StringUtil.isEmpty

class ChangePasswordFragment : Fragment() {

    private var binding: FragmentChangePasswordBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentChangePasswordBinding.inflate(inflater,
                container, false)
        initListener()
        return binding?.root
    }

    private fun initListener() {
        binding?.btnChangePassword?.setOnClickListener { onClickValidateChangePassword() }
    }

    private fun onClickValidateChangePassword() {
        if (activity == null) return
        val strOldPassword = binding?.edtOldPassword?.text.toString().trim { it <= ' ' }
        val strNewPassword = binding?.edtNewPassword?.text.toString().trim { it <= ' ' }
        val strConfirmPassword = binding?.edtConfirmPassword?.text.toString().trim { it <= ' ' }
        if (isEmpty(strOldPassword)) {
            Toast.makeText(activity,
                    getString(R.string.msg_old_password_require), Toast.LENGTH_SHORT).show()
        } else if (isEmpty(strNewPassword)) {
            Toast.makeText(activity,
                    getString(R.string.msg_new_password_require), Toast.LENGTH_SHORT).show()
        } else if (isEmpty(strConfirmPassword)) {
            Toast.makeText(activity,
                    getString(R.string.msg_confirm_password_require), Toast.LENGTH_SHORT).show()
        } else if (user?.password != strOldPassword) {
            Toast.makeText(activity,
                    getString(R.string.msg_old_password_invalid), Toast.LENGTH_SHORT).show()
        } else if (strNewPassword != strConfirmPassword) {
            Toast.makeText(activity,
                    getString(R.string.msg_confirm_password_invalid), Toast.LENGTH_SHORT).show()
        } else if (strOldPassword == strNewPassword) {
            Toast.makeText(activity,
                    getString(R.string.msg_new_password_invalid), Toast.LENGTH_SHORT).show()
        } else {
            changePassword(strNewPassword)
        }
    }

    private fun changePassword(newPassword: String) {
        if (activity == null) return
        val mainActivity = activity as MainActivity?
        mainActivity!!.showProgressDialog(true)
        val user = FirebaseAuth.getInstance().currentUser ?: return
        user.updatePassword(newPassword)
                .addOnCompleteListener { task: Task<Void?> ->
                    mainActivity.showProgressDialog(false)
                    if (task.isSuccessful) {
                        Toast.makeText(mainActivity,
                                getString(R.string.msg_change_password_successfully),
                                Toast.LENGTH_SHORT).show()
                        val userLogin = DataStoreManager.user
                        userLogin!!.password = newPassword
                        DataStoreManager.user = userLogin
                        binding?.edtOldPassword?.setText("")
                        binding?.edtNewPassword?.setText("")
                        binding?.edtConfirmPassword?.setText("")
                    }
                }
    }
}