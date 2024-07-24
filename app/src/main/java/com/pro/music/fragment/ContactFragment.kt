package com.pro.music.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.pro.music.R
import com.pro.music.adapter.ContactAdapter
import com.pro.music.constant.AboutUsConfig
import com.pro.music.constant.GlobalFunction
import com.pro.music.databinding.FragmentContactBinding
import com.pro.music.model.Contact

class ContactFragment : Fragment() {

    private var binding: FragmentContactBinding? = null
    private var mContactAdapter: ContactAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentContactBinding.inflate(inflater, container, false)
        initUi()
        initListener()
        return binding?.root
    }

    private fun initUi() {
        binding?.tvAboutUsTitle?.text = AboutUsConfig.ABOUT_US_TITLE
        binding?.tvAboutUsContent?.text = AboutUsConfig.ABOUT_US_CONTENT
        binding?.tvAboutUsWebsite?.text = AboutUsConfig.ABOUT_US_WEBSITE_TITLE
        mContactAdapter = ContactAdapter(activity, loadListContact(), object : ContactAdapter.ICallPhone {
            override fun onClickCallPhone() {
                activity?.let { GlobalFunction.callPhoneNumber(it) }
            }
        })
        val layoutManager = GridLayoutManager(activity, 3)
        binding?.rcvData?.isNestedScrollingEnabled = false
        binding?.rcvData?.isFocusable = false
        binding?.rcvData?.layoutManager = layoutManager
        binding?.rcvData?.adapter = mContactAdapter
    }

    private fun initListener() {
        binding?.layoutWebsite?.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AboutUsConfig.WEBSITE)))
        }
    }

    private fun loadListContact(): List<Contact> {
        val contactArrayList: MutableList<Contact> = ArrayList()
        contactArrayList.add(Contact(Contact.FACEBOOK, R.drawable.ic_facebook))
        contactArrayList.add(Contact(Contact.HOTLINE, R.drawable.ic_hotline))
        contactArrayList.add(Contact(Contact.GMAIL, R.drawable.ic_gmail))
        contactArrayList.add(Contact(Contact.SKYPE, R.drawable.ic_skype))
        contactArrayList.add(Contact(Contact.YOUTUBE, R.drawable.ic_youtube))
        contactArrayList.add(Contact(Contact.ZALO, R.drawable.ic_zalo))
        return contactArrayList
    }

    override fun onDestroy() {
        super.onDestroy()
        mContactAdapter?.release()
    }
}