package com.pro.music.constant

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pro.music.MyApplication
import com.pro.music.R
import com.pro.music.activity.MainActivity
import com.pro.music.activity.PlayMusicActivity
import com.pro.music.databinding.LayoutBottomSheetOptionBinding
import com.pro.music.model.Song
import com.pro.music.model.UserInfor
import com.pro.music.prefs.DataStoreManager.Companion.user
import com.pro.music.service.MusicReceiver
import com.pro.music.service.MusicService
import com.pro.music.service.MusicService.Companion.clearListSongPlaying
import com.pro.music.service.MusicService.Companion.deleteSongFromPlaylist
import com.pro.music.service.MusicService.Companion.isSongExist
import com.pro.music.service.MusicService.Companion.isSongPlaying
import com.pro.music.utils.GlideUtils.loadUrl
import com.pro.music.utils.StringUtil.isEmpty
import java.text.Normalizer
import java.util.regex.Pattern

object GlobalFunction {

    fun startActivity(context: Context?, clz: Class<*>?) {
        val intent = Intent(context, clz)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(intent)
    }

    @JvmStatic
    fun startActivity(context: Context?, clz: Class<*>?, bundle: Bundle?) {
        val intent = Intent(context, clz)
        intent.putExtras(bundle!!)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(intent)
    }

    @JvmStatic
    fun hideSoftKeyboard(activity: Activity?) {
        try {
            val inputMethodManager = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
        } catch (ex: NullPointerException) {
            ex.printStackTrace()
        }
    }

    @JvmStatic
    fun onClickOpenGmail(context: Context) {
        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", AboutUsConfig.GMAIL, null))
        context.startActivity(Intent.createChooser(emailIntent, "Send Email"))
    }

    @JvmStatic
    fun onClickOpenSkype(context: Context) {
        try {
            val skypeUri = Uri.parse("skype:" + AboutUsConfig.SKYPE_ID + "?chat")
            context.packageManager.getPackageInfo("com.skype.raider", 0)
            val skypeIntent = Intent(Intent.ACTION_VIEW, skypeUri)
            skypeIntent.component = ComponentName("com.skype.raider", "com.skype.raider.Main")
            context.startActivity(skypeIntent)
        } catch (e: Exception) {
            openSkypeWebView(context)
        }
    }

    private fun openSkypeWebView(context: Context) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("skype:" + AboutUsConfig.SKYPE_ID + "?chat")))
        } catch (exception: Exception) {
            val skypePackageName = "com.skype.raider"
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$skypePackageName")))
            } catch (e: ActivityNotFoundException) {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$skypePackageName")))
            }
        }
    }

    @JvmStatic
    fun onClickOpenFacebook(context: Context) {
        var intent: Intent
        try {
            var urlFacebook: String = AboutUsConfig.PAGE_FACEBOOK
            val packageManager = context.packageManager
            val versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode
            if (versionCode >= 3002850) { //newer versions of fb app
                urlFacebook = "fb://facewebmodal/f?href=" + AboutUsConfig.LINK_FACEBOOK
            }
            intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlFacebook))
        } catch (e: Exception) {
            intent = Intent(Intent.ACTION_VIEW, Uri.parse(AboutUsConfig.LINK_FACEBOOK))
        }
        context.startActivity(intent)
    }

    @JvmStatic
    fun onClickOpenYoutubeChannel(context: Context) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AboutUsConfig.LINK_YOUTUBE)))
    }

    @JvmStatic
    fun onClickOpenZalo(context: Context) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AboutUsConfig.ZALO_LINK)))
    }

    fun callPhoneNumber(activity: Activity) {
        try {
            if (Build.VERSION.SDK_INT > 22) {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CALL_PHONE), 101)
                    return
                }
                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data = Uri.parse("tel:" + AboutUsConfig.PHONE_NUMBER)
                activity.startActivity(callIntent)
            } else {
                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data = Uri.parse("tel:" + AboutUsConfig.PHONE_NUMBER)
                activity.startActivity(callIntent)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun showToastMessage(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun getTextSearch(input: String?): String {
        val nfdNormalizedString = Normalizer.normalize(input, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(nfdNormalizedString).replaceAll("")
    }

    @JvmStatic
    fun startMusicService(ctx: Context?, action: Int, songPosition: Int) {
        val musicService = Intent(ctx, MusicService::class.java)
        musicService.putExtra(Constant.MUSIC_ACTION, action)
        musicService.putExtra(Constant.SONG_POSITION, songPosition)
        ctx?.startService(musicService)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun openMusicReceiver(ctx: Context, action: Int): PendingIntent {
        val intent = Intent(ctx, MusicReceiver::class.java)
        intent.putExtra(Constant.MUSIC_ACTION, action)
        val pendingFlag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(ctx.applicationContext, action, intent, pendingFlag)
    }

    @JvmStatic
    fun isFavoriteSong(song: Song): Boolean {
        if (song.favorite == null || song.favorite!!.isEmpty()) return false
        val listUsersFavorite: List<UserInfor> = ArrayList(song.favorite!!.values)
        if (listUsersFavorite.isEmpty()) return false
        for (userInfo in listUsersFavorite) {
            if (user!!.email == userInfo.emailUser) {
                return true
            }
        }
        return false
    }

    private fun getUserFavoriteSong(song: Song): UserInfor? {
        var userInfo: UserInfor? = null
        if (song.favorite == null || song.favorite!!.isEmpty()) return null
        val listUsersFavorite: List<UserInfor> = ArrayList(song.favorite!!.values)
        if (listUsersFavorite.isEmpty()) return null
        for (userObject in listUsersFavorite) {
            if (user!!.email == userObject.emailUser) {
                userInfo = userObject
                break
            }
        }
        return userInfo
    }

    fun onClickFavoriteSong(context: Context?, song: Song, isFavorite: Boolean) {
        if (context == null) return
        if (isFavorite) {
            val userEmail = user!!.email
            val userInfo = UserInfor(System.currentTimeMillis(), userEmail)
            MyApplication[context].songsDatabaseReference()
                    ?.child(song.id.toString())
                    ?.child("favorite")
                    ?.child(userInfo.id.toString())
                    ?.setValue(userInfo)
        } else {
            val userInfo = getUserFavoriteSong(song)
            if (userInfo != null) {
                MyApplication[context].songsDatabaseReference()
                        ?.child(song.id.toString())
                        ?.child("favorite")
                        ?.child(userInfo.id.toString())
                        ?.removeValue()
            }
        }
    }

    @SuppressLint("InflateParams")
    fun handleClickMoreOptions(context: Activity?, song: Song?) {
        if (context == null || song == null) return
        val binding = LayoutBottomSheetOptionBinding
                .inflate(LayoutInflater.from(context))
        val bottomSheetDialog = BottomSheetDialog(context)
        bottomSheetDialog.setContentView(binding.root)
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        loadUrl(song.image, binding.imgSong)
        binding.tvSongName.text = song.title
        binding.tvArtist.text = song.artist

        if (isSongExist(song.id)) {
            binding.layoutRemovePlaylist.visibility = View.VISIBLE
            binding.layoutPriority.visibility = View.VISIBLE
            binding.layoutAddPlaylist.visibility = View.GONE
        } else {
            binding.layoutRemovePlaylist.visibility = View.GONE
            binding.layoutPriority.visibility = View.GONE
            binding.layoutAddPlaylist.visibility = View.VISIBLE
        }

        binding.layoutDownload.setOnClickListener {
            val mainActivity = context as MainActivity
            mainActivity.downloadSong(song)
            bottomSheetDialog.hide()
        }

        binding.layoutPriority.setOnClickListener {
            if (isSongPlaying(song.id)) {
                showToastMessage(context, context.getString(R.string.msg_song_playing))
            } else {
                for (songEntity in MusicService.mListSongPlaying!!) {
                    songEntity.isPriority = songEntity.id == song.id
                }
                showToastMessage(context, context.getString(R.string.msg_setting_priority_successfully))
            }
            bottomSheetDialog.hide()
        }

        binding.layoutAddPlaylist.setOnClickListener {
            if (MusicService.mListSongPlaying == null || MusicService.mListSongPlaying!!.isEmpty()) {
                clearListSongPlaying()
                MusicService.mListSongPlaying!!.add(song)
                MusicService.isPlaying = false
                startMusicService(context, Constant.PLAY, 0)
                startActivity(context, PlayMusicActivity::class.java)
            } else {
                MusicService.mListSongPlaying!!.add(song)
                showToastMessage(context, context.getString(R.string.msg_add_song_playlist_success))
            }
            bottomSheetDialog.hide()
        }

        binding.layoutRemovePlaylist.setOnClickListener {
            if (isSongPlaying(song.id)) {
                showToastMessage(context, context.getString(R.string.msg_cannot_delete_song))
            } else {
                deleteSongFromPlaylist(song.id)
                showToastMessage(context, context.getString(R.string.msg_delete_song_from_playlist_success))
            }
            bottomSheetDialog.hide()
        }

        bottomSheetDialog.show()
    }

    @JvmStatic
    fun startDownloadFile(activity: Activity?, song: Song?) {
        if (activity == null || song == null || isEmpty(song.url)) return
        val request = DownloadManager.Request(Uri.parse(song.url))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE
                or DownloadManager.Request.NETWORK_WIFI)
        request.setTitle(activity.getString(R.string.title_download))
        request.setDescription(activity.getString(R.string.message_download))
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        val fileName = song.title + ".mp3"
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }
}