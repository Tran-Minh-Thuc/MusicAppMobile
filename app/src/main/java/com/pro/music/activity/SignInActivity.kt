package com.pro.music.activity

import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.pro.music.R
import com.pro.music.constant.Constant
import com.pro.music.constant.GlobalFunction.startActivity
import com.pro.music.databinding.ActivitySignInBinding
import com.pro.music.model.User
import com.pro.music.prefs.DataStoreManager
import com.pro.music.prefs.DataStoreManager.Companion.user
import com.pro.music.utils.StringUtil.isEmpty
import com.pro.music.utils.StringUtil.isValidEmail

class SignInActivity : BaseActivity() {

    private var binding: ActivitySignInBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        initListener()
    }

    private fun initListener() {
        binding?.rdbUser?.isChecked = true
        binding?.layoutSignUp?.setOnClickListener {
            startActivity(this@SignInActivity, SignUpActivity::class.java)
        }
        binding?.btnSignIn?.setOnClickListener { onClickValidateSignIn() }
        binding?.tvForgotPassword?.setOnClickListener { onClickForgotPassword() }
    }

    private fun onClickForgotPassword() {
        startActivity(this, ForgotPasswordActivity::class.java)
    }

    private fun onClickValidateSignIn() {
        val strEmail = binding?.edtEmail?.text.toString().trim { it <= ' ' }
        val strPassword = binding?.edtPassword?.text.toString().trim { it <= ' ' }
        if (isEmpty(strEmail)) {
            Toast.makeText(this@SignInActivity, getString(R.string.msg_email_require), Toast.LENGTH_SHORT).show()
        } else if (isEmpty(strPassword)) {
            Toast.makeText(this@SignInActivity, getString(R.string.msg_password_require), Toast.LENGTH_SHORT).show()
        } else if (!isValidEmail(strEmail)) {
            Toast.makeText(this@SignInActivity, getString(R.string.msg_email_invalid), Toast.LENGTH_SHORT).show()
        } else {
            if (binding?.rdbAdmin?.isChecked == true) {
                if (!strEmail.contains(Constant.ADMIN_EMAIL_FORMAT)) {
                    Toast.makeText(this@SignInActivity, getString(R.string.msg_email_invalid_admin), Toast.LENGTH_SHORT).show()
                } else {
                    signInUser(strEmail, strPassword)
                }
                return
            }
            if (strEmail.contains(Constant.ADMIN_EMAIL_FORMAT)) {
                Toast.makeText(this@SignInActivity, getString(R.string.msg_email_invalid_user), Toast.LENGTH_SHORT).show()
            } else {
                signInUser(strEmail, strPassword)
            }
        }
    }

    private fun signInUser(email: String, password: String) {
        showProgressDialog(true)
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task: Task<AuthResult?> ->
                    showProgressDialog(false)
                    if (task.isSuccessful) {
                        val user = firebaseAuth.currentUser
                        if (user != null) {
                            val userObject = User(user.email, password)
                            if (user.email != null && user.email!!.contains(Constant.ADMIN_EMAIL_FORMAT)) {
                                userObject.isAdmin = true
                            }
                            DataStoreManager.user = userObject
                            goToMainActivity()
                        }
                    } else {
                        Toast.makeText(this@SignInActivity, getString(R.string.msg_sign_in_error),
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun goToMainActivity() {
        if (user!!.isAdmin) {
            startActivity(this@SignInActivity, AdminMainActivity::class.java)
        } else {
            startActivity(this@SignInActivity, MainActivity::class.java)
        }
        finishAffinity()
    }
}