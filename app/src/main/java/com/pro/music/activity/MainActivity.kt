package com.pro.music.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.auth.FirebaseAuth
import com.pro.music.R
import com.pro.music.constant.Constant
import com.pro.music.constant.GlobalFunction.hideSoftKeyboard
import com.pro.music.constant.GlobalFunction.startActivity
import com.pro.music.constant.GlobalFunction.startDownloadFile
import com.pro.music.constant.GlobalFunction.startMusicService
import com.pro.music.databinding.ActivityMainBinding
import com.pro.music.fragment.AllSongsFragment
import com.pro.music.fragment.ArtistFragment
import com.pro.music.fragment.CategoryFragment
import com.pro.music.fragment.ChangePasswordFragment
import com.pro.music.fragment.ContactFragment
import com.pro.music.fragment.FavoriteFragment
import com.pro.music.fragment.FeedbackFragment
import com.pro.music.fragment.HomeFragment
import com.pro.music.fragment.PopularSongsFragment
import com.pro.music.fragment.SearchFragment
import com.pro.music.fragment.SongsByArtistFragment
import com.pro.music.fragment.SongsByCategoryFragment
import com.pro.music.model.Artist
import com.pro.music.model.Category
import com.pro.music.model.Song
import com.pro.music.prefs.DataStoreManager.Companion.user
import com.pro.music.service.MusicService
import com.pro.music.utils.GlideUtils.loadUrl

@SuppressLint("NonConstantResourceId")
class MainActivity : BaseActivity(), View.OnClickListener {

    private var mSong: Song? = null
    private var mTypeScreen = TYPE_HOME
    var activityMainBinding: ActivityMainBinding? = null
        private set
    private var mAction = 0
    private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            mAction = intent.getIntExtra(Constant.MUSIC_ACTION, 0)
            handleMusicAction()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding?.root)
        checkNotificationPermission()
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                IntentFilter(Constant.CHANGE_LISTENER))
        openHomeScreen()
        displayUserInformation()
        initListener()
        displayLayoutBottom()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
    }

    private fun displayUserInformation() {
        val user = user
        activityMainBinding?.menuLeft?.tvUserEmail?.text = user!!.email
    }

    private fun initHeader() {
        when (mTypeScreen) {
            TYPE_HOME -> {
                handleToolbarTitle(getString(R.string.app_name))
                handleDisplayIconHeader(true)
                handleDisplayButtonPlayAll(false)
            }

            TYPE_CATEGORY -> {
                handleToolbarTitle(getString(R.string.menu_category))
                handleDisplayIconHeader(true)
                handleDisplayButtonPlayAll(false)
            }

            TYPE_ARTIST -> {
                handleToolbarTitle(getString(R.string.menu_artist))
                handleDisplayIconHeader(true)
                handleDisplayButtonPlayAll(false)
            }

            TYPE_ALL_SONGS -> {
                handleToolbarTitle(getString(R.string.menu_all_songs))
                handleDisplayIconHeader(true)
                handleDisplayButtonPlayAll(true)
            }

            TYPE_POPULAR_SONGS -> {
                handleToolbarTitle(getString(R.string.menu_popular_songs))
                handleDisplayIconHeader(true)
                handleDisplayButtonPlayAll(true)
            }

            TYPE_FAVORITE_SONGS -> {
                handleToolbarTitle(getString(R.string.menu_favorite_songs))
                handleDisplayIconHeader(true)
                handleDisplayButtonPlayAll(true)
            }

            TYPE_FEEDBACK -> {
                handleToolbarTitle(getString(R.string.menu_feedback))
                handleDisplayIconHeader(true)
                handleDisplayButtonPlayAll(false)
            }

            TYPE_CONTACT -> {
                handleToolbarTitle(getString(R.string.menu_contact))
                handleDisplayIconHeader(true)
                handleDisplayButtonPlayAll(false)
            }

            TYPE_CHANGE_PASSWORD -> {
                handleToolbarTitle(getString(R.string.menu_change_password))
                handleDisplayIconHeader(true)
                handleDisplayButtonPlayAll(false)
            }
        }
    }

    private fun handleToolbarTitle(title: String?) {
        activityMainBinding?.header?.tvTitle?.text = title
    }

    private fun handleDisplayIconHeader(isShowMenuLeft: Boolean) {
        if (isShowMenuLeft) {
            activityMainBinding?.header?.imgLeft?.setImageResource(R.drawable.ic_menu_left)
            activityMainBinding?.header?.imgLeft?.setOnClickListener {
                activityMainBinding?.drawerLayout?.openDrawer(GravityCompat.START)
            }
        } else {
            activityMainBinding?.header?.imgLeft?.setImageResource(R.drawable.ic_back_white)
            activityMainBinding?.header?.imgLeft?.setOnClickListener { onBackPressed() }
        }
    }

    private fun handleDisplayButtonPlayAll(isShow: Boolean) {
        if (isShow) {
            activityMainBinding?.header?.layoutPlayAll?.visibility = View.VISIBLE
        } else {
            activityMainBinding?.header?.layoutPlayAll?.visibility = View.GONE
        }
    }

    private fun initListener() {
        activityMainBinding?.header?.layoutPlayAll?.setOnClickListener(this)
        activityMainBinding?.menuLeft?.layoutClose?.setOnClickListener(this)
        activityMainBinding?.menuLeft?.layoutMenuHome?.setOnClickListener(this)
        activityMainBinding?.menuLeft?.layoutMenuCategory?.setOnClickListener(this)
        activityMainBinding?.menuLeft?.layoutMenuArtist?.setOnClickListener(this)
        activityMainBinding?.menuLeft?.layoutMenuAllSongs?.setOnClickListener(this)
        activityMainBinding?.menuLeft?.layoutMenuPopularSongs?.setOnClickListener(this)
        activityMainBinding?.menuLeft?.layoutMenuFavoriteSongs?.setOnClickListener(this)
        activityMainBinding?.menuLeft?.layoutMenuFeedback?.setOnClickListener(this)
        activityMainBinding?.menuLeft?.layoutMenuContact?.setOnClickListener(this)
        activityMainBinding?.menuLeft?.layoutMenuChangePassword?.setOnClickListener(this)
        activityMainBinding?.menuLeft?.layoutMenuSignOut?.setOnClickListener(this)
        activityMainBinding?.layoutBottom?.imgPrevious?.setOnClickListener(this)
        activityMainBinding?.layoutBottom?.imgPlay?.setOnClickListener(this)
        activityMainBinding?.layoutBottom?.imgNext?.setOnClickListener(this)
        activityMainBinding?.layoutBottom?.imgClose?.setOnClickListener(this)
        activityMainBinding?.layoutBottom?.layoutText?.setOnClickListener(this)
        activityMainBinding?.layoutBottom?.imgSong?.setOnClickListener(this)
        supportFragmentManager.addOnBackStackChangedListener {
            when (val fragment = supportFragmentManager.findFragmentById(R.id.content_frame)) {
                is HomeFragment -> {
                    handleToolbarTitle(getString(R.string.app_name))
                    handleDisplayIconHeader(true)
                    handleDisplayButtonPlayAll(false)
                }

                is CategoryFragment -> {
                    handleToolbarTitle(getString(R.string.menu_category))
                    handleDisplayButtonPlayAll(false)
                    handleDisplayIconHeader(fragment.mIsFromMenuLeft)
                }

                is ArtistFragment -> {
                    handleToolbarTitle(getString(R.string.menu_artist))
                    handleDisplayButtonPlayAll(false)
                    handleDisplayIconHeader(fragment.mIsFromMenuLeft)
                }
            }
        }
    }

    private fun openHomeScreen() {
        replaceFragment(HomeFragment())
        mTypeScreen = TYPE_HOME
        initHeader()
    }

    private fun openCategoryScreen() {
        replaceFragment(CategoryFragment.newInstance(true))
        mTypeScreen = TYPE_CATEGORY
        initHeader()
    }

    private fun openArtistScreen() {
        replaceFragment(ArtistFragment.newInstance(true))
        mTypeScreen = TYPE_ARTIST
        initHeader()
    }

    private fun openAllSongsScreen() {
        replaceFragment(AllSongsFragment())
        mTypeScreen = TYPE_ALL_SONGS
        initHeader()
    }

    private fun openPopularSongsScreen() {
        replaceFragment(PopularSongsFragment())
        mTypeScreen = TYPE_POPULAR_SONGS
        initHeader()
    }

    private fun openFavoriteSongsScreen() {
        replaceFragment(FavoriteFragment())
        mTypeScreen = TYPE_FAVORITE_SONGS
        initHeader()
    }

    private fun openFeedbackScreen() {
        replaceFragment(FeedbackFragment())
        mTypeScreen = TYPE_FEEDBACK
        initHeader()
    }

    private fun openContactScreen() {
        replaceFragment(ContactFragment())
        mTypeScreen = TYPE_CONTACT
        initHeader()
    }

    private fun openChangePasswordScreen() {
        replaceFragment(ChangePasswordFragment())
        mTypeScreen = TYPE_CHANGE_PASSWORD
        initHeader()
    }

    fun clickSeeAllCategory() {
        addFragment(CategoryFragment.newInstance(false))
        handleToolbarTitle(getString(R.string.menu_category))
        handleDisplayIconHeader(false)
        handleDisplayButtonPlayAll(false)
    }

    fun clickSeeAllArtist() {
        addFragment(ArtistFragment.newInstance(false))
        handleToolbarTitle(getString(R.string.menu_artist))
        handleDisplayIconHeader(false)
        handleDisplayButtonPlayAll(false)
    }

    fun clickSeeAllPopularSongs() {
        addFragment(PopularSongsFragment())
        handleToolbarTitle(getString(R.string.menu_popular_songs))
        handleDisplayIconHeader(false)
        handleDisplayButtonPlayAll(true)
    }

    fun clickSeeAllFavoriteSongs() {
        addFragment(FavoriteFragment())
        handleToolbarTitle(getString(R.string.menu_favorite_songs))
        handleDisplayIconHeader(false)
        handleDisplayButtonPlayAll(true)
    }

    fun clickOpenSongsByCategory(category: Category) {
        addFragment(SongsByCategoryFragment.newInstance(category.id))
        handleToolbarTitle(category.name)
        handleDisplayIconHeader(false)
        handleDisplayButtonPlayAll(true)
    }

    fun clickOpenSongsByArtist(artist: Artist) {
        addFragment(SongsByArtistFragment.newInstance(artist.id))
        handleToolbarTitle(artist.name)
        handleDisplayIconHeader(false)
        handleDisplayButtonPlayAll(true)
    }

    fun clickSearchSongScreen() {
        addFragment(SearchFragment())
        handleToolbarTitle(getString(R.string.label_search))
        handleDisplayIconHeader(false)
        handleDisplayButtonPlayAll(true)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.layout_close -> activityMainBinding?.drawerLayout?.closeDrawer(GravityCompat.START)
            R.id.layout_menu_home -> {
                activityMainBinding?.drawerLayout?.closeDrawer(GravityCompat.START)
                openHomeScreen()
            }

            R.id.layout_menu_category -> {
                activityMainBinding?.drawerLayout?.closeDrawer(GravityCompat.START)
                openCategoryScreen()
            }

            R.id.layout_menu_artist -> {
                activityMainBinding?.drawerLayout?.closeDrawer(GravityCompat.START)
                openArtistScreen()
            }

            R.id.layout_menu_all_songs -> {
                activityMainBinding?.drawerLayout?.closeDrawer(GravityCompat.START)
                openAllSongsScreen()
            }

            R.id.layout_menu_popular_songs -> {
                activityMainBinding?.drawerLayout?.closeDrawer(GravityCompat.START)
                openPopularSongsScreen()
            }

            R.id.layout_menu_favorite_songs -> {
                activityMainBinding?.drawerLayout?.closeDrawer(GravityCompat.START)
                openFavoriteSongsScreen()
            }

            R.id.layout_menu_feedback -> {
                activityMainBinding?.drawerLayout?.closeDrawer(GravityCompat.START)
                openFeedbackScreen()
            }

            R.id.layout_menu_contact -> {
                activityMainBinding?.drawerLayout?.closeDrawer(GravityCompat.START)
                openContactScreen()
            }

            R.id.layout_menu_change_password -> {
                activityMainBinding?.drawerLayout?.closeDrawer(GravityCompat.START)
                openChangePasswordScreen()
            }

            R.id.layout_menu_sign_out -> onClickSignOut()
            R.id.img_previous -> clickOnPrevButton()
            R.id.img_play -> clickOnPlayButton()
            R.id.img_next -> clickOnNextButton()
            R.id.img_close -> clickOnCloseButton()
            R.id.layout_text, R.id.img_song -> openPlayMusicActivity()
        }
    }

    private fun replaceFragment(fragment: Fragment?) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.content_frame, fragment!!).commitAllowingStateLoss()
    }

    private fun addFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.content_frame, fragment)
                .addToBackStack(fragment.javaClass.name)
                .commit()
    }

    private fun showConfirmExitApp() {
        MaterialDialog.Builder(this)
                .title(getString(R.string.app_name))
                .content(getString(R.string.msg_exit_app))
                .positiveText(getString(R.string.action_ok))
                .onPositive { _: MaterialDialog?, _: DialogAction? -> finish() }
                .negativeText(getString(R.string.action_cancel))
                .cancelable(false)
                .show()
    }

    private fun displayLayoutBottom() {
        if (MusicService.mPlayer == null) {
            activityMainBinding?.layoutBottom?.layoutItem?.visibility = View.GONE
            return
        }
        activityMainBinding?.layoutBottom?.layoutItem?.visibility = View.VISIBLE
        showInfoSong()
        showStatusButtonPlay()
    }

    private fun handleMusicAction() {
        if (Constant.CANCEL_NOTIFICATION == mAction) {
            activityMainBinding?.layoutBottom?.layoutItem?.visibility = View.GONE
            return
        }
        activityMainBinding?.layoutBottom?.layoutItem?.visibility = View.VISIBLE
        showInfoSong()
        showStatusButtonPlay()
    }

    private fun showInfoSong() {
        if (MusicService.mListSongPlaying == null || MusicService.mListSongPlaying!!.isEmpty()) {
            return
        }
        val currentSong = MusicService.mListSongPlaying!![MusicService.mSongPosition]
        activityMainBinding?.layoutBottom?.tvSongName?.text = currentSong.title
        activityMainBinding?.layoutBottom?.tvArtist?.text = currentSong.artist
        activityMainBinding?.layoutBottom?.imgSong?.let { loadUrl(currentSong.image, it) }
    }

    private fun showStatusButtonPlay() {
        if (MusicService.isPlaying) {
            activityMainBinding?.layoutBottom?.imgPlay?.setImageResource(R.drawable.ic_pause_black)
        } else {
            activityMainBinding?.layoutBottom?.imgPlay?.setImageResource(R.drawable.ic_play_black)
        }
    }

    private fun clickOnPrevButton() {
        startMusicService(this, Constant.PREVIOUS, MusicService.mSongPosition)
    }

    private fun clickOnNextButton() {
        startMusicService(this, Constant.NEXT, MusicService.mSongPosition)
    }

    private fun clickOnPlayButton() {
        if (MusicService.isPlaying) {
            startMusicService(this, Constant.PAUSE, MusicService.mSongPosition)
        } else {
            startMusicService(this, Constant.RESUME, MusicService.mSongPosition)
        }
    }

    private fun clickOnCloseButton() {
        startMusicService(this, Constant.CANCEL_NOTIFICATION, MusicService.mSongPosition)
    }

    private fun openPlayMusicActivity() {
        startActivity(this, PlayMusicActivity::class.java)
    }

    private fun onClickSignOut() {
        FirebaseAuth.getInstance().signOut()
        user = null
        // Stop service when user sign out
        clickOnCloseButton()
        startActivity(this, SignInActivity::class.java)
        finishAffinity()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount < 1) {
            showConfirmExitApp()
        } else {
            hideSoftKeyboard(this)
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver)
    }

    fun downloadSong(song: Song?) {
        mSong = song
        checkPermission()
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                val permission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(permission, REQUEST_PERMISSION_CODE)
            } else {
                startDownloadFile(this, mSong)
            }
        } else {
            startDownloadFile(this, mSong)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDownloadFile(this, mSong)
            } else {
                Toast.makeText(this, getString(R.string.msg_permission_denied),
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val TYPE_HOME = 1
        const val TYPE_CATEGORY = 2
        const val TYPE_ARTIST = 3
        const val TYPE_ALL_SONGS = 4
        const val TYPE_POPULAR_SONGS = 5
        const val TYPE_FAVORITE_SONGS = 6
        const val TYPE_FEEDBACK = 7
        const val TYPE_CONTACT = 8
        const val TYPE_CHANGE_PASSWORD = 9
        private const val REQUEST_PERMISSION_CODE = 10
    }
}